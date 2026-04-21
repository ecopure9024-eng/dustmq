package com.rewardapp.data.model

import com.google.firebase.Timestamp

data class UserDoc(
    val phone: String? = null,
    val points: Long = 0,
    val streak: Int = 0,
    val lastAttendance: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val status: String = "active",
)

data class PointTransaction(
    val userId: String = "",
    val type: String = "",
    val amount: Long = 0,
    val refId: String = "",
    val createdAt: Timestamp? = null,
)

data class AdItem(
    val id: String,
    val title: String,
    val category: String,
    val thumbnailUrl: String,
    val durationSec: Int,
    val rewardPoints: Long,
    val adUnitId: String,
)

data class GifticonItem(
    val id: String,
    val name: String,
    val pointsRequired: Long,
    val imageUrl: String,
    val vendor: String,
)

enum class MissionType { WATCH_AD, ATTENDANCE, INVITE }

data class Mission(
    val type: MissionType,
    val title: String,
    val rewardPoints: Long,
    val progress: Int,
    val goal: Int,
    val completed: Boolean,
)
