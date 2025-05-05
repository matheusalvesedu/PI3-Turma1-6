"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.copy = exports.AsyncIterableWithMetadata = void 0;
class AsyncIterableWithMetadata {
    constructor(iterable) {
        this.iterable = iterable;
        this.returnValue = undefined;
        this.iterations = 0;
    }
    [Symbol.asyncIterator]() {
        const asyncIterator = Symbol.asyncIterator in this.iterable
            ? this.iterable[Symbol.asyncIterator]()
            : this.iterable[Symbol.iterator]();
        this.returnValue = undefined;
        this.iterations = 0;
        return {
            next: async () => {
                const result = await asyncIterator.next();
                if (result.done) {
                    this.returnValue = result.value;
                }
                else {
                    this.iterations++;
                }
                return result;
            },
        };
    }
}
exports.AsyncIterableWithMetadata = AsyncIterableWithMetadata;
function copy(src, dst, offset = 0) {
    offset = Math.max(0, Math.min(offset, dst.byteLength));
    const dstBytesAvailable = dst.byteLength - offset;
    if (src.byteLength > dstBytesAvailable) {
        src = src.subarray(0, dstBytesAvailable);
    }
    dst.set(src, offset);
    return src.byteLength;
}
exports.copy = copy;
