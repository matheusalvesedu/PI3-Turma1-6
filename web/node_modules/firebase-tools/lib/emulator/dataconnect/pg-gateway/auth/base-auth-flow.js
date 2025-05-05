"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BaseAuthFlow = void 0;
class BaseAuthFlow {
    constructor(params) {
        this.reader = params.reader;
        this.writer = params.writer;
        this.connectionState = params.connectionState;
    }
}
exports.BaseAuthFlow = BaseAuthFlow;
