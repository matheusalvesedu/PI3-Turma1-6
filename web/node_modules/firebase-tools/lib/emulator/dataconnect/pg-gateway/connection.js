"use strict";
var __asyncValues = (this && this.__asyncValues) || function (o) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var m = o[Symbol.asyncIterator], i;
    return m ? m.call(o) : (o = typeof __values === "function" ? __values(o) : o[Symbol.iterator](), i = {}, verb("next"), verb("throw"), verb("return"), i[Symbol.asyncIterator] = function () { return this; }, i);
    function verb(n) { i[n] = o[n] && function (v) { return new Promise(function (resolve, reject) { v = o[n](v), settle(resolve, reject, v.done, v.value); }); }; }
    function settle(resolve, reject, d, v) { Promise.resolve(v).then(function(v) { resolve({ value: v, done: d }); }, reject); }
};
var __await = (this && this.__await) || function (v) { return this instanceof __await ? (this.v = v, this) : new __await(v); }
var __asyncDelegator = (this && this.__asyncDelegator) || function (o) {
    var i, p;
    return i = {}, verb("next"), verb("throw", function (e) { throw e; }), verb("return"), i[Symbol.iterator] = function () { return this; }, i;
    function verb(n, f) { i[n] = o[n] ? function (v) { return (p = !p) ? { value: __await(o[n](v)), done: n === "return" } : f ? f(v) : v; } : f; }
};
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
exports.closeSignal = void 0;
const index_1 = require("./auth/index");
const backend_error_1 = require("./backend-error");
const buffer_reader_1 = require("./buffer-reader");
const buffer_writer_1 = require("./buffer-writer");
const connection_types_1 = require("./connection.types");
const utils_1 = require("./utils");
const message_buffer_1 = require("./message-buffer");
const message_codes_1 = require("./message-codes");
const logger_1 = require("../../../logger");
exports.closeSignal = Symbol('close');
class PostgresConnection {
    constructor(duplex, options = {}) {
        this.duplex = duplex;
        this.step = connection_types_1.ServerStep.AwaitingInitialMessage;
        this.hasStarted = false;
        this.isAuthenticated = false;
        this.detached = false;
        this.writer = new buffer_writer_1.BufferWriter();
        this.reader = new buffer_reader_1.BufferReader();
        this.messageBuffer = new message_buffer_1.MessageBuffer();
        this.options = Object.assign({ auth: { method: 'trust' } }, options);
        if (this.options.tls && !this.options.upgradeTls) {
            throw new Error('TLS options are only available when upgradeTls() is implemented. Did you mean to use fromNodeSocket()?');
        }
        this.processData();
    }
    get state() {
        return {
            hasStarted: this.hasStarted,
            isAuthenticated: this.isAuthenticated,
            clientInfo: this.clientInfo,
            tlsInfo: this.tlsInfo,
            step: this.step,
        };
    }
    async detach() {
        this.detached = true;
        return this.duplex;
    }
    async processData() {
        var _a, e_1, _b, _c, _d, e_2, _e, _f, _g, e_3, _h, _j, _k, e_4, _l, _m;
        const writer = this.duplex.writable.getWriter();
        try {
            for (var _o = true, _p = __asyncValues(this.duplex.readable), _q; _q = await _p.next(), _a = _q.done, !_a;) {
                _c = _q.value;
                _o = false;
                try {
                    const data = _c;
                    this.messageBuffer.mergeBuffer(data);
                    try {
                        for (var _r = true, _s = (e_2 = void 0, __asyncValues(this.messageBuffer.processMessages(this.hasStarted))), _t; _t = await _s.next(), _d = _t.done, !_d;) {
                            _f = _t.value;
                            _r = false;
                            try {
                                const clientMessage = _f;
                                logger_1.logger.debug('Frontend message', (0, message_codes_1.getFrontendMessageName)(clientMessage[0]));
                                try {
                                    for (var _u = true, _v = (e_3 = void 0, __asyncValues(this.handleClientMessage(clientMessage))), _w; _w = await _v.next(), _g = _w.done, !_g;) {
                                        _j = _w.value;
                                        _u = false;
                                        try {
                                            const responseMessage = _j;
                                            if (responseMessage === exports.closeSignal) {
                                                await writer.close();
                                                return;
                                            }
                                            try {
                                                for (var _x = true, _y = (e_4 = void 0, __asyncValues((0, message_buffer_1.getMessages)(responseMessage))), _z; _z = await _y.next(), _k = _z.done, !_k;) {
                                                    _m = _z.value;
                                                    _x = false;
                                                    try {
                                                        const msg = _m;
                                                        if (msg[0] !== message_codes_1.BackendMessageCode.NoticeMessage) {
                                                            logger_1.logger.debug('Backend message', (0, message_codes_1.getBackendMessageName)(msg[0]));
                                                            if (msg[0] === message_codes_1.BackendMessageCode.ErrorMessage) {
                                                                logger_1.logger.debug(new TextDecoder().decode(msg));
                                                            }
                                                        }
                                                    }
                                                    finally {
                                                        _x = true;
                                                    }
                                                }
                                            }
                                            catch (e_4_1) { e_4 = { error: e_4_1 }; }
                                            finally {
                                                try {
                                                    if (!_x && !_k && (_l = _y.return)) await _l.call(_y);
                                                }
                                                finally { if (e_4) throw e_4.error; }
                                            }
                                            await writer.write(responseMessage);
                                        }
                                        finally {
                                            _u = true;
                                        }
                                    }
                                }
                                catch (e_3_1) { e_3 = { error: e_3_1 }; }
                                finally {
                                    try {
                                        if (!_u && !_g && (_h = _v.return)) await _h.call(_v);
                                    }
                                    finally { if (e_3) throw e_3.error; }
                                }
                            }
                            finally {
                                _r = true;
                            }
                        }
                    }
                    catch (e_2_1) { e_2 = { error: e_2_1 }; }
                    finally {
                        try {
                            if (!_r && !_d && (_e = _s.return)) await _e.call(_s);
                        }
                        finally { if (e_2) throw e_2.error; }
                    }
                    if (this.detached) {
                        return;
                    }
                }
                finally {
                    _o = true;
                }
            }
        }
        catch (e_1_1) { e_1 = { error: e_1_1 }; }
        finally {
            try {
                if (!_o && !_a && (_b = _p.return)) await _b.call(_p);
            }
            finally { if (e_1) throw e_1.error; }
        }
    }
    handleClientMessage(message) {
        var _a, _b;
        return __asyncGenerator(this, arguments, function* handleClientMessage_1() {
            this.reader.setBuffer(message);
            const messageResponse = yield __await(((_b = (_a = this.options).onMessage) === null || _b === void 0 ? void 0 : _b.call(_a, message, this.state)));
            let skipProcessing = messageResponse !== undefined;
            if (messageResponse) {
                const iterableResponse = new utils_1.AsyncIterableWithMetadata(messageResponse instanceof Uint8Array ? [messageResponse] : messageResponse);
                yield __await(yield* __asyncDelegator(__asyncValues(iterableResponse)));
                if (iterableResponse.returnValue instanceof Uint8Array) {
                    yield yield __await(iterableResponse.returnValue);
                }
                skipProcessing =
                    iterableResponse.iterations > 0 || iterableResponse.returnValue !== undefined;
            }
            if (this.detached) {
                return yield __await(void 0);
            }
            if (skipProcessing) {
                if (this.isStartupMessage(message)) {
                    this.hasStarted = true;
                }
                return yield __await(void 0);
            }
            switch (this.step) {
                case connection_types_1.ServerStep.AwaitingInitialMessage:
                    if (this.isSslRequest(message)) {
                        yield __await(yield* __asyncDelegator(__asyncValues(this.handleSslRequest())));
                    }
                    else if (this.isStartupMessage(message)) {
                        if (this.options.tls && !this.tlsInfo) {
                            yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                                severity: 'FATAL',
                                code: '08P01',
                                message: 'SSL connection is required',
                            }));
                            yield yield __await(exports.closeSignal);
                            return yield __await(void 0);
                        }
                        yield __await(yield* __asyncDelegator(__asyncValues(this.handleStartupMessage(message))));
                    }
                    else {
                        throw new Error('Unexpected initial message');
                    }
                    break;
                case connection_types_1.ServerStep.PerformingAuthentication: {
                    const authenticationComplete = yield __await(yield* __asyncDelegator(__asyncValues(this.handleAuthenticationMessage(message))));
                    if (authenticationComplete) {
                        yield __await(yield* __asyncDelegator(__asyncValues(this.completeAuthentication())));
                    }
                    break;
                }
                case connection_types_1.ServerStep.ReadyForQuery:
                    yield __await(yield* __asyncDelegator(__asyncValues(this.handleRegularMessage(message))));
                    break;
                default:
                    throw new Error(`Unexpected step: ${this.step}`);
            }
        });
    }
    handleSslRequest() {
        var _a, _b;
        return __asyncGenerator(this, arguments, function* handleSslRequest_1() {
            if (!this.options.tls || !this.options.upgradeTls) {
                this.writer.addString('N');
                yield yield __await(this.writer.flush());
                return yield __await(void 0);
            }
            this.writer.addString('S');
            yield yield __await(this.writer.flush());
            const requestCert = this.options.auth.method === 'cert';
            const { duplex, tlsInfo } = yield __await(this.options.upgradeTls(this.duplex, this.options.tls, this.tlsInfo, requestCert));
            this.duplex = duplex;
            this.tlsInfo = tlsInfo;
            yield __await(((_b = (_a = this.options).onTlsUpgrade) === null || _b === void 0 ? void 0 : _b.call(_a, this.state)));
        });
    }
    handleStartupMessage(message) {
        var _a, _b;
        return __asyncGenerator(this, arguments, function* handleStartupMessage_1() {
            const { majorVersion, minorVersion, parameters } = this.readStartupMessage();
            if (!parameters.user) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '08000',
                    message: 'user is required',
                }));
                yield yield __await(exports.closeSignal);
                return yield __await(void 0);
            }
            if (majorVersion !== 3 || minorVersion !== 0) {
                yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                    severity: 'FATAL',
                    code: '08000',
                    message: `Unsupported protocol version ${majorVersion.toString()}.${minorVersion.toString()}`,
                }));
                yield yield __await(exports.closeSignal);
                return yield __await(void 0);
            }
            this.clientInfo = {
                majorVersion,
                minorVersion,
                parameters: Object.assign({ user: parameters.user }, parameters),
            };
            this.hasStarted = true;
            yield __await(((_b = (_a = this.options).onStartup) === null || _b === void 0 ? void 0 : _b.call(_a, this.state)));
            if (this.detached) {
                return yield __await(void 0);
            }
            if (this.options.auth.method === 'trust') {
                yield __await(yield* __asyncDelegator(__asyncValues(this.completeAuthentication())));
                return yield __await(void 0);
            }
            this.authFlow = (0, index_1.createAuthFlow)({
                reader: this.reader,
                writer: this.writer,
                username: this.clientInfo.parameters.user,
                auth: this.options.auth,
                connectionState: this.state,
            });
            this.step = connection_types_1.ServerStep.PerformingAuthentication;
            const initialAuthMessage = this.authFlow.createInitialAuthMessage();
            if (initialAuthMessage) {
                yield yield __await(initialAuthMessage);
            }
            if (this.options.auth.method === 'cert') {
                yield __await(yield* __asyncDelegator(__asyncValues(this.authFlow.handleClientMessage(message))));
                if (this.authFlow.isCompleted) {
                    yield __await(yield* __asyncDelegator(__asyncValues(this.completeAuthentication())));
                }
            }
        });
    }
    handleAuthenticationMessage(message) {
        return __asyncGenerator(this, arguments, function* handleAuthenticationMessage_1() {
            const code = this.reader.byte();
            if (code !== message_codes_1.FrontendMessageCode.Password) {
                throw new Error(`Unexpected authentication message code: ${code}`);
            }
            if (!this.authFlow) {
                throw new Error('AuthFlow not initialized');
            }
            yield __await(yield* __asyncDelegator(__asyncValues(this.authFlow.handleClientMessage(message))));
            return yield __await(this.authFlow.isCompleted);
        });
    }
    handleRegularMessage(message) {
        return __asyncGenerator(this, arguments, function* handleRegularMessage_1() {
            const code = this.reader.byte();
            switch (code) {
                case message_codes_1.FrontendMessageCode.Terminate:
                    yield yield __await(exports.closeSignal);
                    return yield __await(void 0);
                default:
                    yield yield __await((0, backend_error_1.createBackendErrorMessage)({
                        severity: 'ERROR',
                        code: '123',
                        message: 'Message code not yet implemented',
                    }));
                    yield yield __await(this.createReadyForQuery());
            }
        });
    }
    isSslRequest(message) {
        if (message.byteLength !== 8)
            return false;
        const dataView = new DataView(message.buffer, message.byteOffset, message.byteLength);
        const mostSignificantPart = dataView.getInt16(4);
        const leastSignificantPart = dataView.getInt16(6);
        return mostSignificantPart === 1234 && leastSignificantPart === 5679;
    }
    isStartupMessage(message) {
        if (message.byteLength < 8)
            return false;
        const dataView = new DataView(message.buffer, message.byteOffset, message.byteLength);
        const length = dataView.getInt32(0);
        const majorVersion = dataView.getInt16(4);
        const minorVersion = dataView.getInt16(6);
        return message.byteLength === length && majorVersion === 3 && minorVersion === 0;
    }
    completeAuthentication() {
        var _a, _b;
        return __asyncGenerator(this, arguments, function* completeAuthentication_1() {
            this.isAuthenticated = true;
            yield yield __await(this.createAuthenticationOk());
            yield __await(((_b = (_a = this.options).onAuthenticated) === null || _b === void 0 ? void 0 : _b.call(_a, this.state)));
            if (this.options.serverVersion) {
                let serverVersion;
                if (typeof this.options.serverVersion === 'function') {
                    serverVersion = yield __await(this.options.serverVersion(this.state));
                }
                else {
                    serverVersion = this.options.serverVersion;
                }
                yield yield __await(this.createParameterStatus('server_version', serverVersion));
            }
            this.step = connection_types_1.ServerStep.ReadyForQuery;
            yield yield __await(this.createReadyForQuery());
        });
    }
    readStartupMessage() {
        const length = this.reader.int32();
        const majorVersion = this.reader.int16();
        const minorVersion = this.reader.int16();
        const parameters = {};
        for (let key; (key = this.reader.cstring()) !== '';) {
            parameters[key] = this.reader.cstring();
        }
        return {
            majorVersion,
            minorVersion,
            parameters,
        };
    }
    readQuery() {
        const query = this.reader.cstring();
        return {
            query,
        };
    }
    createAuthenticationOk() {
        this.writer.addInt32(0);
        return this.writer.flush(message_codes_1.BackendMessageCode.AuthenticationResponse);
    }
    createParameterStatus(name, value) {
        this.writer.addCString(name);
        this.writer.addCString(value);
        return this.writer.flush(message_codes_1.BackendMessageCode.ParameterStatus);
    }
    createReadyForQuery(transactionStatus = 'idle') {
        switch (transactionStatus) {
            case 'idle':
                this.writer.addString('I');
                break;
            case 'transaction':
                this.writer.addString('T');
                break;
            case 'error':
                this.writer.addString('E');
                break;
            default:
                throw new Error(`Unknown transaction status '${transactionStatus}'`);
        }
        return this.writer.flush(message_codes_1.BackendMessageCode.ReadyForQuery);
    }
    createAuthenticationFailedError() {
        var _a;
        return (0, backend_error_1.createBackendErrorMessage)({
            severity: 'FATAL',
            code: '28P01',
            message: ((_a = this.clientInfo) === null || _a === void 0 ? void 0 : _a.parameters.user)
                ? `password authentication failed for user "${this.clientInfo.parameters.user}"`
                : 'password authentication failed',
        });
    }
}
exports.default = PostgresConnection;
