package com.rewardapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
) {
    suspend fun loginWithKakao(context: android.content.Context): Boolean {
        val kakaoAccessToken = obtainKakaoAccessToken(context)
        val firebaseCustomToken = exchangeKakaoForFirebaseToken(kakaoAccessToken)
        auth.signInWithCustomToken(firebaseCustomToken).await()
        return auth.currentUser != null
    }

    private suspend fun obtainKakaoAccessToken(context: android.content.Context): String =
        suspendCancellableCoroutine { cont ->
            val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    error != null -> cont.resumeWithException(error)
                    token != null -> cont.resume(token.accessToken)
                    else -> cont.resumeWithException(IllegalStateException("카카오 토큰 없음"))
                }
            }
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }

    private suspend fun exchangeKakaoForFirebaseToken(kakaoAccessToken: String): String {
        val result = functions
            .getHttpsCallable("kakaoAuthExchange")
            .call(mapOf("accessToken" to kakaoAccessToken))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?>
        return data?.get("firebaseToken") as? String
            ?: throw IllegalStateException("Firebase 커스텀 토큰 발급 실패")
    }
}
