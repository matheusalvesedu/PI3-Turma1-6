"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getBackendMessageName = exports.getFrontendMessageName = exports.BackendMessageCode = exports.FrontendMessageCode = void 0;
exports.FrontendMessageCode = {
    Query: 0x51,
    Parse: 0x50,
    Bind: 0x42,
    Execute: 0x45,
    FunctionCall: 0x46,
    Flush: 0x48,
    Close: 0x43,
    Describe: 0x44,
    CopyFromChunk: 0x64,
    CopyDone: 0x63,
    CopyData: 0x64,
    CopyFail: 0x66,
    Password: 0x70,
    Sync: 0x53,
    Terminate: 0x58,
};
exports.BackendMessageCode = {
    DataRow: 0x44,
    ParseComplete: 0x31,
    BindComplete: 0x32,
    CloseComplete: 0x33,
    CommandComplete: 0x43,
    ReadyForQuery: 0x5a,
    NoData: 0x6e,
    NotificationResponse: 0x41,
    AuthenticationResponse: 0x52,
    ParameterStatus: 0x53,
    BackendKeyData: 0x4b,
    ErrorMessage: 0x45,
    NoticeMessage: 0x4e,
    RowDescriptionMessage: 0x54,
    ParameterDescriptionMessage: 0x74,
    PortalSuspended: 0x73,
    ReplicationStart: 0x57,
    EmptyQuery: 0x49,
    CopyIn: 0x47,
    CopyOut: 0x48,
    CopyDone: 0x63,
    CopyData: 0x64,
};
function getFrontendMessageName(code) {
    var _a;
    return (_a = Object.entries(exports.FrontendMessageCode).find(([_, value]) => value === code)) === null || _a === void 0 ? void 0 : _a[0];
}
exports.getFrontendMessageName = getFrontendMessageName;
function getBackendMessageName(code) {
    var _a;
    return (_a = Object.entries(exports.BackendMessageCode).find(([_, value]) => value === code)) === null || _a === void 0 ? void 0 : _a[0];
}
exports.getBackendMessageName = getBackendMessageName;
