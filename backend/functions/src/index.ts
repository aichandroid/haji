import * as admin from "firebase-admin";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { AdminFirestoreDatabase } from "./database.js";
import { AuthAdmin, TokenService } from "./tokenService.js";
import { ApiError } from "./types.js";

// Inisialisasi Firebase Admin SDK secara idempotent
if (admin.apps.length === 0) {
  admin.initializeApp();
}

const db = new AdminFirestoreDatabase(admin.firestore());
const authAdmin: AuthAdmin = {
  deleteUser: async (uid: string) => {
    await admin.auth().deleteUser(uid);
  }
};
const tokenService = new TokenService(db, authAdmin);

function mapErrorToHttpsError(err: unknown): never {
  if (err instanceof ApiError) {
    const errorDetails = {
      error_code: err.code,
      message: err.message
    };

    switch (err.code) {
      case "AUTH_REQUIRED":
        throw new HttpsError("unauthenticated", err.message, errorDetails);
      case "TOKEN_EXPIRED":
        throw new HttpsError("deadline-exceeded", err.message, errorDetails);
      case "TOKEN_ALREADY_USED":
        throw new HttpsError("already-exists", err.message, errorDetails);
      case "RATE_LIMITED":
        throw new HttpsError("resource-exhausted", err.message, errorDetails);
      case "TRACKING_ACCESS_REVOKED":
        throw new HttpsError("permission-denied", err.message, errorDetails);
      case "INVALID_ARGUMENT":
        throw new HttpsError("invalid-argument", err.message, errorDetails);
      default:
        throw new HttpsError("internal", err.message, errorDetails);
    }
  }

  if (err instanceof Error) {
    throw new HttpsError("internal", err.message);
  }

  throw new HttpsError("internal", "Terjadi kesalahan internal yang tidak diketahui.");
}

/**
 * Endpoint: POST /generatePairingToken
 * Menghasilkan token QR acak bertempo 5 menit.
 */
export const generatePairingToken = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autentikasi Firebase diperlukan.");
  }

  try {
    return await tokenService.generatePairingToken(request.auth.uid, request.data);
  } catch (err) {
    mapErrorToHttpsError(err);
  }
});

/**
 * Endpoint: POST /claimPairingToken
 * Pelacak mengklaim token setelah memindai QR untuk mengaktifkan hubungan pelacakan.
 */
export const claimPairingToken = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autentikasi Firebase diperlukan.");
  }

  try {
    return await tokenService.claimPairingToken(request.auth.uid, request.data);
  } catch (err) {
    mapErrorToHttpsError(err);
  }
});

/**
 * Endpoint: POST /revokeTrackingAccess
 * Orang yang dilacak mencabut hak akses salah satu pelacak secara instan.
 */
export const revokeTrackingAccess = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autentikasi Firebase diperlukan.");
  }

  try {
    return await tokenService.revokeTrackingAccess(request.auth.uid, request.data);
  } catch (err) {
    mapErrorToHttpsError(err);
  }
});

/**
 * Endpoint: POST /purgeUserData
 * Menghapus akun anonim, seluruh riwayat koordinat, dan relasi tracking (Right to be Forgotten).
 */
export const purgeUserData = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autentikasi Firebase diperlukan.");
  }

  try {
    return await tokenService.purgeUserData(request.auth.uid);
  } catch (err) {
    mapErrorToHttpsError(err);
  }
});

/**
 * Endpoint: POST /syncLocations
 * Sinkronisasi kumpulan koordinat lokasi dari antrean offline SQLite ke Cloud Firestore.
 */
export const syncLocations = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Autentikasi Firebase diperlukan.");
  }

  try {
    const locations = request.data?.locations || [];
    return await tokenService.syncLocations(request.auth.uid, locations);
  } catch (err) {
    mapErrorToHttpsError(err);
  }
});

export { TokenService };
export * from "./types.js";
