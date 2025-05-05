"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.timingSafeEqual = exports.createHashKey = exports.createHmacKey = exports.pbkdf2 = void 0;
async function pbkdf2(password, salt, iterations, length, hashAlgorithm) {
    const encoder = new TextEncoder();
    const keyMaterial = await crypto.subtle.importKey('raw', encoder.encode(password), 'PBKDF2', false, ['deriveBits']);
    const derivedBits = await crypto.subtle.deriveBits({
        name: 'PBKDF2',
        salt,
        iterations: iterations,
        hash: hashAlgorithm,
    }, keyMaterial, length * 8);
    return derivedBits;
}
exports.pbkdf2 = pbkdf2;
async function createHmacKey(key, message, algorithm) {
    const encoder = new TextEncoder();
    const keyMaterial = await crypto.subtle.importKey('raw', key, {
        name: 'HMAC',
        hash: { name: algorithm },
    }, false, ['sign']);
    const hmacKey = await crypto.subtle.sign('HMAC', keyMaterial, encoder.encode(message));
    return hmacKey;
}
exports.createHmacKey = createHmacKey;
async function createHashKey(key, algorithm) {
    const hashKey = await crypto.subtle.digest({
        name: algorithm,
    }, key);
    return hashKey;
}
exports.createHashKey = createHashKey;
async function timingSafeEqual(bufferA, bufferB) {
    const algorithm = { name: 'HMAC', hash: 'SHA-256' };
    const key = (await crypto.subtle.generateKey(algorithm, false, ['sign', 'verify']));
    const hmac = await crypto.subtle.sign(algorithm, key, bufferA);
    const equal = await crypto.subtle.verify(algorithm, key, hmac, bufferB);
    return equal;
}
exports.timingSafeEqual = timingSafeEqual;
