package com.rewardapp.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardapp.data.model.AdItem
import com.rewardapp.repository.AdRepository
import com.rewardapp.util.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(
    private val adRepo: AdRepository,
    private val adManager: AdManager,
) : ViewModel() {

    private val _category = MutableStateFlow("전체")
    val category: StateFlow<String> = _category

    private val _ads = MutableStateFlow<List<AdItem>>(emptyList())
    val ads: StateFlow<List<AdItem>> = _ads

    private val _selected = MutableStateFlow<AdItem?>(null)
    val selected: StateFlow<AdItem?> = _selected

    private val _watchState = MutableStateFlow<WatchState>(WatchState.Idle)
    val watchState: StateFlow<WatchState> = _watchState

    init { refresh() }

    fun setCategory(c: String) {
        _category.value = c
        refresh()
    }

    fun selectAd(ad: AdItem?) { _selected.value = ad }

    fun refresh() {
        viewModelScope.launch {
            _ads.value = runCatching { adRepo.fetchAds(_category.value) }.getOrDefault(emptyList())
        }
    }

    fun loadAndShow(activity: Activity, ad: AdItem) {
        _watchState.value = WatchState.Loading
        adManager.loadAndShow(
            activity = activity,
            adUnitId = ad.adUnitId,
            onShown = { _watchState.value = WatchState.Showing },
            onUserEarnedReward = { _watchState.value = WatchState.EarnedPendingServer },
            onDismissed = {
                if (_watchState.value !is WatchState.EarnedPendingServer) {
                    _watchState.value = WatchState.Dismissed
                }
            },
            onError = { _watchState.value = WatchState.Error(it) }
        )
    }

    fun resetWatch() { _watchState.value = WatchState.Idle }

    sealed interface WatchState {
        data object Idle : WatchState
        data object Loading : WatchState
        data object Showing : WatchState
        data object EarnedPendingServer : WatchState
        data object Dismissed : WatchState
        data class Error(val message: String) : WatchState
    }
}
