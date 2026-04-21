package com.rewardapp.ui.ads

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.rewardapp.data.model.AdItem
import com.rewardapp.viewmodel.AdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdWatchBottomSheet(
    ad: AdItem,
    onDismiss: () -> Unit,
    vm: AdViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val state by vm.watchState.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("광고 영역", color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text(ad.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("지급 예정 +${ad.rewardPoints}P", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val activity = context as? Activity ?: return@Button
                    vm.loadAndShow(activity, ad)
                },
                enabled = state is AdViewModel.WatchState.Idle || state is AdViewModel.WatchState.Error,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                val label = when (state) {
                    is AdViewModel.WatchState.Idle -> "광고 시청 시작"
                    AdViewModel.WatchState.Loading -> "광고 로딩 중…"
                    AdViewModel.WatchState.Showing -> "시청 중…"
                    AdViewModel.WatchState.EarnedPendingServer -> "서버 검증 대기 중 (포인트 곧 반영)"
                    AdViewModel.WatchState.Dismissed -> "시청이 완료되지 않았습니다"
                    is AdViewModel.WatchState.Error -> "다시 시도"
                }
                Text(label)
            }
            if (state is AdViewModel.WatchState.EarnedPendingServer) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "포인트는 서버 검증(SSV) 완료 후 자동 반영됩니다.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
