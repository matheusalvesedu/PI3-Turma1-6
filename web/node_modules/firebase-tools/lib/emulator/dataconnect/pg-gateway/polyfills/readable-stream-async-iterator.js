"use strict";
var _a, _b;
var _c;
(_a = (_c = ReadableStream.prototype).values) !== null && _a !== void 0 ? _a : (_c.values = function ({ preventCancel = false } = {}) {
    const reader = this.getReader();
    return {
        async next() {
            try {
                const result = await reader.read();
                if (result.done) {
                    reader.releaseLock();
                }
                return result;
            }
            catch (e) {
                reader.releaseLock();
                throw e;
            }
        },
        async return(value) {
            if (!preventCancel) {
                const cancelPromise = reader.cancel(value);
                reader.releaseLock();
                await cancelPromise;
            }
            else {
                reader.releaseLock();
            }
            return { done: true, value };
        },
        [Symbol.asyncIterator]() {
            return this;
        },
    };
});
(_b = ReadableStream.prototype) !== null && _b !== void 0 ? _b : (ReadableStream.prototype = ReadableStream.prototype.values);
