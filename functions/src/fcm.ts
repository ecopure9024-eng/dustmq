import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export const saveFcmToken = onCall({ region: "asia-northeast3" }, async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "로그인이 필요합니다");

  const token = req.data?.token;
  if (typeof token !== "string" || token.length === 0) {
    throw new HttpsError("invalid-argument", "token 필수");
  }

  await admin.firestore().collection("users").doc(uid).set(
    {
      fcmToken: token,
      fcmTokenUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true },
  );

  return { ok: true };
});
