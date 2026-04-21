package com.rewardapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rewardapp.BuildConfig
import com.rewardapp.data.model.AdItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    suspend fun fetchAds(category: String?): List<AdItem> {
        val query = db.collection("adInventory").let {
            if (category.isNullOrBlank() || category == "전체") it else it.whereEqualTo("category", category)
        }
        val snap = query.get().await()
        return snap.documents.mapNotNull { doc ->
            AdItem(
                id = doc.id,
                title = doc.getString("title") ?: return@mapNotNull null,
                category = doc.getString("category") ?: "기타",
                thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                durationSec = (doc.getLong("durationSec") ?: 30).toInt(),
                rewardPoints = doc.getLong("rewardPoints") ?: 10,
                adUnitId = doc.getString("adUnitId") ?: BuildConfig.ADMOB_REWARDED_UNIT_ID,
            )
        }
    }
}
