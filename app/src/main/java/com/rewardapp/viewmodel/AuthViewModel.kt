package com.rewardapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rewardapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        auth.addAuthStateListener { _isLoggedIn.value = it.currentUser != null }
    }

    fun loginKakao(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { authRepo.loginWithKakao(context) }
                .onFailure { _error.value = it.message ?: "로그인 실패" }
            _loading.value = false
        }
    }

    fun onLoginSuccess() { _isLoggedIn.value = true }

    fun signOut() { auth.signOut() }
}
