package com.rewardapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rewardapp.data.model.Mission
import com.rewardapp.data.model.MissionType
import com.rewardapp.viewmodel.HomeViewModel
import android.widget.Toast

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onGoAds: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel(),
) {
    val user by vm.user.collectAsState()
    val todayAdViews by vm.todayAdViews.collectAsState()
    val missions by vm.missions.collectAsState()
    val toast by vm.toast.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearToast()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        PointCard(points = user?.points ?: 0)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "오늘 시청", value = "${todayAdViews}회", modifier = Modifier.weight(1f))
            SummaryCard(title = "연속 출석", value = "${user?.streak ?: 0}일", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Text("오늘의 미션", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        missions.forEach {
            MissionRow(
                mission = it,
                onClick = {
                    when (it.type) {
                        MissionType.WATCH_AD -> onGoAds()
                        MissionType.ATTENDANCE -> vm.checkAttendance()
                        MissionType.INVITE -> Toast.makeText(context, "초대 링크 공유 예정", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PointCard(points: Long) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C5CE7)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("내 포인트", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text("${points}P", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MissionRow(mission: Mission, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mission.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("+${mission.rewardPoints}P", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            Button(onClick = onClick) { Text(if (mission.completed) "완료" else "하기") }
        }
    }
}
