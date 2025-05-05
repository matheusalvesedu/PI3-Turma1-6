"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BufferWriter = void 0;
const utils_1 = require("./utils");
class BufferWriter {
    constructor(size = 256) {
        this.size = size;
        this.offset = 5;
        this.headerPosition = 0;
        this.encoder = new TextEncoder();
        this.buffer = new Uint8Array(size);
    }
    ensure(size) {
        const remaining = this.buffer.length - this.offset;
        if (remaining < size) {
            const oldBuffer = this.buffer;
            const newSize = oldBuffer.length + (oldBuffer.length >> 1) + size;
            this.buffer = new Uint8Array(newSize);
            (0, utils_1.copy)(oldBuffer, this.buffer);
        }
    }
    addInt32(num) {
        this.ensure(4);
        this.buffer[this.offset++] = (num >>> 24) & 0xff;
        this.buffer[this.offset++] = (num >>> 16) & 0xff;
        this.buffer[this.offset++] = (num >>> 8) & 0xff;
        this.buffer[this.offset++] = (num >>> 0) & 0xff;
        return this;
    }
    addInt16(num) {
        this.ensure(2);
        this.buffer[this.offset++] = (num >>> 8) & 0xff;
        this.buffer[this.offset++] = (num >>> 0) & 0xff;
        return this;
    }
    addCString(string) {
        if (!string) {
            this.ensure(1);
        }
        else {
            const stringBuffer = this.encoder.encode(string);
            this.ensure(stringBuffer.byteLength + 1);
            this.buffer.set(stringBuffer, this.offset);
            this.offset += stringBuffer.byteLength;
        }
        this.buffer[this.offset++] = 0;
        return this;
    }
    addString(string = '') {
        const stringBuffer = this.encoder.encode(string);
        this.ensure(stringBuffer.byteLength);
        this.buffer.set(stringBuffer, this.offset);
        this.offset += stringBuffer.byteLength;
        return this;
    }
    add(otherBuffer) {
        this.ensure(otherBuffer.byteLength);
        (0, utils_1.copy)(otherBuffer, this.buffer, this.offset);
        this.offset += otherBuffer.length;
        return this;
    }
    join(code) {
        if (code) {
            this.buffer[this.headerPosition] = code;
            const length = this.offset - (this.headerPosition + 1);
            const dataView = new DataView(this.buffer.buffer, this.buffer.byteOffset, this.buffer.byteLength);
            dataView.setInt32(this.headerPosition + 1, length);
        }
        return this.buffer.slice(code ? 0 : 5, this.offset);
    }
    flush(code) {
        const result = this.join(code);
        this.offset = 5;
        this.headerPosition = 0;
        this.buffer = new Uint8Array(this.size);
        return result;
    }
}
exports.BufferWriter = BufferWriter;
