import { FirestoreDatabase } from "./database.js";
import { AuthAdmin } from "./tokenService.js";

export interface MassPurgeSummary {
  locationsPurged: number;
  relationsPurged: number;
  tokensPurged: number;
  usersPurged: number;
  timestamp: number;
}

/**
 * Layanan pembersihan massal pasca-musim haji (Roadmap Fase 7 & Zero Data Liability).
 * Menghapus seluruh data sensitif telemetri jemaah setelah musim haji 1448 H selesai.
 */
export class PostHajjCleanupService {
  constructor(
    private readonly db: FirestoreDatabase,
    private readonly auth?: AuthAdmin
  ) {}

  /**
   * Mengeksekusi pembersihan massal seluruh telemetri lokasi, token, dan relasi pelacakan.
   */
  async executeMassPurge(now: number = Date.now()): Promise<MassPurgeSummary> {
    const telemetryResult = await this.db.purgeAllTelemetry();

    return {
      locationsPurged: telemetryResult.locationsCount,
      relationsPurged: telemetryResult.relationsCount,
      tokensPurged: telemetryResult.tokensCount,
      usersPurged: 0,
      timestamp: now
    };
  }
}
