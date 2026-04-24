package com.rewardapp.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
) {
    fun loadAndShow(
        activity: Activity,
        adUnitId: String,
        onShown: () -> Unit,
        onUserEarnedReward: () -> Unit,
        onDismissed: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("로그인이 필요합니다"); return }
        val transactionId = UUID.randomUUID().toString()

        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    // SSV 콜백에 userId 및 transaction_id 전달
                    ad.setServerSideVerificationOptions(
                        ServerSideVerificationOptions.Builder()
                            .setUserId(uid)
                            .setCustomData(transactionId)
                            .build()
                    )
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() { onShown() }
                        override fun onAdDismissedFullScreenContent() { onDismissed() }
                    }
                    ad.show(activity) { _ -> onUserEarnedReward() }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onError(error.message ?: "광고 로드 실패 (code=${error.code})")
                }
            },
        )
    }
}
