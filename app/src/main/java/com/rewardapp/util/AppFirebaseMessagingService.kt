package com.rewardapp.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token: $token")
        // TODO: Firestore /users/{uid}/fcmToken 에 저장
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message data: ${message.data}, notification: ${message.notification?.body}")
        // TODO: 로컬 알림 표시
    }

    companion object { private const val TAG = "AppFCM" }
}
