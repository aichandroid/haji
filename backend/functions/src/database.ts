import * as admin from "firebase-admin";
import {
  ApiError,
  PairingTokenDoc,
  TrackingRelationDoc,
  UserLocationDoc
} from "./types.js";

/**
 * Kontrak operasi Firestore untuk pertukaran token dan pengelolaan relasi pelacakan.
 */
export interface FirestoreDatabase {
  getPairingToken(tokenId: string): Promise<PairingTokenDoc | null>;
  countRecentTokensByUser(targetUid: string, sinceEpochMs: number): Promise<number>;
  savePairingToken(tokenId: string, doc: PairingTokenDoc): Promise<void>;
  claimTokenTransaction(
    tokenId: string,
    trackerUid: string,
    trackerLabel: string,
    now: number
  ): Promise<{ relationId: string; targetUid: string }>;
  revokeTrackingRelation(targetUid: string, trackerUid: string, now: number): Promise<void>;
  purgeAllUserData(userId: string): Promise<void>;
  updateLocation(location: UserLocationDoc): Promise<void>;
}

/**
 * Implementasi produksi menggunakan Google Cloud Firestore SDK.
 */
export class AdminFirestoreDatabase implements FirestoreDatabase {
  constructor(private readonly db: admin.firestore.Firestore) {}

  async getPairingToken(tokenId: string): Promise<PairingTokenDoc | null> {
    const snap = await this.db.collection("pairing_tokens").doc(tokenId).get();
    if (!snap.exists) return null;
    return snap.data() as PairingTokenDoc;
  }

  async countRecentTokensByUser(targetUid: string, sinceEpochMs: number): Promise<number> {
    const snapshot = await this.db
      .collection("pairing_tokens")
      .where("target_uid", "==", targetUid)
      .where("created_at", ">=", sinceEpochMs)
      .count()
      .get();
    return snapshot.data().count;
  }

  async savePairingToken(tokenId: string, doc: PairingTokenDoc): Promise<void> {
    await this.db.collection("pairing_tokens").doc(tokenId).set(doc);
  }

  async claimTokenTransaction(
    tokenId: string,
    trackerUid: string,
    trackerLabel: string,
    now: number
  ): Promise<{ relationId: string; targetUid: string }> {
    const tokenRef = this.db.collection("pairing_tokens").doc(tokenId);

    return await this.db.runTransaction(async (transaction) => {
      const tokenDoc = await transaction.get(tokenRef);
      if (!tokenDoc.exists) {
        throw new ApiError(
          "TOKEN_EXPIRED",
          "Kode QR tidak ditemukan atau telah kedaluwarsa.",
          410
        );
      }

      const data = tokenDoc.data() as PairingTokenDoc;

      // 1. Anti-Replay: Cegah token yang sudah pernah diklaim
      if (data.status === "CLAIMED") {
        throw new ApiError(
          "TOKEN_ALREADY_USED",
          "Token telah diklaim pihak lain. Silakan minta kerabat membuat kode QR baru.",
          409
        );
      }

      // 2. Cegah token kedaluwarsa (> 5 menit)
      if (data.status !== "PENDING" || now > data.expires_at) {
        transaction.update(tokenRef, { status: "EXPIRED" });
        throw new ApiError(
          "TOKEN_EXPIRED",
          "Kode QR telah kedaluwarsa (> 5 menit). Silakan minta kerabat menampilkan kode QR baru.",
          410
        );
      }

      // 3. Cegah melacak diri sendiri
      if (data.target_uid === trackerUid) {
        throw new ApiError(
          "INVALID_ARGUMENT",
          "Tidak dapat memasangkan pelacakan dengan diri sendiri.",
          400
        );
      }

      // 4. Update status token menjadi CLAIMED secara atomik
      transaction.update(tokenRef, { status: "CLAIMED" });

      // 5. Buat entri relasi pelacakan aktif (targetUid_trackerUid)
      const relationId = `rel_${data.target_uid}_${trackerUid}`;
      const relationRef = this.db.collection("tracking_relations").doc(relationId);

      // Berlaku sampai akhir musim haji 2027 (Juli 2027)
      const hajjSeasonEndMs = 1815000000000;

      const relationData: TrackingRelationDoc = {
        target_uid: data.target_uid,
        tracker_uid: trackerUid,
        tracker_label: trackerLabel.trim(),
        status: "ACTIVE",
        paired_at_epoch_ms: now,
        expires_at_epoch_ms: hajjSeasonEndMs
      };

      transaction.set(relationRef, relationData);

      return {
        relationId,
        targetUid: data.target_uid
      };
    });
  }

  async revokeTrackingRelation(targetUid: string, trackerUid: string, now: number): Promise<void> {
    const relationId = `rel_${targetUid}_${trackerUid}`;
    const relationRef = this.db.collection("tracking_relations").doc(relationId);
    const snap = await relationRef.get();

    if (!snap.exists) {
      throw new ApiError(
        "INVALID_ARGUMENT",
        "Relasi pelacakan tidak ditemukan.",
        404
      );
    }

    const data = snap.data() as TrackingRelationDoc;
    if (data.target_uid !== targetUid) {
      throw new ApiError(
        "TRACKING_ACCESS_REVOKED",
        "Anda tidak memiliki hak untuk mencabut relasi ini.",
        403
      );
    }

    await relationRef.update({
      status: "REVOKED",
      revoked_at_epoch_ms: now
    });
  }

  async purgeAllUserData(userId: string): Promise<void> {
    const batch = this.db.batch();

    // 1. Hapus dokumen profil pengguna
    batch.delete(this.db.collection("users").doc(userId));

    // 2. Hapus koordinat lokasi
    batch.delete(this.db.collection("locations").doc(userId));

    // 3. Hapus seluruh pairing token milik user
    const tokensSnap = await this.db
      .collection("pairing_tokens")
      .where("target_uid", "==", userId)
      .get();
    tokensSnap.docs.forEach((doc) => batch.delete(doc.ref));

    // 4. Hapus relasi di mana user adalah target
    const targetRelSnap = await this.db
      .collection("tracking_relations")
      .where("target_uid", "==", userId)
      .get();
    targetRelSnap.docs.forEach((doc) => batch.delete(doc.ref));

    // 5. Hapus relasi di mana user adalah pelacak
    const trackerRelSnap = await this.db
      .collection("tracking_relations")
      .where("tracker_uid", "==", userId)
      .get();
    trackerRelSnap.docs.forEach((doc) => batch.delete(doc.ref));

    await batch.commit();
  }

  async updateLocation(location: UserLocationDoc): Promise<void> {
    await this.db
      .collection("locations")
      .doc(location.user_id)
      .set(location);
  }
}
