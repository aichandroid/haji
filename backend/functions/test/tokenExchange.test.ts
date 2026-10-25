import assert from "node:assert";
import { describe, it } from "node:test";
import { TokenService } from "../src/tokenService.js";
import { ApiError } from "../src/types.js";
import { FakeFirestoreDatabase } from "./fakeDatabase.js";

describe("Token Exchange Protocol (FR-TRACK-001 & FR-TRACK-002)", () => {
  it("generatePairingToken generates 5-minute valid token with correct QR URI", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);

    const now = 1779600000000;
    const response = await service.generatePairingToken(
      "usr_ayah_123",
      {},
      () => now
    );

    assert.strictEqual(response.status, "SUCCESS");
    assert.ok(response.token_id.startsWith("tok_"));
    assert.strictEqual(response.validity_seconds, 300);
    assert.strictEqual(response.expires_at_epoch_ms, now + 300000);
    assert.strictEqual(
      response.qr_payload,
      `hajj2027://pair?t=${response.token_id}&u=usr_ayah_123`
    );

    const savedDoc = await db.getPairingToken(response.token_id);
    assert.ok(savedDoc);
    assert.strictEqual(savedDoc.status, "PENDING");
    assert.strictEqual(savedDoc.target_uid, "usr_ayah_123");
  });

  it("generatePairingToken enforces rate limit of max 3 tokens per 15 minutes", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    // Generate 3 tokens within 15 minutes
    await service.generatePairingToken("usr_ayah_123", {}, () => now);
    await service.generatePairingToken("usr_ayah_123", {}, () => now + 60000);
    await service.generatePairingToken("usr_ayah_123", {}, () => now + 120000);

    // Ke-4 harus ditolak dengan kode RATE_LIMITED (429)
    await assert.rejects(
      async () => {
        await service.generatePairingToken("usr_ayah_123", {}, () => now + 180000);
      },
      (err: unknown) => {
        assert.ok(err instanceof ApiError);
        assert.strictEqual(err.code, "RATE_LIMITED");
        assert.strictEqual(err.httpStatus, 429);
        return true;
      }
    );
  });

  it("claimPairingToken successfully claims valid token and creates ACTIVE tracking relation", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    const tokenRes = await service.generatePairingToken("usr_ayah_123", {}, () => now);

    const claimRes = await service.claimPairingToken(
      "usr_fatimah_456",
      {
        token_id: tokenRes.token_id,
        tracker_label: "Fatimah (Anak)"
      },
      () => now + 30000 // 30 detik kemudian
    );

    assert.strictEqual(claimRes.status, "SUCCESS");
    assert.strictEqual(claimRes.relation_id, "rel_usr_ayah_123_usr_fatimah_456");
    assert.strictEqual(claimRes.target_user_id, "usr_ayah_123");

    // Token harus berubah status menjadi CLAIMED
    const updatedToken = await db.getPairingToken(tokenRes.token_id);
    assert.strictEqual(updatedToken?.status, "CLAIMED");

    // Relasi harus berstatus ACTIVE
    const relation = db.relations.get("rel_usr_ayah_123_usr_fatimah_456");
    assert.ok(relation);
    assert.strictEqual(relation.status, "ACTIVE");
    assert.strictEqual(relation.tracker_label, "Fatimah (Anak)");
  });

  it("claimPairingToken rejects already claimed token (anti-replay attack)", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    const tokenRes = await service.generatePairingToken("usr_ayah_123", {}, () => now);

    // Klaim pertama berhasil
    await service.claimPairingToken(
      "usr_fatimah_456",
      { token_id: tokenRes.token_id, tracker_label: "Anak" },
      () => now + 10000
    );

    // Klaim kedua (replay) harus ditolak
    await assert.rejects(
      async () => {
        await service.claimPairingToken(
          "usr_orang_asing_789",
          { token_id: tokenRes.token_id, tracker_label: "Penyusup" },
          () => now + 20000
        );
      },
      (err: unknown) => {
        assert.ok(err instanceof ApiError);
        assert.strictEqual(err.code, "TOKEN_ALREADY_USED");
        assert.strictEqual(err.httpStatus, 409);
        return true;
      }
    );
  });

  it("claimPairingToken rejects expired token (> 5 minutes)", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    const tokenRes = await service.generatePairingToken("usr_ayah_123", {}, () => now);

    // Dicoba klaim setelah 301 detik (> 300 detik)
    const lateTime = now + 301000;

    await assert.rejects(
      async () => {
        await service.claimPairingToken(
          "usr_fatimah_456",
          { token_id: tokenRes.token_id, tracker_label: "Anak" },
          () => lateTime
        );
      },
      (err: unknown) => {
        assert.ok(err instanceof ApiError);
        assert.strictEqual(err.code, "TOKEN_EXPIRED");
        assert.strictEqual(err.httpStatus, 410);
        return true;
      }
    );
  });

  it("claimPairingToken rejects self-pairing attempt", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    const tokenRes = await service.generatePairingToken("usr_ayah_123", {}, () => now);

    await assert.rejects(
      async () => {
        await service.claimPairingToken(
          "usr_ayah_123", // caller sama dengan target
          { token_id: tokenRes.token_id, tracker_label: "Saya Sendiri" },
          () => now + 10000
        );
      },
      (err: unknown) => {
        assert.ok(err instanceof ApiError);
        assert.strictEqual(err.code, "INVALID_ARGUMENT");
        return true;
      }
    );
  });

  it("revokeTrackingAccess marks relation as REVOKED immediately", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);
    const now = 1779600000000;

    const tokenRes = await service.generatePairingToken("usr_ayah_123", {}, () => now);
    await service.claimPairingToken(
      "usr_fatimah_456",
      { token_id: tokenRes.token_id, tracker_label: "Anak" },
      () => now + 5000
    );

    const revokeRes = await service.revokeTrackingAccess(
      "usr_ayah_123",
      { tracker_user_id: "usr_fatimah_456" },
      () => now + 60000
    );

    assert.strictEqual(revokeRes.status, "SUCCESS");
    assert.strictEqual(revokeRes.revoked_tracker_user_id, "usr_fatimah_456");

    const relation = db.relations.get("rel_usr_ayah_123_usr_fatimah_456");
    assert.strictEqual(relation?.status, "REVOKED");
    assert.strictEqual(relation?.revoked_at_epoch_ms, now + 60000);
  });

  it("syncLocations persists latest coordinate to Firestore", async () => {
    const db = new FakeFirestoreDatabase();
    const service = new TokenService(db);

    const result = await service.syncLocations("usr_ayah_123", [
      {
        user_id: "usr_ayah_123",
        latitude: 21.4225,
        longitude: 39.8262,
        accuracy_meters: 5.0,
        timestamp_epoch_ms: 1000
      },
      {
        user_id: "usr_ayah_123",
        latitude: 21.4230,
        longitude: 39.8265,
        accuracy_meters: 4.0,
        timestamp_epoch_ms: 2000
      }
    ]);

    assert.strictEqual(result.status, "SUCCESS");
    const loc = db.locations.get("usr_ayah_123");
    assert.ok(loc);
    assert.strictEqual(loc.latitude, 21.4230);
    assert.strictEqual(loc.accuracy, 4.0);
  });
});
