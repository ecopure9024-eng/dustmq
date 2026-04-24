package com.rewardapp.ui.exchange

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import coil.compose.AsyncImage
import com.rewardapp.data.model.GifticonItem
import com.rewardapp.viewmodel.ExchangeViewModel

@Composable
fun ExchangeScreen(
    modifier: Modifier = Modifier,
    vm: ExchangeViewModel = hiltViewModel(),
) {
    val user by vm.user.collectAsState()
    val items by vm.items.collectAsState()
    val toast by vm.toast.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearToast()
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEAFF))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("보유 포인트", fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("${user?.points ?: 0}P", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("기프티콘", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            items(items, key = { it.id }) { g ->
                GifticonRow(item = g, userPoints = user?.points ?: 0) { vm.requestExchange(g) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                "현금 출금 기능은 준비 중입니다",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun GifticonRow(item: GifticonItem, userPoints: Long, onExchange: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.height(60.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.vendor, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text("${item.pointsRequired}P", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
            Button(onClick = onExchange, enabled = userPoints >= item.pointsRequired) {
                Text("교환")
            }
        }
    }
}
