"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createAuthFlow = void 0;
const cert_1 = require("./cert");
const md5_1 = require("./md5");
const password_1 = require("./password");
const scram_sha_256_1 = require("./sasl/scram-sha-256");
function createAuthFlow(options) {
    switch (options.auth.method) {
        case 'password':
            return new password_1.PasswordAuthFlow(Object.assign(Object.assign({}, options), { auth: options.auth }));
        case 'md5':
            return new md5_1.Md5AuthFlow(Object.assign(Object.assign({}, options), { auth: options.auth }));
        case 'scram-sha-256':
            return new scram_sha_256_1.ScramSha256AuthFlow(Object.assign(Object.assign({}, options), { auth: options.auth }));
        case 'cert':
            return new cert_1.CertAuthFlow(Object.assign(Object.assign({}, options), { auth: options.auth }));
        default:
            throw new Error(`Unsupported auth method: ${options.auth.method}`);
    }
}
exports.createAuthFlow = createAuthFlow;
