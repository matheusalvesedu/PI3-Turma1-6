"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.command = void 0;
const command_1 = require("../command");
const projectUtils_1 = require("../projectUtils");
const ensureApis_1 = require("../dataconnect/ensureApis");
const requirePermissions_1 = require("../requirePermissions");
const fileUtils_1 = require("../dataconnect/fileUtils");
const schemaMigration_1 = require("../dataconnect/schemaMigration");
const requireAuth_1 = require("../requireAuth");
const error_1 = require("../error");
const permissionsSetup_1 = require("../gcp/cloudsql/permissionsSetup");
const allowedRoles = Object.keys(permissionsSetup_1.fdcSqlRoleMap);
exports.command = new command_1.Command("dataconnect:sql:grant [serviceId]")
    .description("grants the SQL role <role> to the provided user or service account <email>")
    .option("-R, --role <role>", "The SQL role to grant. One of: owner, writer, or reader.")
    .option("-E, --email <email>", "The email of the user or service account we would like to grant the role to.")
    .before(requirePermissions_1.requirePermissions, ["firebasedataconnect.services.list"])
    .before(requireAuth_1.requireAuth)
    .action(async (serviceId, options) => {
    const role = options.role;
    const email = options.email;
    if (!role) {
        throw new error_1.FirebaseError("-R, --role <role> is required. Run the command with -h for more info.");
    }
    if (!email) {
        throw new error_1.FirebaseError("-E, --email <email> is required. Run the command with -h for more info.");
    }
    if (!allowedRoles.includes(role.toLowerCase())) {
        throw new error_1.FirebaseError(`Role should be one of ${allowedRoles.join(" | ")}.`);
    }
    const projectId = (0, projectUtils_1.needProjectId)(options);
    await (0, ensureApis_1.ensureApis)(projectId);
    const serviceInfo = await (0, fileUtils_1.pickService)(projectId, options.config, serviceId);
    await (0, schemaMigration_1.grantRoleToUserInSchema)(options, serviceInfo.schema);
    return { projectId, serviceId };
});
