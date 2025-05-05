"use strict";
var __await = (this && this.__await) || function (v) { return this instanceof __await ? (this.v = v, this) : new __await(v); }
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
exports.validateBinaryLike = exports.encodeHex = exports.concat = exports.createPreHashedPassword = exports.generateMd5Salt = exports.md5 = exports.hashPreHashedPassword = exports.Md5AuthFlow = void 0;
const backend_error_1 = require("../backend-error");
const connection_1 = require("../connection");
const message_codes_1 = require("../message-codes");
const base_auth_flow_1 = require("./base-auth-flow");
class Md5AuthFlow extends base_auth_flow_1.BaseAuthFlow {
    constructor(params) {
        var _a;
        super(params);
        this.completed = false;
        this.auth = Object.assign(Object.assign({}, params.auth), { validateCredentials: (_a = params.auth.validateCredentials) !== null && _a !== void 0 ? _a : (async ({ preHashedPassword, hashedPassword, salt }) => {
                const expectedHashedPassword = await hashPreHashedPassword(preHashedPassword, salt);
                return hashedPassword === expectedHashedPassword;
            }) });
        this.username = params.username;
        this.salt = generateMd5Salt();
    }
    handleClientMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientMessage_1() {
            const length = this.reader.int32();
            const hashedPassword = this.reader.cstring();
            const preHashedPassword = yield __await(this.auth.getPreHashedPassword({
                username: this.username,
            }, this.connectionState));
            const isValid = yield __await(this.auth.validateCredentials({
                username: this.username,
                hashedPassword,
                preHashedPassword,
                salt: this.salt,
            }, this.connectionState));
            if (!isValid) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '28P01',
                    message: `password authentication failed for user "${this.username}"`,
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
            this.completed = true;
        });
    }
    createInitialAuthMessage() {
        return this.createAuthenticationMD5Password();
    }
    get isCompleted() {
        return this.completed;
    }
    createAuthenticationMD5Password() {
        this.writer.addInt32(5);
        this.writer.add(Buffer.from(this.salt));
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
}
exports.Md5AuthFlow = Md5AuthFlow;
async function hashPreHashedPassword(preHashedPassword, salt) {
    const hash = await md5(concat([
        new TextEncoder().encode(preHashedPassword),
        salt instanceof ArrayBuffer
            ? new Uint8Array(salt)
            : new Uint8Array(salt.buffer, salt.byteOffset, salt.byteLength),
    ]));
    return `md5${hash}`;
}
exports.hashPreHashedPassword = hashPreHashedPassword;
async function md5(value) {
    const hash = await crypto.subtle.digest('MD5', typeof value === 'string' ? new TextEncoder().encode(value) : value);
    return encodeHex(hash);
}
exports.md5 = md5;
function generateMd5Salt() {
    const salt = new Uint8Array(4);
    crypto.getRandomValues(salt);
    return salt;
}
exports.generateMd5Salt = generateMd5Salt;
async function createPreHashedPassword(username, password) {
    return await md5(`${password}${username}`);
}
exports.createPreHashedPassword = createPreHashedPassword;
function concat(buffers) {
    let length = 0;
    for (const buffer of buffers) {
        length += buffer.length;
    }
    const output = new Uint8Array(length);
    let index = 0;
    for (const buffer of buffers) {
        output.set(buffer, index);
        index += buffer.length;
    }
    return output;
}
exports.concat = concat;
const hexTable = new TextEncoder().encode("0123456789abcdef");
const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();
function encodeHex(src) {
    const u8 = validateBinaryLike(src);
    const dst = new Uint8Array(u8.length * 2);
    for (let i = 0; i < u8.length; i++) {
        const v = u8[i];
        dst[i * 2] = hexTable[v >> 4];
        dst[i * 2 + 1] = hexTable[v & 0x0f];
    }
    return textDecoder.decode(dst);
}
exports.encodeHex = encodeHex;
function validateBinaryLike(source) {
    if (typeof source === "string") {
        return textEncoder.encode(source);
    }
    else if (source instanceof Uint8Array) {
        return source;
    }
    else if (source instanceof ArrayBuffer) {
        return new Uint8Array(source);
    }
    throw new TypeError(`Cannot validate the input as it must be a Uint8Array, a string, or an ArrayBuffer`);
}
exports.validateBinaryLike = validateBinaryLike;
