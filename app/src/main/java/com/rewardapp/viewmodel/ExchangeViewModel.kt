package com.rewardapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardapp.data.model.GifticonItem
import com.rewardapp.data.model.UserDoc
import com.rewardapp.repository.ExchangeRepository
import com.rewardapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val exchangeRepo: ExchangeRepository,
) : ViewModel() {

    val user: StateFlow<UserDoc?> = userRepo.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _items = MutableStateFlow<List<GifticonItem>>(emptyList())
    val items: StateFlow<List<GifticonItem>> = _items

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _items.value = runCatching { exchangeRepo.fetchGifticons() }.getOrDefault(emptyList())
        }
    }

    fun requestExchange(item: GifticonItem) {
        viewModelScope.launch {
            runCatching { exchangeRepo.requestExchange(item.id) }
                .onSuccess { _toast.value = "${item.name} 교환 신청 완료" }
                .onFailure { _toast.value = it.message ?: "교환 실패" }
        }
    }

    fun clearToast() { _toast.value = null }
}
