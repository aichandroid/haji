import assert from "node:assert";
import { describe, it } from "node:test";
import { AuthAdmin, TokenService } from "../src/tokenService.js";
import { FakeFirestoreDatabase } from "./fakeDatabase.js";

describe("Right to be Forgotten (FR-DIST-004 & UU PDP No. 27/2022)", () => {
  it("purgeUserData cleanly wipes all Firestore documents and deletes Auth user", async () => {
    const db = new FakeFirestoreDatabase();
    let deletedAuthUid: string | null = null;

    const mockAuthAdmin: AuthAdmin = {
      deleteUser: async (uid: string) => {
        deletedAuthUid = uid;
      }
    };

    const service = new TokenService(db, mockAuthAdmin);

    // Setup dummy user data
    const userId = "usr_ayah_123";
    db.users.add(userId);
    db.locations.set(userId, {
      user_id: userId,
      latitude: 21.42,
      longitude: 39.82,
      accuracy: 5.0,
      updated_at_epoch_ms: 1000
    });

    // Setup active token
    await service.generatePairingToken(userId, {}, () => 1000);

    // Setup active tracking relation
    await service.claimPairingToken(
      "usr_fatimah_456",
      {
        token_id: Array.from(db.tokens.keys())[0],
        tracker_label: "Anak"
      },
      () => 1500
    );

    assert.strictEqual(db.tokens.size, 1);
    assert.strictEqual(db.relations.size, 1);
    assert.strictEqual(db.locations.size, 1);

    // Eksekusi purgeUserData
    const purgeResponse = await service.purgeUserData(userId);

    assert.strictEqual(purgeResponse.status, "SUCCESS");
    assert.strictEqual(purgeResponse.purged_user_id, userId);

    // Verifikasi data bersih total
    assert.strictEqual(db.tokens.size, 0, "Pairing tokens harus kosong");
    assert.strictEqual(db.relations.size, 0, "Tracking relations harus kosong");
    assert.strictEqual(db.locations.size, 0, "Location telemetry harus terhapus");
    assert.strictEqual(db.users.has(userId), false, "User doc harus terhapus");
    assert.strictEqual(deletedAuthUid, userId, "Auth account harus dihapus di Firebase Auth");
  });
});
