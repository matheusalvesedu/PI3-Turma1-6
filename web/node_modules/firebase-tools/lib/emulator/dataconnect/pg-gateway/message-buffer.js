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
exports.getMessages = exports.MessageBuffer = void 0;
const utils_1 = require("./utils");
class MessageBuffer {
    constructor() {
        this.buffer = new Uint8Array();
        this.bufferLength = 0;
        this.bufferOffset = 0;
    }
    mergeBuffer(newData) {
        if (this.bufferLength > 0) {
            const newLength = this.bufferLength + newData.byteLength;
            const newFullLength = newLength + this.bufferOffset;
            if (newFullLength > this.buffer.byteLength) {
                let newBuffer;
                if (newLength <= this.buffer.byteLength && this.bufferOffset >= this.bufferLength) {
                    newBuffer = this.buffer;
                }
                else {
                    let newBufferLength = this.buffer.byteLength * 2;
                    while (newLength >= newBufferLength) {
                        newBufferLength *= 2;
                    }
                    newBuffer = new Uint8Array(newBufferLength);
                }
                const bufferView = this.buffer.subarray(this.bufferOffset, this.bufferOffset + this.bufferLength);
                (0, utils_1.copy)(bufferView, newBuffer, 0);
                this.buffer = newBuffer;
                this.bufferOffset = 0;
            }
            (0, utils_1.copy)(newData, this.buffer, this.bufferOffset + this.bufferLength);
            this.bufferLength = newLength;
        }
        else {
            this.buffer = newData;
            this.bufferOffset = 0;
            this.bufferLength = newData.byteLength;
        }
    }
    processMessages(hasStarted) {
        return __asyncGenerator(this, arguments, function* processMessages_1() {
            const bufferFullLength = this.bufferOffset + this.bufferLength;
            let offset = this.bufferOffset;
            const codeLength = !hasStarted ? 0 : 1;
            const headerLength = 4 + codeLength;
            while (offset + headerLength <= bufferFullLength) {
                const dataView = new DataView(this.buffer.buffer);
                const length = dataView.getUint32(offset + codeLength);
                const fullMessageLength = codeLength + length;
                if (offset + fullMessageLength <= bufferFullLength) {
                    yield yield __await(this.buffer.subarray(offset, offset + fullMessageLength));
                    offset += fullMessageLength;
                }
                else {
                    break;
                }
            }
            if (offset === bufferFullLength) {
                this.buffer = new Uint8Array();
                this.bufferLength = 0;
                this.bufferOffset = 0;
            }
            else {
                this.bufferLength = bufferFullLength - offset;
                this.bufferOffset = offset;
            }
        });
    }
}
exports.MessageBuffer = MessageBuffer;
function* getMessages(data) {
    const dataView = new DataView(data.buffer, data.byteOffset, data.byteLength);
    let offset = 0;
    if (dataView.byteLength === 0) {
        return;
    }
    while (offset < dataView.byteLength) {
        const length = dataView.getUint32(offset + 1);
        yield data.subarray(offset, offset + length + 1);
        offset += length + 1;
    }
}
exports.getMessages = getMessages;
