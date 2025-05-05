"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getEmulatorEnvs = exports.start = void 0;
const net_1 = require("net");
const clc = require("colorette");
const portUtils_1 = require("../portUtils");
const developmentServer_1 = require("./developmentServer");
const constants_1 = require("../constants");
const spawn_1 = require("../../init/spawn");
const developmentServer_2 = require("./developmentServer");
const types_1 = require("../types");
const config_1 = require("./config");
const projectPath_1 = require("../../projectPath");
const registry_1 = require("../registry");
const env_1 = require("../env");
const error_1 = require("../../error");
const secrets = require("../../gcp/secretManager");
const utils_1 = require("../../utils");
async function start(options) {
    var _a;
    const hostname = constants_1.DEFAULT_HOST;
    let port = (_a = options === null || options === void 0 ? void 0 : options.port) !== null && _a !== void 0 ? _a : constants_1.DEFAULT_PORTS.apphosting;
    while (!(await availablePort(hostname, port))) {
        port += 1;
    }
    await serve(options === null || options === void 0 ? void 0 : options.projectId, port, options === null || options === void 0 ? void 0 : options.startCommand, options === null || options === void 0 ? void 0 : options.rootDirectory);
    return { hostname, port };
}
exports.start = start;
const secretResourceRegex = /^projects\/([^/]+)\/secrets\/([^/]+)(?:\/versions\/((?:latest)|\d+))?$/;
const secretShorthandRegex = /^([^/@]+)(?:@((?:latest)|\d+))?$/;
async function loadSecret(project, name) {
    var _a, _b, _c, _d;
    let projectId;
    let secretId;
    let version;
    const match = secretResourceRegex.exec(name);
    if (match) {
        projectId = match[1];
        secretId = match[2];
        version = match[3] || "latest";
    }
    else {
        const match = secretShorthandRegex.exec(name);
        if (!match) {
            throw new error_1.FirebaseError(`Invalid secret name: ${name}`);
        }
        if (!project) {
            throw new error_1.FirebaseError(`Cannot load secret ${match[1]} without a project. ` +
                `Please use ${clc.bold("firebase use")} or pass the --project flag.`);
        }
        projectId = project;
        secretId = match[1];
        version = match[2] || "latest";
    }
    try {
        return await secrets.accessSecretVersion(projectId, secretId, version);
    }
    catch (err) {
        if (((_a = err === null || err === void 0 ? void 0 : err.original) === null || _a === void 0 ? void 0 : _a.code) === 403 || ((_d = (_c = (_b = err === null || err === void 0 ? void 0 : err.original) === null || _b === void 0 ? void 0 : _b.context) === null || _c === void 0 ? void 0 : _c.response) === null || _d === void 0 ? void 0 : _d.statusCode) === 403) {
            (0, utils_1.logLabeledError)(types_1.Emulators.APPHOSTING, `Permission denied to access secret ${secretId}. Use ` +
                `${clc.bold("firebase apphosting:secrets:grantaccess")} to get permissions.`);
        }
        throw err;
    }
}
async function serve(projectId, port, startCommand, backendRelativeDir) {
    backendRelativeDir = backendRelativeDir !== null && backendRelativeDir !== void 0 ? backendRelativeDir : "./";
    const backendRoot = (0, projectPath_1.resolveProjectPath)({}, backendRelativeDir);
    const apphostingLocalConfig = await (0, config_1.getLocalAppHostingConfiguration)(backendRoot);
    const resolveEnv = Object.entries(apphostingLocalConfig.env).map(async ([key, value]) => [
        key,
        value.value ? value.value : await loadSecret(projectId, value.secret),
    ]);
    const environmentVariablesToInject = Object.assign(Object.assign(Object.assign({}, getEmulatorEnvs()), Object.fromEntries(await Promise.all(resolveEnv))), { PORT: port.toString() });
    if (startCommand) {
        developmentServer_2.logger.logLabeled("BULLET", types_1.Emulators.APPHOSTING, `running custom start command: '${startCommand}'`);
        (0, spawn_1.spawnWithCommandString)(startCommand, backendRoot, environmentVariablesToInject)
            .catch((err) => {
            developmentServer_2.logger.logLabeled("ERROR", types_1.Emulators.APPHOSTING, `failed to start Dev Server: ${err}`);
        })
            .then(() => developmentServer_2.logger.logLabeled("BULLET", types_1.Emulators.APPHOSTING, `Dev Server stopped`));
        return;
    }
    const detectedStartCommand = await (0, developmentServer_1.detectStartCommand)(backendRoot);
    developmentServer_2.logger.logLabeled("BULLET", types_1.Emulators.APPHOSTING, `starting app with: '${detectedStartCommand}'`);
    (0, spawn_1.spawnWithCommandString)(detectedStartCommand, backendRoot, environmentVariablesToInject)
        .catch((err) => {
        developmentServer_2.logger.logLabeled("ERROR", types_1.Emulators.APPHOSTING, `failed to start Dev Server: ${err}`);
    })
        .then(() => developmentServer_2.logger.logLabeled("BULLET", types_1.Emulators.APPHOSTING, `Dev Server stopped`));
}
function availablePort(host, port) {
    return (0, portUtils_1.checkListenable)({
        address: host,
        port,
        family: (0, net_1.isIPv4)(host) ? "IPv4" : "IPv6",
    });
}
function getEmulatorEnvs() {
    const envs = {};
    const emulatorInfos = registry_1.EmulatorRegistry.listRunningWithInfo().filter((emulator) => emulator.name !== types_1.Emulators.APPHOSTING);
    (0, env_1.setEnvVarsForEmulators)(envs, emulatorInfos);
    return envs;
}
exports.getEmulatorEnvs = getEmulatorEnvs;
