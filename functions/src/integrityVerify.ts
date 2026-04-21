import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import * as crypto from "crypto";
import { google } from "googleapis";

/**
 * Play Integrity API 토큰 검증.
 *
 * 흐름:
 *   1. 앱이 issueIntegrityNonce로 nonce 발급 → Play Integrity에 nonce 포함 토큰 요청.
 *   2. 앱이 integrityVerify로 토큰 전달 → 서버에서 Google에 검증 요청.
 *   3. deviceRecognitionVerdict, appRecognitionVerdict 확인 후 OK/차단 결정.
 */

export const issueIntegrityNonce = onCall(
  { region: "asia-northeast3" },
  async (req) => {
    const uid = req.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "로그인이 필요합니다");

    const nonce = crypto.randomBytes(24).toString("base64url");
    const db = admin.firestore();
    await db.collection("integrityNonces").doc(nonce).set({
      userId: uid,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      used: false,
    });
    return { nonce };
  },
);

export const integrityVerify = onCall(
  { region: "asia-northeast3", secrets: [] },
  async (req) => {
    const uid = req.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "로그인이 필요합니다");
    const token = req.data?.token as string | undefined;
    if (!token) throw new HttpsError("invalid-argument", "token이 필요합니다");

    const packageName = process.env.ANDROID_PACKAGE_NAME ?? "com.rewardapp";

    // Google API 인증 — Cloud Functions의 기본 서비스 계정 사용
    const auth = new google.auth.GoogleAuth({
      scopes: ["https://www.googleapis.com/auth/playintegrity"],
    });
    const playintegrity = google.playintegrity({ version: "v1", auth });

    const resp = await playintegrity.v1.decodeIntegrityToken({
      packageName,
      requestBody: { integrityToken: token },
    });

    const payload = resp.data.tokenPayloadExternal;
    const deviceOk = payload?.deviceIntegrity?.deviceRecognitionVerdict?.includes(
      "MEETS_DEVICE_INTEGRITY",
    );
    const appOk = payload?.appIntegrity?.appRecognitionVerdict === "PLAY_RECOGNIZED";
    const ok = Boolean(deviceOk && appOk);

    // 검증 실패 시 사용자 일시 정지로 마킹
    if (!ok) {
      await admin.firestore().collection("users").doc(uid).set(
        { status: "suspended", suspendReason: "integrity_failed" },
        { merge: true },
      );
    }

    return { ok, payload };
  },
);
