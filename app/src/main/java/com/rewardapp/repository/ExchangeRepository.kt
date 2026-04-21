package com.rewardapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rewardapp.data.model.GifticonItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions,
) {
    suspend fun fetchGifticons(): List<GifticonItem> {
        val snap = db.collection("gifticons").get().await()
        return snap.documents.mapNotNull { d ->
            GifticonItem(
                id = d.id,
                name = d.getString("name") ?: return@mapNotNull null,
                pointsRequired = d.getLong("pointsRequired") ?: 0,
                imageUrl = d.getString("imageUrl") ?: "",
                vendor = d.getString("vendor") ?: "",
            )
        }
    }

    suspend fun requestExchange(itemId: String): Map<String, Any?> {
        val result = functions
            .getHttpsCallable("requestExchange")
            .call(mapOf("itemId" to itemId))
            .await()
        @Suppress("UNCHECKED_CAST")
        return (result.data as? Map<String, Any?>) ?: emptyMap()
    }
}
