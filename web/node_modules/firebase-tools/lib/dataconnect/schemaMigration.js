"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ensureServiceIsConnectedToCloudSql = exports.getIdentifiers = exports.grantRoleToUserInSchema = exports.migrateSchema = exports.diffSchema = void 0;
const clc = require("colorette");
const sql_formatter_1 = require("sql-formatter");
const types_1 = require("./types");
const client_1 = require("./client");
const connect_1 = require("../gcp/cloudsql/connect");
const projectUtils_1 = require("../projectUtils");
const permissionsSetup_1 = require("../gcp/cloudsql/permissionsSetup");
const permissions_1 = require("../gcp/cloudsql/permissions");
const prompt_1 = require("../prompt");
const logger_1 = require("../logger");
const error_1 = require("../error");
const utils_1 = require("../utils");
const cloudsqladmin_1 = require("../gcp/cloudsql/cloudsqladmin");
const cloudSqlAdminClient = require("../gcp/cloudsql/cloudsqladmin");
const errors = require("./errors");
async function setupSchemaIfNecessary(instanceId, databaseId, options) {
    await (0, connect_1.setupIAMUsers)(instanceId, databaseId, options);
    const schemaInfo = await (0, permissionsSetup_1.getSchemaMetadata)(instanceId, databaseId, permissions_1.DEFAULT_SCHEMA, options);
    if (schemaInfo.setupStatus !== permissionsSetup_1.SchemaSetupStatus.BrownField &&
        schemaInfo.setupStatus !== permissionsSetup_1.SchemaSetupStatus.GreenField) {
        return await (0, permissionsSetup_1.setupSQLPermissions)(instanceId, databaseId, schemaInfo, options, true);
    }
    else {
        logger_1.logger.debug(`Detected schema "${schemaInfo.name}" is setup in ${schemaInfo.setupStatus} mode. Skipping Setup.`);
    }
    return schemaInfo.setupStatus;
}
async function diffSchema(options, schema, schemaValidation) {
    const { serviceName, instanceName, databaseId, instanceId } = getIdentifiers(schema);
    await ensureServiceIsConnectedToCloudSql(serviceName, instanceName, databaseId, false);
    let diffs = [];
    await setupSchemaIfNecessary(instanceId, databaseId, options);
    let validationMode = schemaValidation !== null && schemaValidation !== void 0 ? schemaValidation : "COMPATIBLE";
    setSchemaValidationMode(schema, validationMode);
    try {
        if (!schemaValidation) {
            (0, utils_1.logLabeledBullet)("dataconnect", `generating required schema changes...`);
        }
        await (0, client_1.upsertSchema)(schema, true);
        if (validationMode === "STRICT") {
            (0, utils_1.logLabeledSuccess)("dataconnect", `Database schema is up to date.`);
        }
        else {
            (0, utils_1.logLabeledSuccess)("dataconnect", `Database schema is compatible.`);
        }
    }
    catch (err) {
        if ((err === null || err === void 0 ? void 0 : err.status) !== 400) {
            throw err;
        }
        const invalidConnectors = errors.getInvalidConnectors(err);
        const incompatible = errors.getIncompatibleSchemaError(err);
        if (!incompatible && !invalidConnectors.length) {
            throw err;
        }
        if (invalidConnectors.length) {
            displayInvalidConnectors(invalidConnectors);
        }
        if (incompatible) {
            displaySchemaChanges(incompatible, validationMode, instanceName, databaseId);
            diffs = incompatible.diffs;
        }
    }
    if (!schemaValidation) {
        validationMode = "STRICT";
        setSchemaValidationMode(schema, validationMode);
        try {
            (0, utils_1.logLabeledBullet)("dataconnect", `generating schema changes, including optional changes...`);
            await (0, client_1.upsertSchema)(schema, true);
            (0, utils_1.logLabeledSuccess)("dataconnect", `no additional optional changes`);
        }
        catch (err) {
            if ((err === null || err === void 0 ? void 0 : err.status) !== 400) {
                throw err;
            }
            const incompatible = errors.getIncompatibleSchemaError(err);
            if (incompatible) {
                if (!diffsEqual(diffs, incompatible.diffs)) {
                    if (diffs.length === 0) {
                        displaySchemaChanges(incompatible, "STRICT_AFTER_COMPATIBLE", instanceName, databaseId);
                    }
                    else {
                        displaySchemaChanges(incompatible, validationMode, instanceName, databaseId);
                    }
                    diffs = incompatible.diffs;
                }
                else {
                    (0, utils_1.logLabeledSuccess)("dataconnect", `no additional optional changes`);
                }
            }
        }
    }
    return diffs;
}
exports.diffSchema = diffSchema;
async function migrateSchema(args) {
    const { options, schema, validateOnly, schemaValidation } = args;
    const { serviceName, instanceId, instanceName, databaseId } = getIdentifiers(schema);
    await ensureServiceIsConnectedToCloudSql(serviceName, instanceName, databaseId, true);
    await (0, connect_1.setupIAMUsers)(instanceId, databaseId, options);
    let diffs = [];
    await setupSchemaIfNecessary(instanceId, databaseId, options);
    let validationMode = schemaValidation !== null && schemaValidation !== void 0 ? schemaValidation : "COMPATIBLE";
    setSchemaValidationMode(schema, validationMode);
    try {
        await (0, client_1.upsertSchema)(schema, validateOnly);
        logger_1.logger.debug(`Database schema was up to date for ${instanceId}:${databaseId}`);
    }
    catch (err) {
        if ((err === null || err === void 0 ? void 0 : err.status) !== 400) {
            throw err;
        }
        const incompatible = errors.getIncompatibleSchemaError(err);
        const invalidConnectors = errors.getInvalidConnectors(err);
        if (!incompatible && !invalidConnectors.length) {
            throw err;
        }
        const migrationMode = await promptForSchemaMigration(options, instanceName, databaseId, incompatible, validateOnly, validationMode);
        const shouldDeleteInvalidConnectors = await promptForInvalidConnectorError(options, serviceName, invalidConnectors, validateOnly);
        if (incompatible) {
            diffs = await handleIncompatibleSchemaError({
                options,
                databaseId,
                instanceId,
                incompatibleSchemaError: incompatible,
                choice: migrationMode,
            });
        }
        if (shouldDeleteInvalidConnectors) {
            await deleteInvalidConnectors(invalidConnectors);
        }
        if (!validateOnly) {
            await (0, client_1.upsertSchema)(schema, validateOnly);
        }
    }
    if (!schemaValidation) {
        validationMode = "STRICT";
        setSchemaValidationMode(schema, validationMode);
        try {
            await (0, client_1.upsertSchema)(schema, validateOnly);
        }
        catch (err) {
            if (err.status !== 400) {
                throw err;
            }
            const incompatible = errors.getIncompatibleSchemaError(err);
            const invalidConnectors = errors.getInvalidConnectors(err);
            if (!incompatible && !invalidConnectors.length) {
                throw err;
            }
            const migrationMode = await promptForSchemaMigration(options, instanceName, databaseId, incompatible, validateOnly, "STRICT_AFTER_COMPATIBLE");
            if (incompatible) {
                const maybeDiffs = await handleIncompatibleSchemaError({
                    options,
                    databaseId,
                    instanceId,
                    incompatibleSchemaError: incompatible,
                    choice: migrationMode,
                });
                diffs = diffs.concat(maybeDiffs);
            }
        }
    }
    return diffs;
}
exports.migrateSchema = migrateSchema;
async function grantRoleToUserInSchema(options, schema) {
    const role = options.role;
    const email = options.email;
    const { instanceId, databaseId } = getIdentifiers(schema);
    const projectId = (0, projectUtils_1.needProjectId)(options);
    const { user, mode } = (0, connect_1.toDatabaseUser)(email);
    const fdcSqlRole = permissionsSetup_1.fdcSqlRoleMap[role](databaseId);
    await (0, connect_1.setupIAMUsers)(instanceId, databaseId, options);
    const userIsCSQLAdmin = await (0, cloudsqladmin_1.iamUserIsCSQLAdmin)(options);
    if (!userIsCSQLAdmin) {
        throw new error_1.FirebaseError(`Only users with 'roles/cloudsql.admin' can grant SQL roles. If you do not have this role, ask your database administrator to run this command or manually grant ${fdcSqlRole} to ${user}`);
    }
    const schemaSetupStatus = await setupSchemaIfNecessary(instanceId, databaseId, options);
    if (schemaSetupStatus !== permissionsSetup_1.SchemaSetupStatus.GreenField &&
        fdcSqlRole === (0, permissions_1.firebaseowner)(databaseId, permissions_1.DEFAULT_SCHEMA)) {
        throw new error_1.FirebaseError(`Owner rule isn't available in brownfield databases. If you would like Data Connect to manage and own your database schema, run 'firebase dataconnect:sql:setup'`);
    }
    await cloudSqlAdminClient.createUser(projectId, instanceId, mode, user);
    await (0, connect_1.executeSqlCmdsAsSuperUser)(options, instanceId, databaseId, [`GRANT "${fdcSqlRole}" TO "${user}"`], false);
}
exports.grantRoleToUserInSchema = grantRoleToUserInSchema;
function diffsEqual(x, y) {
    if (x.length !== y.length) {
        return false;
    }
    for (let i = 0; i < x.length; i++) {
        if (x[i].description !== y[i].description ||
            x[i].destructive !== y[i].destructive ||
            x[i].sql !== y[i].sql) {
            return false;
        }
    }
    return true;
}
function setSchemaValidationMode(schema, schemaValidation) {
    const postgresDatasource = schema.datasources.find((d) => d.postgresql);
    if (postgresDatasource === null || postgresDatasource === void 0 ? void 0 : postgresDatasource.postgresql) {
        postgresDatasource.postgresql.schemaValidation = schemaValidation;
    }
}
function getIdentifiers(schema) {
    var _a, _b;
    const postgresDatasource = schema.datasources.find((d) => d.postgresql);
    const databaseId = (_a = postgresDatasource === null || postgresDatasource === void 0 ? void 0 : postgresDatasource.postgresql) === null || _a === void 0 ? void 0 : _a.database;
    if (!databaseId) {
        throw new error_1.FirebaseError("Service does not have a postgres datasource, cannot migrate");
    }
    const instanceName = (_b = postgresDatasource === null || postgresDatasource === void 0 ? void 0 : postgresDatasource.postgresql) === null || _b === void 0 ? void 0 : _b.cloudSql.instance;
    if (!instanceName) {
        throw new error_1.FirebaseError("tried to migrate schema but instance name was not provided in dataconnect.yaml");
    }
    const instanceId = instanceName.split("/").pop();
    const serviceName = schema.name.replace(`/schemas/${types_1.SCHEMA_ID}`, "");
    return {
        databaseId,
        instanceId,
        instanceName,
        serviceName,
    };
}
exports.getIdentifiers = getIdentifiers;
function suggestedCommand(serviceName, invalidConnectorNames) {
    const serviceId = serviceName.split("/")[5];
    const connectorIds = invalidConnectorNames.map((i) => i.split("/")[7]);
    const onlys = connectorIds.map((c) => `dataconnect:${serviceId}:${c}`).join(",");
    return `firebase deploy --only ${onlys}`;
}
async function handleIncompatibleSchemaError(args) {
    const { incompatibleSchemaError, options, instanceId, databaseId, choice } = args;
    if (incompatibleSchemaError.destructive && choice === "safe") {
        throw new error_1.FirebaseError("This schema migration includes potentially destructive changes. If you'd like to execute it anyway, rerun this command with --force");
    }
    const commandsToExecute = incompatibleSchemaError.diffs
        .filter((d) => {
        switch (choice) {
            case "all":
                return true;
            case "safe":
                return !d.destructive;
            case "none":
                return false;
        }
    })
        .map((d) => d.sql);
    if (commandsToExecute.length) {
        const commandsToExecuteBySuperUser = commandsToExecute.filter((sql) => sql.startsWith("CREATE EXTENSION") || sql.startsWith("CREATE SCHEMA"));
        const commandsToExecuteByOwner = commandsToExecute.filter((sql) => !commandsToExecuteBySuperUser.includes(sql));
        const userIsCSQLAdmin = await (0, cloudsqladmin_1.iamUserIsCSQLAdmin)(options);
        if (!userIsCSQLAdmin && commandsToExecuteBySuperUser.length) {
            throw new error_1.FirebaseError(`Some SQL commands required for this migration require Admin permissions.\n 
        Please ask a user with 'roles/cloudsql.admin' to apply the following commands.\n
        ${commandsToExecuteBySuperUser.join("\n")}`);
        }
        const schemaInfo = await (0, permissionsSetup_1.getSchemaMetadata)(instanceId, databaseId, permissions_1.DEFAULT_SCHEMA, options);
        if (schemaInfo.setupStatus !== permissionsSetup_1.SchemaSetupStatus.GreenField) {
            throw new error_1.FirebaseError(`Brownfield database are protected from SQL changes by Data Connect.\n` +
                `You can use the SQL diff generated by 'firebase dataconnect:sql:diff' to assist you in applying the required changes to your CloudSQL database. Connector deployment will succeed when there is no required diff changes.\n` +
                `If you would like Data Connect to manage your database schema, run 'firebase dataconnect:sql:setup'`);
        }
        if (!(await (0, permissionsSetup_1.checkSQLRoleIsGranted)(options, instanceId, databaseId, (0, permissions_1.firebaseowner)(databaseId), (await (0, connect_1.getIAMUser)(options)).user))) {
            throw new error_1.FirebaseError(`Command aborted. Only users granted firebaseowner SQL role can run migrations.`);
        }
        if (commandsToExecuteBySuperUser.length) {
            logger_1.logger.info(`The diffs require CloudSQL superuser permissions, attempting to apply changes as superuser.`);
            await (0, connect_1.executeSqlCmdsAsSuperUser)(options, instanceId, databaseId, commandsToExecuteBySuperUser, false);
        }
        if (commandsToExecuteByOwner.length) {
            await (0, connect_1.executeSqlCmdsAsIamUser)(options, instanceId, databaseId, [`SET ROLE "${(0, permissions_1.firebaseowner)(databaseId)}"`, ...commandsToExecuteByOwner], false);
            return incompatibleSchemaError.diffs;
        }
    }
    return [];
}
async function promptForSchemaMigration(options, instanceName, databaseId, err, validateOnly, validationMode) {
    if (!err) {
        return "none";
    }
    if (validationMode === "STRICT_AFTER_COMPATIBLE" && (options.nonInteractive || options.force)) {
        return "none";
    }
    displaySchemaChanges(err, validationMode, instanceName, databaseId);
    if (!options.nonInteractive) {
        if (validateOnly && options.force) {
            return "all";
        }
        const message = validationMode === "STRICT_AFTER_COMPATIBLE"
            ? `Would you like to execute these optional changes against ${databaseId} in your CloudSQL instance ${instanceName}?`
            : `Would you like to execute these changes against ${databaseId} in your CloudSQL instance ${instanceName}?`;
        let executeChangePrompt = "Execute changes";
        if (validationMode === "STRICT_AFTER_COMPATIBLE") {
            executeChangePrompt = "Execute optional changes";
        }
        if (err.destructive) {
            executeChangePrompt = executeChangePrompt + " (including destructive changes)";
        }
        const choices = [
            { name: executeChangePrompt, value: "all" },
            { name: "Abort changes", value: "none" },
        ];
        const defaultValue = validationMode === "STRICT_AFTER_COMPATIBLE" ? "none" : "all";
        return await (0, prompt_1.promptOnce)({
            message: message,
            type: "list",
            choices,
            default: defaultValue,
        });
    }
    if (!validateOnly) {
        throw new error_1.FirebaseError("Command aborted. Your database schema is incompatible with your Data Connect schema. Run `firebase dataconnect:sql:migrate` to migrate your database schema");
    }
    else if (options.force) {
        return "all";
    }
    else if (!err.destructive) {
        return "all";
    }
    else {
        throw new error_1.FirebaseError("Command aborted. This schema migration includes potentially destructive changes. If you'd like to execute it anyway, rerun this command with --force");
    }
}
async function promptForInvalidConnectorError(options, serviceName, invalidConnectors, validateOnly) {
    if (!invalidConnectors.length) {
        return false;
    }
    displayInvalidConnectors(invalidConnectors);
    if (validateOnly) {
        return false;
    }
    if (options.force) {
        return true;
    }
    if (!options.nonInteractive &&
        (await (0, prompt_1.confirm)(Object.assign(Object.assign({}, options), { message: `Would you like to delete and recreate these connectors? This will cause ${clc.red(`downtime`)}.` })))) {
        return true;
    }
    const cmd = suggestedCommand(serviceName, invalidConnectors);
    throw new error_1.FirebaseError(`Command aborted. Try deploying those connectors first with ${clc.bold(cmd)}`);
}
async function deleteInvalidConnectors(invalidConnectors) {
    return Promise.all(invalidConnectors.map(client_1.deleteConnector));
}
function displayInvalidConnectors(invalidConnectors) {
    const connectorIds = invalidConnectors.map((i) => i.split("/").pop()).join(", ");
    (0, utils_1.logLabeledWarning)("dataconnect", `The schema you are deploying is incompatible with the following existing connectors: ${connectorIds}.`);
    (0, utils_1.logLabeledWarning)("dataconnect", `This is a ${clc.red("breaking")} change and may break existing apps.`);
}
async function ensureServiceIsConnectedToCloudSql(serviceName, instanceId, databaseId, linkIfNotConnected) {
    let currentSchema = await (0, client_1.getSchema)(serviceName);
    if (!currentSchema) {
        if (!linkIfNotConnected) {
            (0, utils_1.logLabeledWarning)("dataconnect", `Not yet linked to the Cloud SQL instance.`);
            return;
        }
        (0, utils_1.logLabeledBullet)("dataconnect", `Linking the Cloud SQL instance...`);
        currentSchema = {
            name: `${serviceName}/schemas/${types_1.SCHEMA_ID}`,
            source: {
                files: [],
            },
            datasources: [
                {
                    postgresql: {
                        database: databaseId,
                        schemaValidation: "NONE",
                        cloudSql: {
                            instance: instanceId,
                        },
                    },
                },
            ],
        };
    }
    const postgresDatasource = currentSchema.datasources.find((d) => d.postgresql);
    const postgresql = postgresDatasource === null || postgresDatasource === void 0 ? void 0 : postgresDatasource.postgresql;
    if ((postgresql === null || postgresql === void 0 ? void 0 : postgresql.cloudSql.instance) !== instanceId) {
        (0, utils_1.logLabeledWarning)("dataconnect", `Switching connected Cloud SQL instance\nFrom ${postgresql === null || postgresql === void 0 ? void 0 : postgresql.cloudSql.instance}\nTo ${instanceId}`);
    }
    if ((postgresql === null || postgresql === void 0 ? void 0 : postgresql.database) !== databaseId) {
        (0, utils_1.logLabeledWarning)("dataconnect", `Switching connected Postgres database from ${postgresql === null || postgresql === void 0 ? void 0 : postgresql.database} to ${databaseId}`);
    }
    if (!postgresql || postgresql.schemaValidation !== "NONE") {
        return;
    }
    postgresql.schemaValidation = "STRICT";
    try {
        await (0, client_1.upsertSchema)(currentSchema, false);
    }
    catch (err) {
        if ((err === null || err === void 0 ? void 0 : err.status) >= 500) {
            throw err;
        }
        logger_1.logger.debug(err);
    }
}
exports.ensureServiceIsConnectedToCloudSql = ensureServiceIsConnectedToCloudSql;
function displaySchemaChanges(error, validationMode, instanceName, databaseId) {
    switch (error.violationType) {
        case "INCOMPATIBLE_SCHEMA":
            {
                let message;
                if (validationMode === "COMPATIBLE") {
                    message =
                        "Your PostgreSQL database " +
                            databaseId +
                            " in your CloudSQL instance " +
                            instanceName +
                            " must be migrated in order to be compatible with your application schema. " +
                            "The following SQL statements will migrate your database schema to be compatible with your new Data Connect schema.\n" +
                            error.diffs.map(toString).join("\n");
                }
                else if (validationMode === "STRICT_AFTER_COMPATIBLE") {
                    message =
                        "Your new application schema is compatible with the schema of your PostgreSQL database " +
                            databaseId +
                            " in your CloudSQL instance " +
                            instanceName +
                            ", but contains unused tables or columns. " +
                            "The following optional SQL statements will migrate your database schema to match your new Data Connect schema.\n" +
                            error.diffs.map(toString).join("\n");
                }
                else {
                    message =
                        "Your PostgreSQL database " +
                            databaseId +
                            " in your CloudSQL instance " +
                            instanceName +
                            " must be migrated in order to match your application schema. " +
                            "The following SQL statements will migrate your database schema to match your new Data Connect schema.\n" +
                            error.diffs.map(toString).join("\n");
                }
                (0, utils_1.logLabeledWarning)("dataconnect", message);
            }
            break;
        case "INACCESSIBLE_SCHEMA":
            {
                const message = "Cannot access your CloudSQL database to validate schema. " +
                    "The following SQL statements can setup a new database schema.\n" +
                    error.diffs.map(toString).join("\n");
                (0, utils_1.logLabeledWarning)("dataconnect", message);
                (0, utils_1.logLabeledWarning)("dataconnect", "Some SQL resources may already exist.");
            }
            break;
        default:
            throw new error_1.FirebaseError(`Unknown schema violation type: ${error.violationType}, IncompatibleSqlSchemaError: ${error}`);
    }
}
function toString(diff) {
    return `\/** ${diff.destructive ? clc.red("Destructive: ") : ""}${diff.description}*\/\n${(0, sql_formatter_1.format)(diff.sql, { language: "postgresql" })}`;
}
