import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import axios from "axios";

/**
 * 카카오 access token을 검증하고 Firebase 커스텀 토큰을 발급한다.
 * 앱은 카카오 로그인 → 받은 access token을 이 함수에 전달 → 커스텀 토큰으로 Firebase 로그인.
 */
export const kakaoAuthExchange = onCall(
  { region: "asia-northeast3" },
  async (req) => {
    const accessToken = req.data?.accessToken as string | undefined;
    if (!accessToken) throw new HttpsError("invalid-argument", "accessToken이 필요합니다");

    // 카카오 사용자 정보 조회
    const { data } = await axios.get("https://kapi.kakao.com/v2/user/me", {
      headers: { Authorization: `Bearer ${accessToken}` },
    });

    const kakaoId = String(data.id);
    const nickname = data.properties?.nickname ?? "";
    const uid = `kakao:${kakaoId}`;

    const db = admin.firestore();
    const userRef = db.collection("users").doc(uid);
    const snap = await userRef.get();
    if (!snap.exists) {
      await userRef.set({
        nickname,
        points: 0,
        streak: 0,
        status: "active",
        provider: "kakao",
        kakaoId,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    const firebaseToken = await admin.auth().createCustomToken(uid, { provider: "kakao" });
    return { firebaseToken };
  },
);
