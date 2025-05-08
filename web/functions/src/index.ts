import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import * as QRCode from "qrcode";
import cors from 'cors';

admin.initializeApp();
const db = admin.firestore();
const corsHandler = cors({ origin: true });

interface PartnerData {
    apiKey: string;
    url: string;
}

interface LoginData {
    apiKey: string;
    createdAt: Date;
    loginToken: string;
    user?: string; 
}

export const performAuth = functions.https.onRequest((request, response) => {
    corsHandler(request, response, async () => {
        const requestData: PartnerData = request.body;

        if (!requestData.url || !requestData.apiKey) {
            response.status(400).json({ error: 'siteUrl and apiKey are required' });
            return;
        }

        try {
            const partnersRef = db.collection('testePartners');
            const partnerSnapshot = await partnersRef.where('url', '==', requestData.url).get();

            if (partnerSnapshot.empty) {
                return response.status(403).json({ error: 'Partner not found' });
            }

            const partnerDoc = partnerSnapshot.docs[0];
            const partnerDocData = partnerDoc.data() as PartnerData;

            if (!partnerDocData) {
                return response.status(403).json({ error: 'Partner not registered' });
            }

            if (partnerDocData.apiKey !== requestData.apiKey) {
                return response.status(403).json({ error: 'Invalid API key' });
            }

            const loginToken = require('crypto').randomBytes(128).toString('hex');
            const createdAt = new Date();
            
            const loginData: LoginData = {
                apiKey: requestData.apiKey,
                createdAt,
                loginToken,
            };

            await db.collection('login').doc(loginToken).set(loginData);

            const qrImage = await QRCode.toDataURL(loginToken);

            return response.status(200).json({ qrCodeBase64: qrImage, loginToken });

        } catch (err) {
            return response.status(500).json({ error: 'Internal server error' });
        }
    });
});

export const getLoginStatus = functions.https.onRequest((request, response) => {
    corsHandler(request, response, async () => {
        const { loginToken } = request.body;

        if (!loginToken) {
            response.status(400).json({ error: 'loginToken is required' });
            return;
        }

        try {
            const loginDoc = await db.collection('login').doc(loginToken).get();

            if (!loginDoc.exists) {
                response.status(404).json({ error: 'Invalid loginToken' });
                return;
            }

            const loginDocData = loginDoc.data();
            const loginData = loginDocData ? {
                ...loginDocData,
                createdAt: loginDocData.createdAt instanceof Date ? 
                           loginDocData.createdAt : 
                           new Date(loginDocData.createdAt._seconds * 1000)
            } as LoginData : undefined;

            const now = new Date();

            if (!loginData || !loginData.createdAt) {
                response.status(404).json({ error: 'Invalid loginToken' });
                return;
            }

            const elapsedSeconds = (now.getTime() - new Date(loginData.createdAt).getTime()) / 1000;

            if (elapsedSeconds > 60) {
                await db.collection('login').doc(loginToken).delete();
                response.status(410).json({ error: 'Token expired' });
                return;
            }

            if (loginData.user) {
                response.status(200).json({ status: 'authenticated', uid: loginData.user });
            } else {
                response.status(200).json({ status: 'pending' });
            }
        } catch (err) {
            response.status(500).json({ error: 'Internal server error' });
        }
    });
});
