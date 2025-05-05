"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.fromNodeSocket = void 0;
const node_stream_1 = require("node:stream");
const connection_1 = require("../../connection");
async function fromNodeSocket(socket, options) {
    const rs = node_stream_1.Readable.toWeb(socket);
    const ws = node_stream_1.Writable.toWeb(socket);
    const opts = options
        ? Object.assign({}, options) : undefined;
    return new connection_1.default({ readable: rs, writable: ws }, opts);
}
exports.fromNodeSocket = fromNodeSocket;
