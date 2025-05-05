"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BufferReader = void 0;
class BufferReader {
    constructor(offset = 0) {
        this.offset = offset;
        this.buffer = new Uint8Array();
        this.decoder = new TextDecoder();
    }
    setBuffer(buffer, offset = 0) {
        this.buffer = buffer;
        this.offset = offset;
    }
    int16() {
        const dataView = new DataView(this.buffer.buffer, this.buffer.byteOffset, this.buffer.byteLength);
        const result = dataView.getInt16(this.offset);
        this.offset += 2;
        return result;
    }
    byte() {
        const dataView = new DataView(this.buffer.buffer, this.buffer.byteOffset, this.buffer.byteLength);
        const result = dataView.getUint8(this.offset);
        this.offset++;
        return result;
    }
    int32() {
        const dataView = new DataView(this.buffer.buffer, this.buffer.byteOffset, this.buffer.byteLength);
        const result = dataView.getInt32(this.offset);
        this.offset += 4;
        return result;
    }
    string(length) {
        const dataView = new DataView(this.buffer.buffer, this.offset, length);
        this.offset += length;
        return this.decoder.decode(dataView);
    }
    cstring() {
        var _a;
        const start = this.offset;
        let end = start;
        while (((_a = this.buffer[end++]) !== null && _a !== void 0 ? _a : 0) !== 0) { }
        if (this.buffer[end - 1] === undefined) {
            throw new Error('Reached end of buffer before null character found for PG String');
        }
        const dataView = new DataView(this.buffer.buffer, start, end - start - 1);
        this.offset = end;
        return this.decoder.decode(dataView);
    }
    bytes(length) {
        const result = this.buffer.slice(this.offset, this.offset + length);
        this.offset += length;
        return result;
    }
}
exports.BufferReader = BufferReader;
