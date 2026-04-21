import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v2";
import * as logger from "firebase-functions/logger";
import axios from "axios";
import * as crypto from "crypto";

/**
 * AdMob SSV (Server-Side Verification) 엔드포인트.
 *
 * 흐름:
 *   1. AdMob이 쿼리스트링으로 서명된 파라미터를 GET 요청으로 보낸다.
 *   2. Google 공개키(키 ID로 식별)를 가져와 ECDSA 서명을 검증한다.
 *   3. transaction_id 기반 중복 지급 차단.
 *   4. Firestore 트랜잭션으로 users.points 증가 + pointTransactions 로그.
 *   5. adViews 로그 기록.
 *
 * 중요: 반드시 HTTPS로 공개되어야 하고, AdMob 콘솔에서 이 함수 URL을
 * 광고 단위별 SSV 콜백 URL로 등록해야 한다.
 *
 * 참고: https://developers.google.com/admob/android/ssv
 */

const GOOGLE_KEYS_URL = "https://www.gstatic.com/admob/reward/verifier-keys.json";

// 공개키 캐시 — 메모리에 싱글턴으로 보관 (함수 인스턴스 유지 시 재사용)
let cachedKeys: { [keyId: string]: string } | null = null;
let cachedAt = 0;
const KEYS_TTL_MS = 60 * 60 * 1000; // 1시간

async function fetchVerifierKeys(): Promise<{ [keyId: string]: string }> {
  if (cachedKeys && Date.now() - cachedAt < KEYS_TTL_MS) return cachedKeys;
  const { data } = await axios.get(GOOGLE_KEYS_URL);
  const map: { [k: string]: string } = {};
  for (const k of data.keys) {
    map[String(k.keyId)] = k.pem;
  }
  cachedKeys = map;
  cachedAt = Date.now();
  return map;
}

/**
 * AdMob SSV 서명 검증 로직.
 *
 * AdMob은 `signature`와 `key_id`를 *제외한* 나머지 쿼리 파라미터를
 * 원본 URL에 나타난 순서대로 연결한 문자열에 ECDSA(secp256r1 + SHA-256) 서명을 만든다.
 */
function verifySignature(rawQuery: string, signatureB64: string, pem: string): boolean {
  // signature와 key_id 앞의 `&`부터 쿼리 끝까지 잘라낸다.
  const sigIdx = rawQuery.indexOf("&signature=");
  if (sigIdx < 0) return false;
  const message = rawQuery.substring(0, sigIdx);

  // AdMob SSV 서명은 base64url 인코딩
  const signature = Buffer.from(signatureB64.replace(/-/g, "+").replace(/_/g, "/"), "base64");

  const verifier = crypto.createVerify("sha256");
  verifier.update(message);
  verifier.end();
  return verifier.verify(pem, signature);
}

export const admobSsv = functions.https.onRequest(
  { region: "asia-northeast3", cors: false },
  async (req, res) => {
    try {
      const {
        ad_network,
        ad_unit,
        reward_amount,
        reward_item,
        timestamp,
        transaction_id,
        user_id,
        custom_data,
        signature,
        key_id,
      } = req.query as Record<string, string>;

      if (!transaction_id || !user_id || !signature || !key_id || !reward_amount) {
        logger.warn("SSV missing params", req.query);
        res.status(400).send("bad_request");
        return;
      }

      // 1. 공개키 로드 및 서명 검증
      const keys = await fetchVerifierKeys();
      const pem = keys[key_id];
      if (!pem) {
        logger.warn("unknown key_id", { key_id });
        res.status(400).send("unknown_key");
        return;
      }

      const rawQuery = req.url.split("?")[1] ?? "";
      if (!verifySignature(rawQuery, signature, pem)) {
        logger.warn("signature mismatch", { transaction_id });
        res.status(400).send("invalid_signature");
        return;
      }

      // 2. 중복 지급 차단 + Firestore 트랜잭션으로 포인트 증가
      const db = admin.firestore();
      const adViewRef = db.collection("adViews").doc(transaction_id);
      const userRef = db.collection("users").doc(user_id);

      const dailyCap = 20;
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await db.runTransaction(async (tx) => {
        const existing = await tx.get(adViewRef);
        if (existing.exists) {
          throw new Error("duplicate_transaction");
        }

        // 일일 시청 한도 체크
        const dailyQuerySnap = await db
          .collection("adViews")
          .where("userId", "==", user_id)
          .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(today))
          .count()
          .get();
        if (dailyQuerySnap.data().count >= dailyCap) {
          throw new Error("daily_cap_exceeded");
        }

        const amount = parseInt(reward_amount, 10);

        tx.set(adViewRef, {
          userId: user_id,
          adUnitId: ad_unit ?? "",
          adNetwork: ad_network ?? "",
          rewardAmount: amount,
          rewardItem: reward_item ?? "",
          ssvVerified: true,
          customData: custom_data ?? "",
          timestamp: timestamp ?? "",
          ip: req.ip ?? "",
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        tx.update(userRef, {
          points: admin.firestore.FieldValue.increment(amount),
        });

        tx.set(db.collection("pointTransactions").doc(), {
          userId: user_id,
          type: "ad_reward",
          amount,
          refId: transaction_id,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      });

      res.status(200).send("OK");
    } catch (err: any) {
      const msg = err?.message ?? "error";
      if (msg === "duplicate_transaction") {
        // AdMob은 2xx를 받아야 재시도를 멈춘다. 이미 처리된 건은 성공으로 응답.
        res.status(200).send("OK");
        return;
      }
      logger.error("admobSsv failed", err);
      res.status(500).send(msg);
    }
  },
);
