package com.rewardapp.ui.ads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rewardapp.data.model.AdItem
import com.rewardapp.viewmodel.AdViewModel

private val CATEGORIES = listOf("전체", "쇼핑", "게임", "앱설치", "금융")

@Composable
fun AdListScreen(
    modifier: Modifier = Modifier,
    vm: AdViewModel = hiltViewModel(),
) {
    val category by vm.category.collectAsState()
    val ads by vm.ads.collectAsState()
    val selected by vm.selected.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CATEGORIES) { c ->
                FilterChip(
                    selected = c == category,
                    onClick = { vm.setCategory(c) },
                    label = { Text(c) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(ads, key = { it.id }) { ad ->
                AdCard(ad = ad, onClick = { vm.selectAd(ad) })
            }
        }
    }

    selected?.let { ad ->
        AdWatchBottomSheet(ad = ad, onDismiss = { vm.selectAd(null); vm.resetWatch() })
    }
}

@Composable
private fun AdCard(ad: AdItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            AsyncImage(
                model = ad.thumbnailUrl,
                contentDescription = ad.title,
                modifier = Modifier.fillMaxWidth().height(100.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(ad.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            Text(ad.title, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text("${ad.durationSec}초 · +${ad.rewardPoints}P", fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            androidx.compose.material3.Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("시청하기")
            }
        }
    }
}
