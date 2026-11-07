package com.retirada.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retirada.app.ui.components.EmptyMessage
import com.retirada.app.ui.components.ScreenTitle
import com.retirada.app.ui.components.SectionCard
import com.retirada.app.ui.components.SectionLabel
import com.retirada.app.viewmodel.AppViewModelProvider
import com.retirada.app.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen(vm: ReportsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val r by vm.report.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle("Relatórios", "Calculados a partir das retiradas deste aparelho")
        if (r.totalWithdrawals == 0) {
            EmptyMessage("Sem dados ainda. Registre uma retirada na aba Início.")
            return@Column
        }
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column {
                        Text(r.totalWithdrawals.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                        Text("Retiradas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column {
                        Text(r.totalQuantity.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                        Text("Unidades retiradas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            SectionCard {
                SectionLabel("Por prioridade")
                r.byPriority.forEach { s ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.priority.label, style = MaterialTheme.typography.bodyMedium)
                            Text(s.count.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                        LinearProgressIndicator(
                            progress = { s.count.toFloat() / r.totalWithdrawals },
                            modifier = Modifier.fillMaxWidth(),
                            color = priorityColor(s.priority),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
            SectionCard {
                SectionLabel("Itens mais retirados (unidades)")
                val max = r.topItems.maxOf { it.quantity }.coerceAtLeast(1)
                r.topItems.forEach { s ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(s.quantity.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                        LinearProgressIndicator(
                            progress = { s.quantity.toFloat() / max },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
