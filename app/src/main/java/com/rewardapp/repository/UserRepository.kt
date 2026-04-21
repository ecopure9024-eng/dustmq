package com.rewardapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rewardapp.data.model.PointTransaction
import com.rewardapp.data.model.UserDoc
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) {
    val uid: String? get() = auth.currentUser?.uid

    fun observeUser(): Flow<UserDoc?> = callbackFlow {
        val userId = uid ?: run { trySend(null); close(); return@callbackFlow }
        val registration = db.collection("users").document(userId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(UserDoc::class.java))
            }
        awaitClose { registration.remove() }
    }

    suspend fun observeTransactions(limit: Long = 50): List<PointTransaction> {
        val userId = uid ?: return emptyList()
        val snap = db.collection("pointTransactions")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt")
            .limit(limit)
            .get()
            .await()
        return snap.toObjects(PointTransaction::class.java)
    }

    fun signOut() {
        auth.signOut()
    }
}
