"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getFrameworksFromPackageJson = exports.frameworksMap = exports.SUPPORTED_FRAMEWORKS = exports.resolvePackageJson = exports.getPlatformFromFolder = exports.pickService = exports.readGQLFiles = exports.readConnectorYaml = exports.readDataConnectYaml = exports.readFirebaseJson = void 0;
const fs = require("fs-extra");
const path = require("path");
const error_1 = require("../error");
const types_1 = require("./types");
const utils_1 = require("../utils");
const load_1 = require("./load");
function readFirebaseJson(config) {
    if (!(config === null || config === void 0 ? void 0 : config.has("dataconnect"))) {
        return [];
    }
    const validator = (cfg) => {
        if (!cfg["source"]) {
            throw new error_1.FirebaseError("Invalid firebase.json: DataConnect requires `source`");
        }
        return {
            source: cfg["source"],
        };
    };
    const configs = config.get("dataconnect");
    if (typeof configs === "object" && !Array.isArray(configs)) {
        return [validator(configs)];
    }
    else if (Array.isArray(configs)) {
        return configs.map(validator);
    }
    else {
        throw new error_1.FirebaseError("Invalid firebase.json: dataconnect should be of the form { source: string }");
    }
}
exports.readFirebaseJson = readFirebaseJson;
async function readDataConnectYaml(sourceDirectory) {
    const file = await (0, utils_1.readFileFromDirectory)(sourceDirectory, "dataconnect.yaml");
    const dataconnectYaml = await (0, utils_1.wrappedSafeLoad)(file.source);
    return validateDataConnectYaml(dataconnectYaml);
}
exports.readDataConnectYaml = readDataConnectYaml;
function validateDataConnectYaml(unvalidated) {
    if (!unvalidated["location"]) {
        throw new error_1.FirebaseError("Missing required field 'location' in dataconnect.yaml");
    }
    return unvalidated;
}
async function readConnectorYaml(sourceDirectory) {
    const file = await (0, utils_1.readFileFromDirectory)(sourceDirectory, "connector.yaml");
    const connectorYaml = await (0, utils_1.wrappedSafeLoad)(file.source);
    return validateConnectorYaml(connectorYaml);
}
exports.readConnectorYaml = readConnectorYaml;
function validateConnectorYaml(unvalidated) {
    return unvalidated;
}
async function readGQLFiles(sourceDir) {
    if (!fs.existsSync(sourceDir)) {
        return [];
    }
    const files = await fs.readdir(sourceDir);
    return files
        .filter((f) => f.endsWith(".gql") || f.endsWith(".graphql"))
        .map((f) => toFile(sourceDir, f));
}
exports.readGQLFiles = readGQLFiles;
function toFile(sourceDir, relPath) {
    const fullPath = path.join(sourceDir, relPath);
    if (!fs.existsSync(fullPath)) {
        throw new error_1.FirebaseError(`file ${fullPath} not found`);
    }
    const content = fs.readFileSync(fullPath).toString();
    return {
        path: relPath,
        content,
    };
}
async function pickService(projectId, config, serviceId) {
    const serviceCfgs = readFirebaseJson(config);
    let serviceInfo;
    if (serviceCfgs.length === 0) {
        throw new error_1.FirebaseError("No Data Connect services found in firebase.json.");
    }
    else if (serviceCfgs.length === 1) {
        serviceInfo = await (0, load_1.load)(projectId, config, serviceCfgs[0].source);
    }
    else {
        if (!serviceId) {
            throw new error_1.FirebaseError("Multiple Data Connect services found in firebase.json. Please specify a service ID to use.");
        }
        const infos = await Promise.all(serviceCfgs.map((c) => (0, load_1.load)(projectId, config, c.source)));
        const maybe = infos.find((i) => i.dataConnectYaml.serviceId === serviceId);
        if (!maybe) {
            throw new error_1.FirebaseError(`No service named ${serviceId} declared in firebase.json.`);
        }
        serviceInfo = maybe;
    }
    return serviceInfo;
}
exports.pickService = pickService;
const WEB_INDICATORS = ["package.json", "package-lock.json", "node_modules"];
const IOS_INDICATORS = ["info.plist", "podfile", "package.swift", ".xcodeproj"];
const ANDROID_INDICATORS = ["androidmanifest.xml", "build.gradle", "build.gradle.kts"];
const DART_INDICATORS = ["pubspec.yaml", "pubspec.lock"];
const IOS_POSTFIX_INDICATORS = [".xcworkspace", ".xcodeproj"];
async function getPlatformFromFolder(dirPath) {
    const fileNames = await fs.readdir(dirPath);
    let hasWeb = false;
    let hasAndroid = false;
    let hasIOS = false;
    let hasDart = false;
    for (const fileName of fileNames) {
        const cleanedFileName = fileName.toLowerCase();
        hasWeb || (hasWeb = WEB_INDICATORS.some((indicator) => indicator === cleanedFileName));
        hasAndroid || (hasAndroid = ANDROID_INDICATORS.some((indicator) => indicator === cleanedFileName));
        hasIOS || (hasIOS = IOS_INDICATORS.some((indicator) => indicator === cleanedFileName) ||
            IOS_POSTFIX_INDICATORS.some((indicator) => cleanedFileName.endsWith(indicator)));
        hasDart || (hasDart = DART_INDICATORS.some((indicator) => indicator === cleanedFileName));
    }
    if (!hasWeb && !hasAndroid && !hasIOS && !hasDart) {
        return types_1.Platform.NONE;
    }
    else if (hasWeb && !hasAndroid && !hasIOS && !hasDart) {
        return types_1.Platform.WEB;
    }
    else if (hasAndroid && !hasWeb && !hasIOS && !hasDart) {
        return types_1.Platform.ANDROID;
    }
    else if (hasIOS && !hasWeb && !hasAndroid && !hasDart) {
        return types_1.Platform.IOS;
    }
    else if (hasDart && !hasWeb && !hasIOS && !hasAndroid) {
        return types_1.Platform.FLUTTER;
    }
    return types_1.Platform.MULTIPLE;
}
exports.getPlatformFromFolder = getPlatformFromFolder;
async function resolvePackageJson(packageJsonPath) {
    let validPackageJsonPath = packageJsonPath;
    if (!packageJsonPath.endsWith("package.json")) {
        validPackageJsonPath = path.join(packageJsonPath, "package.json");
    }
    validPackageJsonPath = path.resolve(validPackageJsonPath);
    try {
        return JSON.parse((await fs.readFile(validPackageJsonPath)).toString());
    }
    catch (_a) {
        return undefined;
    }
}
exports.resolvePackageJson = resolvePackageJson;
exports.SUPPORTED_FRAMEWORKS = ["react", "angular"];
exports.frameworksMap = {
    react: ["react", "next"],
    angular: ["@angular/core"],
};
function getFrameworksFromPackageJson(packageJson) {
    var _a, _b;
    const devDependencies = Object.keys((_a = packageJson.devDependencies) !== null && _a !== void 0 ? _a : {});
    const dependencies = Object.keys((_b = packageJson.dependencies) !== null && _b !== void 0 ? _b : {});
    const allDeps = Array.from(new Set([...devDependencies, ...dependencies]));
    return exports.SUPPORTED_FRAMEWORKS.filter((framework) => exports.frameworksMap[framework].find((dep) => allDeps.includes(dep)));
}
exports.getFrameworksFromPackageJson = getFrameworksFromPackageJson;
