"use strict";
var __asyncValues = (this && this.__asyncValues) || function (o) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var m = o[Symbol.asyncIterator], i;
    return m ? m.call(o) : (o = typeof __values === "function" ? __values(o) : o[Symbol.iterator](), i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i);
    function verb(n) { i[n] = o[n] && function (v) { return new Promise(function (resolve, reject) { v = o[n](v), settle(resolve, reject, v.done, v.value); }); }; }
    function settle(resolve, reject, d, v) { Promise.resolve(v).then(function(v) { resolve({ value: v, done: d }); }, reject); }
};
var __await = (this && this.__await) || function (v) { return this instanceof __await ? (this.v = v, this) : new __await(v); }
var __asyncDelegator = (this && this.__asyncDelegator) || function (o) {
    var i, p;
    return i = {}, verb("next"), verb("throw", function (e) { throw e; }), verb("return"), i[Symbol.iterator] = function () { return this; }, i;
    function verb(n, f) { i[n] = o[n] ? function (v) { return (p = !p) ? { value: __await(o[n](v)), done: n === "return" } : f ? f(v) : v; } : f; }
};
var __asyncGenerator = (this && this.__asyncGenerator) || function (thisArg, _arguments, generator) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var g = generator.apply(thisArg, _arguments || []), i, q = [];
    return i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i;
    function verb(n) { if (g[n]) i[n] = function (v) { return new Promise(function (a, b) { q.push([n, v, a, b]) > 1 || resume(n, v); }); }; }
    function resume(n, v) { try { step(g[n](v)); } catch (e) { settle(q[0][3], e); } }
    function step(r) { r.value instanceof __await ? Promise.resolve(r.value.v).then(fulfill, reject) : settle(q[0][2], r); }
    function fulfill(value) { resume("next", value); }
    function reject(value) { resume("throw", value); }
    function settle(f, v) { if (f(v), q.shift(), q.length) resume(q[0][0], q[0][1]); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.decodeBase64 = exports.encodeBase64 = exports.ScramSha256AuthFlow = exports.verifyScramSha256Password = exports.createScramSha256Data = void 0;
const backend_error_1 = require("../../backend-error");
const connection_1 = require("../../connection");
const crypto_1 = require("../../crypto");
const sasl_mechanism_1 = require("./sasl-mechanism");
async function createScramSha256Data(password, iterations = 4096) {
    const salt = new Uint8Array(16);
    crypto.getRandomValues(salt);
    const saltedPassword = await (0, crypto_1.pbkdf2)(password, salt, iterations, 32, 'SHA-256');
    const clientKey = await (0, crypto_1.createHmacKey)(saltedPassword, 'Client Key', 'SHA-256');
    const storedKey = await (0, crypto_1.createHashKey)(clientKey, 'SHA-256');
    const serverKey = await (0, crypto_1.createHmacKey)(saltedPassword, 'Server Key', 'SHA-256');
    return {
        salt: encodeBase64(salt),
        iterations,
        storedKey: encodeBase64(storedKey),
        serverKey: encodeBase64(serverKey),
    };
}
exports.createScramSha256Data = createScramSha256Data;
async function verifyScramSha256Password(params) {
    const { authMessage, clientProof, storedKey } = params;
    const clientProofBuffer = decodeBase64(clientProof);
    const storedKeyBuffer = decodeBase64(storedKey);
    const clientSignature = await (0, crypto_1.createHmacKey)(storedKeyBuffer, authMessage, 'SHA-256');
    const clientSignatureView = new Uint8Array(clientSignature);
    const clientKey = new Uint8Array(clientProofBuffer.length);
    for (let i = 0; i < clientProofBuffer.length; i++) {
        clientKey[i] = clientProofBuffer[i] ^ clientSignatureView[i];
    }
    const computedStoredKey = await (0, crypto_1.createHashKey)(clientKey, 'SHA-256');
    return await (0, crypto_1.timingSafeEqual)(storedKeyBuffer, computedStoredKey);
}
exports.verifyScramSha256Password = verifyScramSha256Password;
const ScramSha256Step = {
    Initial: 'Initial',
    ServerFirstMessage: 'ServerFirstMessage',
    ServerFinalMessage: 'ServerFinalMessage',
    Completed: 'Completed',
};
class ScramSha256AuthFlow extends sasl_mechanism_1.SaslMechanism {
    constructor(params) {
        var _a;
        super({ writer: params.writer });
        this.step = ScramSha256Step.Initial;
        this.username = params.username;
        this.auth = Object.assign(Object.assign({}, params.auth), { validateCredentials: (_a = params.auth.validateCredentials) !== null && _a !== void 0 ? _a : (async ({ authMessage, clientProof, scramSha256Data }) => {
                return verifyScramSha256Password({
                    authMessage,
                    clientProof,
                    storedKey: scramSha256Data.storedKey,
                });
            }) });
        this.reader = params.reader;
        this.connectionState = params.connectionState;
    }
    async getScramSha256Data(params) {
        if (!this.scramSha256Data) {
            this.scramSha256Data = await this.auth.getScramSha256Data(params, this.connectionState);
        }
        return this.scramSha256Data;
    }
    createInitialAuthMessage() {
        return this.createAuthenticationSASL();
    }
    handleClientMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientMessage_1() {
            switch (this.step) {
                case ScramSha256Step.Initial:
                    return yield __await(yield __await(yield* __asyncDelegator(__asyncValues(this.handleClientFirstMessage(message)))));
                case ScramSha256Step.ServerFirstMessage:
                    return yield __await(yield __await(yield* __asyncDelegator(__asyncValues(this.handleClientFinalMessage(message)))));
                default:
                    throw new Error('Unexpected SCRAM-SHA-256 authentication step');
            }
        });
    }
    handleClientFirstMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientFirstMessage_1() {
            const length = this.reader.int32();
            const saslMechanism = this.reader.cstring();
            if (saslMechanism !== 'SCRAM-SHA-256') {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '28000',
                    message: 'Unsupported SASL authentication mechanism',
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
            const responseLength = this.reader.int32();
            const clientFirstMessage = this.reader.string(responseLength);
            const serverFirstMessage = yield __await(this.createServerFirstMessage(clientFirstMessage));
            this.step = ScramSha256Step.ServerFirstMessage;
            yield yield __await(this.createAuthenticationSASLContinue(serverFirstMessage));
        });
    }
    async createServerFirstMessage(clientFirstMessage) {
        var _a;
        const clientFirstMessageParts = clientFirstMessage.split(',');
        this.clientFirstMessageBare = clientFirstMessageParts.slice(2).join(',');
        const clientNonce = ((_a = clientFirstMessageParts.find((part) => part.startsWith('r='))) === null || _a === void 0 ? void 0 : _a.substring(2)) || '';
        const serverNoncePart = new Uint8Array(18);
        crypto.getRandomValues(serverNoncePart);
        this.serverNonce = clientNonce + encodeBase64(serverNoncePart);
        const { salt, iterations } = await this.getScramSha256Data({
            username: this.username,
        });
        this.serverFirstMessage = `r=${this.serverNonce},s=${salt},i=${iterations}`;
        return this.serverFirstMessage;
    }
    handleClientFinalMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientFinalMessage_1() {
            try {
                const serverFinalMessage = yield __await(this.createServerFinalMessage(message));
                this.step = ScramSha256Step.Completed;
                yield yield __await(this.createAuthenticationSASLFinal(serverFinalMessage));
            }
            catch (error) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '28000',
                    message: error.message,
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
        });
    }
    get isCompleted() {
        return this.step === ScramSha256Step.Completed;
    }
    async createServerFinalMessage(message) {
        var _a, _b, _c;
        const length = this.reader.int32();
        const stringLength = length - 4;
        const clientFinalMessage = this.reader.string(stringLength);
        const clientFinalMessageParts = clientFinalMessage.split(',');
        const channelBinding = (_a = clientFinalMessageParts
            .find((part) => part.startsWith('c='))) === null || _a === void 0 ? void 0 : _a.substring(2);
        const fullNonce = (_b = clientFinalMessageParts.find((part) => part.startsWith('r='))) === null || _b === void 0 ? void 0 : _b.substring(2);
        const clientProof = (_c = clientFinalMessageParts.find((part) => part.startsWith('p='))) === null || _c === void 0 ? void 0 : _c.substring(2);
        if (!channelBinding || !fullNonce || !clientProof) {
            throw new Error('Invalid client final message');
        }
        if (fullNonce !== this.serverNonce) {
            throw new Error('Nonce mismatch');
        }
        const clientFinalMessageWithoutProof = `c=${channelBinding},r=${fullNonce}`;
        const authMessage = `${this.clientFirstMessageBare},${this.serverFirstMessage},${clientFinalMessageWithoutProof}`;
        const data = await this.getScramSha256Data({
            username: this.username,
        });
        const isValid = await this.auth.validateCredentials({
            authMessage,
            clientProof,
            username: this.username,
            scramSha256Data: data,
        }, this.connectionState);
        if (!isValid) {
            throw new Error(`password authentication failed for user "${this.username}"`);
        }
        const serverKeyBuffer = decodeBase64(data.serverKey);
        const serverSignature = await (0, crypto_1.createHmacKey)(serverKeyBuffer, authMessage, 'SHA-256');
        return `v=${encodeBase64(serverSignature)}`;
    }
}
exports.ScramSha256AuthFlow = ScramSha256AuthFlow;
function encodeBase64(data) {
    const uint8 = data;
    let result = "";
    let i;
    const l = uint8.length;
    for (i = 2; i < l; i += 3) {
        result += base64abc[(uint8[i - 2]) >> 2];
        result += base64abc[(((uint8[i - 2]) & 0x03) << 4) |
            ((uint8[i - 1]) >> 4)];
        result += base64abc[(((uint8[i - 1]) & 0x0f) << 2) |
            ((uint8[i]) >> 6)];
        result += base64abc[(uint8[i]) & 0x3f];
    }
    if (i === l + 1) {
        result += base64abc[(uint8[i - 2]) >> 2];
        result += base64abc[((uint8[i - 2]) & 0x03) << 4];
        result += "==";
    }
    if (i === l) {
        result += base64abc[(uint8[i - 2]) >> 2];
        result += base64abc[(((uint8[i - 2]) & 0x03) << 4) |
            ((uint8[i - 1]) >> 4)];
        result += base64abc[((uint8[i - 1]) & 0x0f) << 2];
        result += "=";
    }
    return result;
}
exports.encodeBase64 = encodeBase64;
function decodeBase64(b64) {
    const binString = atob(b64);
    const size = binString.length;
    const bytes = new Uint8Array(size);
    for (let i = 0; i < size; i++) {
        bytes[i] = binString.charCodeAt(i);
    }
    return bytes;
}
exports.decodeBase64 = decodeBase64;
const base64abc = [
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
    "H",
    "I",
    "J",
    "K",
    "L",
    "M",
    "N",
    "O",
    "P",
    "Q",
    "R",
    "S",
    "T",
    "U",
    "V",
    "W",
    "X",
    "Y",
    "Z",
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "+",
    "/",
];
