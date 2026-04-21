package com.rewardapp.util

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 루팅/에뮬레이터/변조된 앱을 서버에서 검증한다.
 * 실제 앱 실행 가드로 쓰기 전에, Cloud Functions 쪽 `integrityVerify`가 배포되어야 한다.
 */
@Singleton
class IntegrityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val functions: FirebaseFunctions,
) {
    suspend fun check(): Boolean {
        val nonce = requestNonceFromServer()
        val manager = IntegrityManagerFactory.create(context)
        val token = manager.requestIntegrityToken(
            IntegrityTokenRequest.builder().setNonce(nonce).build()
        ).await().token()
        return verifyWithServer(token)
    }

    private suspend fun requestNonceFromServer(): String {
        val result = functions.getHttpsCallable("issueIntegrityNonce").call().await()
        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?>
        return data?.get("nonce") as? String ?: throw IllegalStateException("nonce 발급 실패")
    }

    private suspend fun verifyWithServer(token: String): Boolean {
        val result = functions
            .getHttpsCallable("integrityVerify")
            .call(mapOf("token" to token))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?>
        return data?.get("ok") as? Boolean ?: false
    }
}
