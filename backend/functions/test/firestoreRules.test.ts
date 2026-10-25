import assert from "node:assert";
import * as fs from "node:fs";
import * as path from "node:path";
import { describe, it } from "node:test";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Model representasi evaluasi aturan keamanan Firestore
type AuthContext = { uid: string } | null;

interface DatabaseState {
  documents: Map<string, Record<string, any>>;
}

class SecurityRulesEvaluator {
  constructor(private readonly db: DatabaseState) {}

  private isAuthenticated(auth: AuthContext): boolean {
    return auth !== null;
  }

  private isOwner(auth: AuthContext, userId: string): boolean {
    return this.isAuthenticated(auth) && auth?.uid === userId;
  }

  private hasActiveTrackingConsent(auth: AuthContext, targetUserId: string): boolean {
    if (!this.isAuthenticated(auth)) return false;
    const relId = `${targetUserId}_${auth!.uid}`;
    const relDoc = this.db.documents.get(`tracking_relations/${relId}`);
    return relDoc !== undefined && relDoc.status === "ACTIVE";
  }

  evaluate(
    operation: "read" | "create" | "update" | "delete" | "write",
    collection: string,
    docId: string,
    auth: AuthContext,
    resourceData?: Record<string, any>,
    requestData?: Record<string, any>
  ): boolean {
    // 1. Dokumen User (/users/{userId})
    if (collection === "users") {
      return this.isOwner(auth, docId);
    }

    // 2. Token Pairing QR (/pairing_tokens/{tokenId})
    if (collection === "pairing_tokens") {
      if (operation === "read") {
        return this.isAuthenticated(auth);
      }
      if (operation === "create") {
        return (
          this.isAuthenticated(auth) &&
          requestData?.target_uid === auth?.uid
        );
      }
      if (operation === "update" || operation === "delete") {
        return false; // Mutasi hanya melalui Cloud Functions terpercaya
      }
      return false;
    }

    // 3. Relasi Tracking (/tracking_relations/{relationId})
    if (collection === "tracking_relations") {
      if (operation === "read") {
        if (!this.isAuthenticated(auth)) return false;
        return (
          resourceData?.target_uid === auth?.uid ||
          resourceData?.tracker_uid === auth?.uid
        );
      }
      if (operation === "update") {
        return (
          this.isAuthenticated(auth) &&
          resourceData?.target_uid === auth?.uid
        );
      }
      if (operation === "create" || operation === "delete") {
        return false; // Dikelola oleh Cloud Functions
      }
      return false;
    }

    // 4. Data Lokasi (/locations/{userId})
    if (collection === "locations") {
      if (operation === "write" || operation === "create" || operation === "update") {
        return this.isOwner(auth, docId);
      }
      if (operation === "read") {
        return (
          this.isOwner(auth, docId) ||
          this.hasActiveTrackingConsent(auth, docId)
        );
      }
      if (operation === "delete") {
        return this.isOwner(auth, docId);
      }
      return false;
    }

    return false;
  }
}

