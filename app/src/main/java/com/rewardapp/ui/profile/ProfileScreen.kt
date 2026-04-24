package com.rewardapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardapp.data.model.PointTransaction
import com.rewardapp.repository.UserRepository
import com.rewardapp.viewmodel.AuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {
    private val _txs = MutableStateFlow<List<PointTransaction>>(emptyList())
    val txs: StateFlow<List<PointTransaction>> = _txs

    init {
        viewModelScope.launch {
            _txs.value = runCatching { userRepo.observeTransactions() }.getOrDefault(emptyList())
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    vm: ProfileViewModel = hiltViewModel(),
    authVm: AuthViewModel = hiltViewModel(),
) {
    val txs by vm.txs.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("적립/사용 내역", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(txs) { tx ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(tx.type, modifier = Modifier.weight(1f))
                        Text(
                            (if (tx.amount >= 0) "+" else "") + "${tx.amount}P",
                            color = if (tx.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { authVm.signOut() }, modifier = Modifier.fillMaxWidth()) { Text("로그아웃") }
    }
}
