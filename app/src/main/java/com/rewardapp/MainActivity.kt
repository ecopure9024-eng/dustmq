package com.rewardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rewardapp.ui.auth.LoginScreen
import com.rewardapp.ui.main.MainScaffold
import com.rewardapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RootNavigation()
                }
            }
        }
    }
}

@Composable
private fun RootNavigation(authVm: AuthViewModel = hiltViewModel()) {
    val isLoggedIn by authVm.isLoggedIn.collectAsState()
    if (isLoggedIn) {
        MainScaffold()
    } else {
        LoginScreen(onLoggedIn = authVm::onLoginSuccess)
    }
}
