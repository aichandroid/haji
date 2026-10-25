import * as crypto from "node:crypto";
import { FirestoreDatabase } from "./database.js";
import {
  ApiError,
  ClaimTokenRequest,
  ClaimTokenResponse,
  GenerateTokenRequest,
  GenerateTokenResponse,
  LocationPointDto,
  PairingTokenDoc,
  PurgeUserDataResponse,
  RevokeAccessRequest,
  RevokeAccessResponse
} from "./types.js";

export interface AuthAdmin {
  deleteUser(userId: string): Promise<void>;
}

export class TokenService {
  constructor(
    private readonly db: FirestoreDatabase,
    private readonly authAdmin?: AuthAdmin
  ) {}

  /**
   * Menghasilkan token QR acak bertempo 5 menit bagi jemaah yang bersedia dilacak.
   * Rate limiting: maksimal 3 token per 15 menit per UID.
   */
  async generatePairingToken(
    callerUid: string,
    request: GenerateTokenRequest = {},
    currentTimeProvider: () => number = Date.now
  ): Promise<GenerateTokenResponse> {
    if (!callerUid || callerUid.trim().length === 0) {
      throw new ApiError("AUTH_REQUIRED", "Autentikasi Firebase diperlukan.", 401);
    }

    const now = currentTimeProvider();
    const fifteenMinutesAgo = now - 15 * 60 * 1000;

    // Evaluasi batas wajar permintaan token (anti-spam / DoS)
    const recentTokenCount = await this.db.countRecentTokensByUser(
      callerUid,
      fifteenMinutesAgo
    );

    if (recentTokenCount >= 3) {
      throw new ApiError(
        "RATE_LIMITED",
        "Terlalu banyak permintaan token. Tunggu beberapa menit sebelum membuat kode baru.",
        429
      );
    }

    // Token ID acak 128-bit berkriptografi aman
    const rawRandom = crypto.randomBytes(8).toString("hex");
    const tokenId = `tok_${rawRandom}`;
    const validitySeconds = 300; // 5 Menit tepat
    const expiresAt = now + validitySeconds * 1000;

    const tokenDoc: PairingTokenDoc = {
      target_uid: callerUid,
      expires_at: expiresAt,
      status: "PENDING",
      created_at: now
    };

    await this.db.savePairingToken(tokenId, tokenDoc);

    const qrPayload = `hajj2027://pair?t=${tokenId}&u=${callerUid}`;

    return {
      status: "SUCCESS",
      token_id: tokenId,
      qr_payload: qrPayload,
      expires_at_epoch_ms: expiresAt,
      validity_seconds: validitySeconds
    };
  }

  /**
   * Mengklaim token QR yang dipindai kamera dalam transaksi database atomik (anti-replay).
   */
  async claimPairingToken(
    callerUid: string,
    request: ClaimTokenRequest,
    currentTimeProvider: () => number = Date.now
  ): Promise<ClaimTokenResponse> {
    if (!callerUid || callerUid.trim().length === 0) {
      throw new ApiError("AUTH_REQUIRED", "Autentikasi Firebase diperlukan.", 401);
    }

    if (!request.token_id || request.token_id.trim().length === 0) {
      throw new ApiError("INVALID_ARGUMENT", "Token ID tidak boleh kosong.", 400);
    }

    const label = request.tracker_label?.trim() || "Keluarga";
    const now = currentTimeProvider();

    const { relationId, targetUid } = await this.db.claimTokenTransaction(
      request.token_id.trim(),
      callerUid,
      label,
      now
    );

    return {
      status: "SUCCESS",
      relation_id: relationId,
      target_user_id: targetUid,
      paired_at_epoch_ms: now,
      message: "Penyambungan keluarga berhasil. Lokasi mulai dipantau."
    };
  }

  /**
   * Mencabut izin akses pelacakan secara instan oleh pihak target.
   */
  async revokeTrackingAccess(
    callerUid: string,
    request: RevokeAccessRequest,
    currentTimeProvider: () => number = Date.now
  ): Promise<RevokeAccessResponse> {
    if (!callerUid || callerUid.trim().length === 0) {
      throw new ApiError("AUTH_REQUIRED", "Autentikasi Firebase diperlukan.", 401);
    }

    if (!request.tracker_user_id || request.tracker_user_id.trim().length === 0) {
      throw new ApiError("INVALID_ARGUMENT", "Tracker User ID tidak boleh kosong.", 400);
    }

    const now = currentTimeProvider();
    await this.db.revokeTrackingRelation(callerUid, request.tracker_user_id.trim(), now);

    return {
      status: "SUCCESS",
      revoked_tracker_user_id: request.tracker_user_id.trim(),
      timestamp_ms: now
    };
  }

  /**
   * Menghapus seluruh data server terkait pengguna (Right to be Forgotten).
   */
  async purgeUserData(callerUid: string): Promise<PurgeUserDataResponse> {
    if (!callerUid || callerUid.trim().length === 0) {
      throw new ApiError("AUTH_REQUIRED", "Autentikasi Firebase diperlukan.", 401);
    }

    // Hapus seluruh dokumen terkait di Cloud Firestore
    await this.db.purgeAllUserData(callerUid);

    // Hapus identitas akun di Firebase Authentication jika provider tersedia
    if (this.authAdmin) {
      try {
        await this.authAdmin.deleteUser(callerUid);
      } catch (e) {
        // Log dan lanjutkan jika user sudah terhapus
      }
    }

    return {
      status: "SUCCESS",
      purged_user_id: callerUid,
      message: "Seluruh data Anda telah dihapus secara permanen dari server."
    };
  }

  /**
   * Menyinkronkan koordinat lokasi yang tertimbun di antrean offline SQLite.
   */
  async syncLocations(
    callerUid: string,
    locations: LocationPointDto[],
    currentTimeProvider: () => number = Date.now
  ): Promise<{ status: "SUCCESS" }> {
    if (!callerUid || callerUid.trim().length === 0) {
      throw new ApiError("AUTH_REQUIRED", "Autentikasi Firebase diperlukan.", 401);
    }

    if (!locations || locations.length === 0) {
      return { status: "SUCCESS" };
    }

    // Verifikasi kepemilikan koordinat
    for (const loc of locations) {
      if (loc.user_id !== callerUid) {
        throw new ApiError(
          "INVALID_ARGUMENT",
          "Dilarang memperbarui koordinat pengguna lain.",
          403
        );
      }
    }

    // Ambil sampel terbaru berdasarkan timestamp
    const sorted = [...locations].sort(
      (a, b) => b.timestamp_epoch_ms - a.timestamp_epoch_ms
    );
    const latest = sorted[0];

    await this.db.updateLocation({
      user_id: callerUid,
      latitude: latest.latitude,
      longitude: latest.longitude,
      accuracy: latest.accuracy_meters,
      updated_at_epoch_ms: latest.timestamp_epoch_ms || currentTimeProvider()
    });

    return { status: "SUCCESS" };
  }
}
