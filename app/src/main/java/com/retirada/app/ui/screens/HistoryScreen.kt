package com.retirada.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retirada.app.domain.Priority
import com.retirada.app.domain.Withdrawal
import com.retirada.app.ui.components.EmptyMessage
import com.retirada.app.ui.components.ScreenTitle
import com.retirada.app.ui.components.SectionCard
import com.retirada.app.utils.formatDateTime
import com.retirada.app.viewmodel.AppViewModelProvider
import com.retirada.app.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(vm: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val list by vm.withdrawals.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Histórico", "Retiradas registradas neste aparelho (demonstração)")
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { vm.setFilter(null) }, label = { Text("Todas") })
            Priority.entries.forEach { p ->
                FilterChip(selected = filter == p, onClick = { vm.setFilter(p) }, label = { Text(p.label) })
            }
        }
        if (list.isEmpty()) {
            EmptyMessage("Nenhuma retirada registrada ainda.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(list, key = { it.id }) { WithdrawalCard(it) }
            }
        }
    }
}

@Composable
private fun WithdrawalCard(w: Withdrawal) {
    SectionCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${w.quantity}× ${w.itemName}", Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            Text(w.priority.label, color = priorityColor(w.priority), style = MaterialTheme.typography.labelLarge)
        }
        Text(w.description, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${w.operator} · ${formatDateTime(w.timestampMillis)}" + if (w.imageUri != null) " · com imagem" else "",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