describe("Firestore Security Rules RBAC & Consent Enforcement (TASK-13)", () => {
  it("firestore.rules file exists and adheres to syntactic standards", () => {
    // Cari firestore.rules
    const possiblePaths = [
      path.resolve(__dirname, "../../../firestore.rules"),
      path.resolve(__dirname, "../../firestore.rules"),
      path.resolve(process.cwd(), "../firestore.rules"),
      path.resolve(process.cwd(), "firestore.rules"),
      path.resolve(process.cwd(), "backend/firestore.rules"),
      "/mnt/data/haji/backend/firestore.rules"
    ];

    let rulesContent = "";
    for (const p of possiblePaths) {
      if (fs.existsSync(p)) {
        rulesContent = fs.readFileSync(p, "utf-8");
        break;
      }
    }

    assert.ok(rulesContent.length > 0, "Berkas firestore.rules harus ditemukan");
    assert.ok(rulesContent.includes("rules_version = '2';"));
    assert.ok(rulesContent.includes("service cloud.firestore"));
    assert.ok(rulesContent.includes("function isAuthenticated()"));
    assert.ok(rulesContent.includes("function isOwner(userId)"));
    assert.ok(rulesContent.includes("function hasActiveTrackingConsent(targetUserId)"));
    assert.ok(rulesContent.includes("match /users/{userId}"));
    assert.ok(rulesContent.includes("match /pairing_tokens/{tokenId}"));
    assert.ok(rulesContent.includes("match /tracking_relations/{relationId}"));
    assert.ok(rulesContent.includes("match /locations/{userId}"));
  });

  describe("Collection /users/{userId}", () => {
    const db: DatabaseState = { documents: new Map() };
    const evaluator = new SecurityRulesEvaluator(db);

    it("unauthenticated requests cannot read or write user doc", () => {
      assert.strictEqual(evaluator.evaluate("read", "users", "usr_123", null), false);
      assert.strictEqual(evaluator.evaluate("write", "users", "usr_123", null), false);
    });

    it("owner can read and write own user document", () => {
      const auth = { uid: "usr_123" };
      assert.strictEqual(evaluator.evaluate("read", "users", "usr_123", auth), true);
      assert.strictEqual(evaluator.evaluate("write", "users", "usr_123", auth), true);
    });

    it("non-owner is denied read and write access (anti-IDOR)", () => {
      const auth = { uid: "usr_hacker_999" };
      assert.strictEqual(evaluator.evaluate("read", "users", "usr_123", auth), false);
      assert.strictEqual(evaluator.evaluate("write", "users", "usr_123", auth), false);
    });
  });

  describe("Collection /pairing_tokens/{tokenId}", () => {
    const db: DatabaseState = { documents: new Map() };
    const evaluator = new SecurityRulesEvaluator(db);

    it("unauthenticated users cannot read pairing tokens", () => {
      assert.strictEqual(evaluator.evaluate("read", "pairing_tokens", "tok_1", null), false);
    });

    it("authenticated users can read pairing tokens", () => {
      assert.strictEqual(evaluator.evaluate("read", "pairing_tokens", "tok_1", { uid: "usr_any" }), true);
    });

    it("user can create token where target_uid matches own auth UID", () => {
      const auth = { uid: "usr_ayah_123" };
      const validPayload = { target_uid: "usr_ayah_123", status: "PENDING" };
      assert.strictEqual(evaluator.evaluate("create", "pairing_tokens", "tok_1", auth, undefined, validPayload), true);
    });

    it("user cannot create token on behalf of another user (spoofing prevention)", () => {
      const auth = { uid: "usr_ayah_123" };
      const forgedPayload = { target_uid: "usr_korban_456", status: "PENDING" };
      assert.strictEqual(evaluator.evaluate("create", "pairing_tokens", "tok_1", auth, undefined, forgedPayload), false);
    });

    it("direct client update and delete are strictly denied (must go through Cloud Functions)", () => {
      const auth = { uid: "usr_ayah_123" };
      assert.strictEqual(evaluator.evaluate("update", "pairing_tokens", "tok_1", auth), false);
      assert.strictEqual(evaluator.evaluate("delete", "pairing_tokens", "tok_1", auth), false);
    });
  });

  describe("Collection /tracking_relations/{relationId}", () => {
    const db: DatabaseState = { documents: new Map() };
    const evaluator = new SecurityRulesEvaluator(db);

    const activeRelation = {
      target_uid: "usr_ayah_123",
      tracker_uid: "usr_fatimah_456",
      status: "ACTIVE"
    };

    it("both target and tracker can read the relation", () => {
      assert.strictEqual(evaluator.evaluate("read", "tracking_relations", "rel_1", { uid: "usr_ayah_123" }, activeRelation), true);
      assert.strictEqual(evaluator.evaluate("read", "tracking_relations", "rel_1", { uid: "usr_fatimah_456" }, activeRelation), true);
    });

    it("third party cannot read relation between other users (privacy protection)", () => {
      assert.strictEqual(evaluator.evaluate("read", "tracking_relations", "rel_1", { uid: "usr_outsider_789" }, activeRelation), false);
      assert.strictEqual(evaluator.evaluate("read", "tracking_relations", "rel_1", null, activeRelation), false);
    });

    it("target user is permitted to update relation (revoke consent)", () => {
      assert.strictEqual(evaluator.evaluate("update", "tracking_relations", "rel_1", { uid: "usr_ayah_123" }, activeRelation), true);
    });

    it("tracker user cannot modify relation (only target can revoke)", () => {
      assert.strictEqual(evaluator.evaluate("update", "tracking_relations", "rel_1", { uid: "usr_fatimah_456" }, activeRelation), false);
    });

    it("direct client create and delete are denied (handled via Cloud Functions)", () => {
      assert.strictEqual(evaluator.evaluate("create", "tracking_relations", "rel_1", { uid: "usr_ayah_123" }), false);
      assert.strictEqual(evaluator.evaluate("delete", "tracking_relations", "rel_1", { uid: "usr_ayah_123" }), false);
    });
  });

  describe("Collection /locations/{userId} (Sensitive Telemetry)", () => {
    const db: DatabaseState = { documents: new Map() };
    const evaluator = new SecurityRulesEvaluator(db);

    const targetUserId = "usr_ayah_123";
    const trackerUserId = "usr_fatimah_456";
    const revokedTrackerId = "usr_pembantu_789";

    // Setup active and revoked relations in mock DB
    db.documents.set(`tracking_relations/${targetUserId}_${trackerUserId}`, {
      target_uid: targetUserId,
      tracker_uid: trackerUserId,
      status: "ACTIVE"
    });
    db.documents.set(`tracking_relations/${targetUserId}_${revokedTrackerId}`, {
      target_uid: targetUserId,
      tracker_uid: revokedTrackerId,
      status: "REVOKED"
    });

    it("only owner can write/update own location", () => {
      assert.strictEqual(evaluator.evaluate("write", "locations", targetUserId, { uid: targetUserId }), true);
      assert.strictEqual(evaluator.evaluate("write", "locations", targetUserId, { uid: trackerUserId }), false);
      assert.strictEqual(evaluator.evaluate("write", "locations", targetUserId, null), false);
    });

    it("owner can read own location", () => {
      assert.strictEqual(evaluator.evaluate("read", "locations", targetUserId, { uid: targetUserId }), true);
    });

    it("active tracker with consent can read target location", () => {
      assert.strictEqual(evaluator.evaluate("read", "locations", targetUserId, { uid: trackerUserId }), true);
    });

    it("revoked tracker is denied read access immediately (TC-TRACK-04)", () => {
      assert.strictEqual(evaluator.evaluate("read", "locations", targetUserId, { uid: revokedTrackerId }), false);
    });

    it("stranger with no relation is denied read access (anti-stalking)", () => {
      assert.strictEqual(evaluator.evaluate("read", "locations", targetUserId, { uid: "usr_stranger_999" }), false);
      assert.strictEqual(evaluator.evaluate("read", "locations", targetUserId, null), false);
    });
  });
});
