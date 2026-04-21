import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

export const checkAttendance = onCall({ region: "asia-northeast3" }, async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "로그인이 필요합니다");

  const db = admin.firestore();
  const userRef = db.collection("users").doc(uid);

  return await db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    const data = snap.data() ?? {};
    const last = (data.lastAttendance as admin.firestore.Timestamp | undefined)?.toDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (last && last >= today) {
      throw new HttpsError("already-exists", "이미 출석했습니다");
    }

    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    const currentStreak = (data.streak as number | undefined) ?? 0;
    const newStreak = last && last >= yesterday ? currentStreak + 1 : 1;
    const reward = newStreak >= 7 ? 100 : 30;

    tx.set(
      userRef,
      {
        points: admin.firestore.FieldValue.increment(reward),
        streak: newStreak,
        lastAttendance: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true },
    );

    tx.set(db.collection("pointTransactions").doc(), {
      userId: uid,
      type: "attendance",
      amount: reward,
      refId: `attend_${today.toISOString().slice(0, 10)}`,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { reward, streak: newStreak };
  });
});
