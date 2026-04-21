import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export const requestExchange = onCall({ region: "asia-northeast3" }, async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "로그인이 필요합니다");

  const itemId = (req.data?.itemId as string | undefined)?.trim();
  if (!itemId) throw new HttpsError("invalid-argument", "itemId가 필요합니다");

  const db = admin.firestore();
  const userRef = db.collection("users").doc(uid);
  const itemRef = db.collection("gifticons").doc(itemId);

  return await db.runTransaction(async (tx) => {
    const [userSnap, itemSnap] = await Promise.all([tx.get(userRef), tx.get(itemRef)]);
    const user = userSnap.data();
    const item = itemSnap.data();

    if (!user) throw new HttpsError("failed-precondition", "사용자 정보가 없습니다");
    if (!item) throw new HttpsError("not-found", "기프티콘을 찾을 수 없습니다");

    if (user.status === "suspended") {
      throw new HttpsError("permission-denied", "정지된 계정입니다");
    }

    const required = item.pointsRequired as number;
    const currentPoints = (user.points as number | undefined) ?? 0;
    if (currentPoints < required) {
      throw new HttpsError("failed-precondition", "포인트가 부족합니다");
    }

    const wdRef = db.collection("withdrawals").doc();
    tx.set(wdRef, {
      userId: uid,
      type: "gifticon",
      itemId,
      itemName: item.name,
      pointsUsed: required,
      status: "pending",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    tx.update(userRef, {
      points: admin.firestore.FieldValue.increment(-required),
    });

    tx.set(db.collection("pointTransactions").doc(), {
      userId: uid,
      type: "withdrawal",
      amount: -required,
      refId: wdRef.id,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { withdrawalId: wdRef.id, remainingPoints: currentPoints - required };
  });
});
