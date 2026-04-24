package com.rewardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctions
import com.rewardapp.data.model.Mission
import com.rewardapp.data.model.MissionType
import com.rewardapp.data.model.UserDoc
import com.rewardapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val functions: FirebaseFunctions,
) : ViewModel() {

    val user: StateFlow<UserDoc?> = userRepo.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _todayAdViews = MutableStateFlow(0)
    val todayAdViews: StateFlow<Int> = _todayAdViews

    private val _missions = MutableStateFlow(defaultMissions())
    val missions: StateFlow<List<Mission>> = _missions

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    fun checkAttendance() {
        viewModelScope.launch {
            runCatching {
                functions.getHttpsCallable("checkAttendance").call().await()
            }.onSuccess { result ->
                @Suppress("UNCHECKED_CAST")
                val data = result.data as? Map<String, Any?>
                val reward = (data?.get("reward") as? Number)?.toLong() ?: 0L
                _toast.value = "출석 완료! +${reward}P"
            }.onFailure {
                _toast.value = it.message ?: "출석 실패"
            }
        }
    }

    fun clearToast() { _toast.value = null }

    private fun defaultMissions(): List<Mission> = listOf(
        Mission(MissionType.WATCH_AD, "광고 3개 시청", 30, 0, 3, false),
        Mission(MissionType.ATTENDANCE, "출석 체크", 30, 0, 1, false),
        Mission(MissionType.INVITE, "친구 초대", 200, 0, 1, false),
    )
}
