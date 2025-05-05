"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.doSetup = void 0;
const logger_1 = require("../../../logger");
const apiEnabled = require("../../../ensureApiEnabled");
const requirePermissions_1 = require("../../../requirePermissions");
const checkDatabaseType_1 = require("../../../firestore/checkDatabaseType");
const rules = require("./rules");
const indexes = require("./indexes");
const error_1 = require("../../../error");
const clc = require("colorette");
const prompt_1 = require("../../../prompt");
async function checkProjectSetup(setup, config, options) {
    const firestoreUnusedError = new error_1.FirebaseError(`It looks like you haven't used Cloud Firestore in this project before. Go to ${clc.bold(clc.underline(`https://console.firebase.google.com/project/${setup.projectId}/firestore`))} to create your Cloud Firestore database.`, { exit: 1 });
    const isFirestoreEnabled = await apiEnabled.check(setup.projectId, "firestore.googleapis.com", "", true);
    if (!isFirestoreEnabled) {
        throw firestoreUnusedError;
    }
    const dbType = await getDatabaseType(setup);
    logger_1.logger.debug(`database_type: ${dbType}`);
    if (!dbType) {
        throw firestoreUnusedError;
    }
    else if (dbType !== "FIRESTORE_NATIVE") {
        throw new error_1.FirebaseError(`It looks like this project is using Cloud Datastore or Cloud Firestore in Datastore mode. The Firebase CLI can only manage projects using Cloud Firestore in Native mode. For more information, visit https://cloud.google.com/datastore/docs/firestore-or-datastore`, { exit: 1 });
    }
    await (0, requirePermissions_1.requirePermissions)(Object.assign(Object.assign({}, options), { project: setup.projectId }));
}
async function getDatabaseType(setup) {
    const dbType = await (0, checkDatabaseType_1.checkDatabaseType)(setup.projectId, setup.databaseId);
    logger_1.logger.debug(`database_type: ${dbType}`);
    if (dbType === "DATABASE_DOES_NOT_EXIST") {
        setup.databaseId = await selectDatabaseByPrompting();
        return await getDatabaseType(setup);
    }
    else {
        return dbType;
    }
}
async function selectDatabaseByPrompting() {
    const database = await (0, prompt_1.promptOnce)({
        type: "input",
        message: "Please input the name of the Native Firestore database you would like to use:",
    });
    return database;
}
async function doSetup(setup, config, options) {
    if (setup.projectId) {
        await checkProjectSetup(setup, config, options);
    }
    setup.config.firestore = {};
    await rules.initRules(setup, config);
    await indexes.initIndexes(setup, config);
}
exports.doSetup = doSetup;
