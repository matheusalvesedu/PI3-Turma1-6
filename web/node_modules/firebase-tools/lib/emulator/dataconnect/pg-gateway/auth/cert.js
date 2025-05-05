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
exports.CertAuthFlow = void 0;
const backend_error_1 = require("../backend-error");
const base_auth_flow_1 = require("./base-auth-flow");
const connection_1 = require("../connection");
class CertAuthFlow extends base_auth_flow_1.BaseAuthFlow {
    constructor(params) {
        var _a;
        super(params);
        this.completed = false;
        this.auth = Object.assign(Object.assign({}, params.auth), { validateCredentials: (_a = params.auth.validateCredentials) !== null && _a !== void 0 ? _a : (async ({ username, certificate }) => {
                return certificate.subject.CN === username;
            }) });
        this.username = params.username;
    }
    handleClientMessage(message) {
        return __asyncGenerator(this, arguments, function* handleClientMessage_1() {
            if (false) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '08000',
                    message: `ssl connection required when auth mode is 'certificate'`,
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
            if (false) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '08000',
                    message: 'client certificate is invalid',
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
            const isValid = false;
            if (!isValid) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '08000',
                    message: 'client certificate is invalid',
                }));
                yield yield __await(connection_1.closeSignal);
                return yield __await(void 0);
            }
            this.completed = true;
        });
    }
    createInitialAuthMessage() {
        return undefined;
    }
    get isCompleted() {
        return this.completed;
    }
}
exports.CertAuthFlow = CertAuthFlow;
