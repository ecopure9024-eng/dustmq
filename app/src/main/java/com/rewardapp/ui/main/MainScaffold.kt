package com.rewardapp.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rewardapp.ui.ads.AdListScreen
import com.rewardapp.ui.exchange.ExchangeScreen
import com.rewardapp.ui.home.HomeScreen
import com.rewardapp.ui.profile.ProfileScreen

enum class MainTab(val label: String) {
    HOME("홈"), ADS("광고보기"), EXCHANGE("교환하기"), PROFILE("내 정보")
}

@Composable
fun MainScaffold() {
    var tab by remember { mutableStateOf(MainTab.HOME) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == MainTab.HOME,
                    onClick = { tab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text(MainTab.HOME.label) },
                )
                NavigationBarItem(
                    selected = tab == MainTab.ADS,
                    onClick = { tab = MainTab.ADS },
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    label = { Text(MainTab.ADS.label) },
                )
                NavigationBarItem(
                    selected = tab == MainTab.EXCHANGE,
                    onClick = { tab = MainTab.EXCHANGE },
                    icon = { Icon(Icons.Default.Redeem, null) },
                    label = { Text(MainTab.EXCHANGE.label) },
                )
                NavigationBarItem(
                    selected = tab == MainTab.PROFILE,
                    onClick = { tab = MainTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text(MainTab.PROFILE.label) },
                )
            }
        }
    ) { padding ->
        val innerModifier = Modifier.padding(padding)
        when (tab) {
            MainTab.HOME -> HomeScreen(modifier = innerModifier, onGoAds = { tab = MainTab.ADS })
            MainTab.ADS -> AdListScreen(modifier = innerModifier)
            MainTab.EXCHANGE -> ExchangeScreen(modifier = innerModifier)
            MainTab.PROFILE -> ProfileScreen(modifier = innerModifier)
        }
    }
}
