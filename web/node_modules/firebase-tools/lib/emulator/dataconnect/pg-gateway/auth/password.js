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
exports.PasswordAuthFlow = void 0;
const backend_error_1 = require("../backend-error");
const connection_1 = require("../connection");
const message_codes_1 = require("../message-codes");
const base_auth_flow_1 = require("./base-auth-flow");
class PasswordAuthFlow extends base_auth_flow_1.BaseAuthFlow {
    constructor(params) {
        var _a;
        super(params);
        this.completed = false;
        this.auth = Object.assign(Object.assign({}, params.auth), { validateCredentials: (_a = params.auth.validateCredentials) !== null && _a !== void 0 ? _a : (async ({ password, clearTextPassword }) => {
                return password === clearTextPassword;
            }) });
        this.username = params.username;
    }
    handleClientMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientMessage_1() {
            const length = this.reader.int32();
            const password = this.reader.cstring();
            const clearTextPassword = yield __await(this.auth.getClearTextPassword({
                username: this.username,
            }, this.connectionState));
            const isValid = yield __await(this.auth.validateCredentials({
                username: this.username,
                password,
                clearTextPassword,
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
        return this.createAuthenticationCleartextPassword();
    }
    get isCompleted() {
        return this.completed;
    }
    createAuthenticationCleartextPassword() {
        this.writer.addInt32(3);
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
}
exports.PasswordAuthFlow = PasswordAuthFlow;
