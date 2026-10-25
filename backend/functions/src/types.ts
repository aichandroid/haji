/**
 * Tipe data & kontrak API untuk layanan pertukaran token dan pelacakan keluarga.
 * Selaras dengan Bagian 20 & 21 SYSTEM_DESIGN_DOCUMENT.md
 */

export type ApiErrorCode =
  | "AUTH_REQUIRED"
  | "TOKEN_EXPIRED"
  | "TOKEN_ALREADY_USED"
  | "TRACKING_ACCESS_REVOKED"
  | "RATE_LIMITED"
  | "NETWORK_UNAVAILABLE"
  | "INVALID_ARGUMENT"
  | "INTERNAL_ERROR";

export class ApiError extends Error {
  constructor(
    public readonly code: ApiErrorCode,
    message: string,
    public readonly httpStatus: number = 400
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export interface PairingTokenDoc {
  target_uid: string;
  expires_at: number; // Epoch ms (TTL: 5 menit / 300.000 ms)
  status: "PENDING" | "CLAIMED" | "EXPIRED";
  created_at: number; // Epoch ms
}

export interface TrackingRelationDoc {
  target_uid: string;
  tracker_uid: string;
  tracker_label: string;
  status: "ACTIVE" | "REVOKED";
  paired_at_epoch_ms: number;
  expires_at_epoch_ms: number;
  revoked_at_epoch_ms?: number;
}

export interface UserLocationDoc {
  user_id: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  updated_at_epoch_ms: number;
}

export interface GenerateTokenRequest {
  client_timestamp_ms?: number;
}

export interface GenerateTokenResponse {
  status: "SUCCESS";
  token_id: string;
  qr_payload: string;
  expires_at_epoch_ms: number;
  validity_seconds: number;
}

export interface ClaimTokenRequest {
  token_id: string;
  tracker_label: string;
}

export interface ClaimTokenResponse {
  status: "SUCCESS";
  relation_id: string;
  target_user_id: string;
  paired_at_epoch_ms: number;
  message: string;
}

export interface RevokeAccessRequest {
  tracker_user_id: string;
}

export interface RevokeAccessResponse {
  status: "SUCCESS";
  revoked_tracker_user_id: string;
  timestamp_ms: number;
}

export interface PurgeUserDataResponse {
  status: "SUCCESS";
  purged_user_id: string;
  message: string;
}

export interface LocationPointDto {
  user_id: string;
  latitude: number;
  longitude: number;
  accuracy_meters: number;
  timestamp_epoch_ms: number;
}

export interface LocationSyncRequest {
  locations: LocationPointDto[];
}
