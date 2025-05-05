"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createVirtualServer = exports.createDuplexPair = exports.BufferedStream = void 0;
const web_1 = require("node:stream/web");
class BufferedStream {
    constructor() {
        const buffer = [];
        this.readable = new web_1.ReadableStream({
            async pull(controller) {
                while (buffer.length === 0) {
                    await new Promise((resolve) => setTimeout(resolve));
                }
                const chunk = buffer.shift();
                controller.enqueue(chunk);
            },
        });
        this.writable = new WritableStream({
            async write(chunk) {
                buffer.push(chunk);
                await new Promise((resolve) => setTimeout(resolve));
            },
        });
    }
}
exports.BufferedStream = BufferedStream;
function createDuplexPair() {
    const aToB = new BufferedStream();
    const bToA = new BufferedStream();
    const duplexA = {
        readable: bToA.readable,
        writable: aToB.writable,
    };
    const duplexB = {
        readable: aToB.readable,
        writable: bToA.writable,
    };
    return [duplexA, duplexB];
}
exports.createDuplexPair = createDuplexPair;
function createVirtualServer() {
    const listener = new BufferedStream();
    const connWriter = listener.writable.getWriter();
    function listen() {
        return listener.readable;
    }
    async function connect() {
        const [clientConn, serverConn] = createDuplexPair();
        await connWriter.write(serverConn);
        return clientConn;
    }
    return { listen, connect };
}
exports.createVirtualServer = createVirtualServer;
