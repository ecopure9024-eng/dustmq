package com.rewardapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rewardapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel = hiltViewModel(),
    onLoggedIn: () -> Unit,
) {
    val context = LocalContext.current
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val isLoggedIn by vm.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) { if (isLoggedIn) onLoggedIn() }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "포인트 쌓고\n기프티콘 받자",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = { vm.loginKakao(context) },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500), contentColor = Color.Black),
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                else Text("카카오로 시작하기", fontWeight = FontWeight.SemiBold)
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
