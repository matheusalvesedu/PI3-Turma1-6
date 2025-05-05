"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SaslMechanism = void 0;
const message_codes_1 = require("../../message-codes");
const SaslMessageCode = {
    AuthenticationSASL: 10,
    AuthenticationSASLContinue: 11,
    AuthenticationSASLFinal: 12,
};
class SaslMechanism {
    constructor(params) {
        this.writer = params.writer;
    }
    createAuthenticationSASL() {
        const mechanisms = ['SCRAM-SHA-256'];
        this.writer.addInt32(SaslMessageCode.AuthenticationSASL);
        for (const mechanism of mechanisms) {
            this.writer.addCString(mechanism);
        }
        this.writer.addCString('');
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
    createAuthenticationSASLContinue(message) {
        this.writer.addInt32(SaslMessageCode.AuthenticationSASLContinue);
        this.writer.addString(message);
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
    createAuthenticationSASLFinal(message) {
        this.writer.addInt32(SaslMessageCode.AuthenticationSASLFinal);
        this.writer.addString(message);
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
}
exports.SaslMechanism = SaslMechanism;
