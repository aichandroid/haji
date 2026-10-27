import assert from "node:assert";
import { describe, it } from "node:test";
import { PostHajjCleanupService } from "../src/postHajjCleanup.js";
import { FakeFirestoreDatabase } from "./fakeDatabase.js";

describe("Post-Hajj Mass Purge & Zero Data Liability (Roadmap Fase 7 & UU PDP)", () => {
  it("executeMassPurge cleanly deletes all locations, relations, and tokens across all pilgrims", async () => {
    const db = new FakeFirestoreDatabase();

    // Populate multiple dummy locations
    db.locations.set("usr_1", {
      user_id: "usr_1",
      latitude: 21.4225,
      longitude: 39.8262,
      accuracy: 8.0,
      updated_at_epoch_ms: 1000
    });
    db.locations.set("usr_2", {
      user_id: "usr_2",
      latitude: 21.3548,
      longitude: 39.9841,
      accuracy: 6.5,
      updated_at_epoch_ms: 1200
    });

    // Populate dummy pairing tokens
    db.tokens.set("tok_1", {
      target_uid: "usr_1",
      expires_at: 2000,
      created_at: 1000,
      status: "CLAIMED"
    });
    db.tokens.set("tok_2", {
      target_uid: "usr_2",
      expires_at: 2500,
      created_at: 1500,
      status: "PENDING"
    });

    // Populate dummy relations
    db.relations.set("rel_1", {
      target_uid: "usr_1",
      tracker_uid: "usr_tracker",
      tracker_label: "Anak",
      status: "ACTIVE",
      paired_at_epoch_ms: 1100,
      expires_at_epoch_ms: 86400000
    });

    assert.strictEqual(db.locations.size, 2);
    assert.strictEqual(db.tokens.size, 2);
    assert.strictEqual(db.relations.size, 1);

    const cleanupService = new PostHajjCleanupService(db);
    const summary = await cleanupService.executeMassPurge(5000);

    // Verifikasi hasil pembersihan
    assert.strictEqual(summary.locationsPurged, 2);
    assert.strictEqual(summary.tokensPurged, 2);
    assert.strictEqual(summary.relationsPurged, 1);
    assert.strictEqual(summary.timestamp, 5000);

    // Basis data telemetri harus bersih total (0 entri)
    assert.strictEqual(db.locations.size, 0, "Koleksi locations harus kosong");
    assert.strictEqual(db.tokens.size, 0, "Koleksi pairing_tokens harus kosong");
    assert.strictEqual(db.relations.size, 0, "Koleksi tracking_relations harus kosong");
  });
});
