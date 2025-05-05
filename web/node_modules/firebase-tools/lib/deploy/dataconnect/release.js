"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const utils = require("../../utils");
const client_1 = require("../../dataconnect/client");
const prompts_1 = require("../../dataconnect/prompts");
const schemaMigration_1 = require("../../dataconnect/schemaMigration");
const projectUtils_1 = require("../../projectUtils");
async function default_1(context, options) {
    const project = (0, projectUtils_1.needProjectId)(options);
    const serviceInfos = context.dataconnect.serviceInfos;
    const filters = context.dataconnect.filters;
    const wantSchemas = serviceInfos
        .filter((si) => {
        return (!filters ||
            filters.some((f) => {
                return f.serviceId === si.dataConnectYaml.serviceId && (f.schemaOnly || f.fullService);
            }));
    })
        .map((s) => {
        var _a;
        return ({
            schema: s.schema,
            validationMode: (_a = s.dataConnectYaml.schema.datasource.postgresql) === null || _a === void 0 ? void 0 : _a.schemaValidation,
        });
    });
    if (wantSchemas.length) {
        utils.logLabeledBullet("dataconnect", "Deploying Data Connect schemas...");
        for (const s of wantSchemas) {
            await (0, schemaMigration_1.migrateSchema)({
                options,
                schema: s.schema,
                validateOnly: false,
                schemaValidation: s.validationMode,
            });
        }
        utils.logLabeledBullet("dataconnect", "Schemas deployed.");
    }
    let wantConnectors = [];
    wantConnectors = wantConnectors.concat(...serviceInfos.map((si) => si.connectorInfo
        .filter((c) => {
        return (!filters ||
            filters.some((f) => {
                return (f.serviceId === si.dataConnectYaml.serviceId &&
                    (f.connectorId === c.connectorYaml.connectorId || f.fullService));
            }));
    })
        .map((c) => c.connector)));
    const haveConnectors = await have(serviceInfos);
    const connectorsToDelete = filters
        ? []
        : haveConnectors.filter((h) => !wantConnectors.some((w) => w.name === h.name));
    if (wantConnectors.length) {
        utils.logLabeledBullet("dataconnect", "Deploying connectors...");
        await Promise.all(wantConnectors.map(async (c) => {
            await (0, client_1.upsertConnector)(c);
            utils.logLabeledSuccess("dataconnect", `Deployed connector ${c.name}`);
        }));
        for (const c of connectorsToDelete) {
            await (0, prompts_1.promptDeleteConnector)(options, c.name);
        }
        utils.logLabeledBullet("dataconnect", "Connectors deployed.");
    }
    else {
        utils.logLabeledBullet("dataconnect", "No connectors to deploy.");
    }
    utils.logLabeledSuccess("dataconnect", `Deployment complete! View your deployed schema and connectors at ${utils.consoleUrl(project, "/dataconnect")}`);
    return;
}
exports.default = default_1;
async function have(serviceInfos) {
    let connectors = [];
    for (const si of serviceInfos) {
        connectors = connectors.concat(await (0, client_1.listConnectors)(si.serviceName));
    }
    return connectors;
}
