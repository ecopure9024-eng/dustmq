package com.rewardapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RewardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        MobileAds.initialize(this)
    }
}
