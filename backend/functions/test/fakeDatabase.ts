import { FirestoreDatabase } from "../src/database.js";
import {
  ApiError,
  PairingTokenDoc,
  TrackingRelationDoc,
  UserLocationDoc
} from "../src/types.js";

export class FakeFirestoreDatabase implements FirestoreDatabase {
  public tokens: Map<string, PairingTokenDoc> = new Map();
  public relations: Map<string, TrackingRelationDoc> = new Map();
  public locations: Map<string, UserLocationDoc> = new Map();
  public users: Set<string> = new Set();

  async getPairingToken(tokenId: string): Promise<PairingTokenDoc | null> {
    return this.tokens.get(tokenId) || null;
  }

  async countRecentTokensByUser(targetUid: string, sinceEpochMs: number): Promise<number> {
    let count = 0;
    for (const doc of this.tokens.values()) {
      if (doc.target_uid === targetUid && doc.created_at >= sinceEpochMs) {
        count++;
      }
    }
    return count;
  }

  async savePairingToken(tokenId: string, doc: PairingTokenDoc): Promise<void> {
    this.tokens.set(tokenId, { ...doc });
  }

  async claimTokenTransaction(
    tokenId: string,
    trackerUid: string,
    trackerLabel: string,
    now: number
  ): Promise<{ relationId: string; targetUid: string }> {
    const token = this.tokens.get(tokenId);
    if (!token) {
      throw new ApiError(
        "TOKEN_EXPIRED",
        "Kode QR tidak ditemukan atau telah kedaluwarsa.",
        410
      );
    }

    if (token.status === "CLAIMED") {
      throw new ApiError(
        "TOKEN_ALREADY_USED",
        "Token telah diklaim pihak lain. Silakan minta kerabat membuat kode QR baru.",
        409
      );
    }

    if (token.status !== "PENDING" || now > token.expires_at) {
      token.status = "EXPIRED";
      throw new ApiError(
        "TOKEN_EXPIRED",
        "Kode QR telah kedaluwarsa (> 5 menit). Silakan minta kerabat menampilkan kode QR baru.",
        410
      );
    }

    if (token.target_uid === trackerUid) {
      throw new ApiError(
        "INVALID_ARGUMENT",
        "Tidak dapat memasangkan pelacakan dengan diri sendiri.",
        400
      );
    }

    token.status = "CLAIMED";
    const relationId = `rel_${token.target_uid}_${trackerUid}`;

    this.relations.set(relationId, {
      target_uid: token.target_uid,
      tracker_uid: trackerUid,
      tracker_label: trackerLabel.trim(),
      status: "ACTIVE",
      paired_at_epoch_ms: now,
      expires_at_epoch_ms: 1815000000000
    });

    return { relationId, targetUid: token.target_uid };
  }

  async revokeTrackingRelation(targetUid: string, trackerUid: string, now: number): Promise<void> {
    const relationId = `rel_${targetUid}_${trackerUid}`;
    const rel = this.relations.get(relationId);
    if (!rel) {
      throw new ApiError(
        "INVALID_ARGUMENT",
        "Relasi pelacakan tidak ditemukan.",
        404
      );
    }

    if (rel.target_uid !== targetUid) {
      throw new ApiError(
        "TRACKING_ACCESS_REVOKED",
        "Anda tidak memiliki hak untuk mencabut relasi ini.",
        403
      );
    }

    rel.status = "REVOKED";
    rel.revoked_at_epoch_ms = now;
  }

  async purgeAllUserData(userId: string): Promise<void> {
    this.users.delete(userId);
    this.locations.delete(userId);

    for (const [key, token] of this.tokens.entries()) {
      if (token.target_uid === userId) {
        this.tokens.delete(key);
      }
    }

    for (const [key, rel] of this.relations.entries()) {
      if (rel.target_uid === userId || rel.tracker_uid === userId) {
        this.relations.delete(key);
      }
    }
  }

  async updateLocation(location: UserLocationDoc): Promise<void> {
    this.locations.set(location.user_id, { ...location });
  }
}
