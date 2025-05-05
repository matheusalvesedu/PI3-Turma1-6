"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.doSetup = void 0;
const clc = require("colorette");
const utils = require("../../utils");
const templates_1 = require("../../templates");
const cloudbilling_1 = require("../../gcp/cloudbilling");
const APPHOSTING_YAML_TEMPLATE = (0, templates_1.readTemplateSync)("init/apphosting/apphosting.yaml");
async function doSetup(setup, config) {
    await (0, cloudbilling_1.checkBillingEnabled)(setup.projectId);
    utils.logBullet("Writing default settings to " + clc.bold("apphosting.yaml") + "...");
    await config.askWriteProjectFile("apphosting.yaml", APPHOSTING_YAML_TEMPLATE);
    utils.logSuccess("Create a new App Hosting backend with `firebase apphosting:backends:create`");
}
exports.doSetup = doSetup;
