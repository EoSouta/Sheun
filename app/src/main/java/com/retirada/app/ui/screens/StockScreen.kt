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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retirada.app.domain.Item
import com.retirada.app.ui.components.DemoNotice
import com.retirada.app.ui.components.ScreenTitle
import com.retirada.app.ui.components.SectionCard
import com.retirada.app.viewmodel.AppViewModelProvider
import com.retirada.app.viewmodel.StockViewModel

@Composable
fun StockScreen(vm: StockViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val items by vm.items.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var restocking by remember { mutableStateOf<Item?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Estoque", "Recarga e reposição de itens")
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { DemoNotice() }
            message?.let { m ->
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(m, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = vm::dismissMessage) { Text("OK") }
                    }
                }
            }
            items(items, key = { it.id }) { item ->
                SectionCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (item.isLow) "Estoque baixo (mínimo ${item.minStock})" else "Estoque normal",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (item.isLow) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            item.stock.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                    OutlinedButton(onClick = { restocking = item }) { Text("Repor") }
                }
            }
        }
    }

    restocking?.let { item ->
        var qty by remember(item.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { restocking = null },
            title = { Text("Repor ${item.name}") },
            text = {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter(Char::isDigit).take(5) },
                    label = { Text("Quantidade a adicionar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.restock(item.id, qty.toIntOrNull() ?: 0)
                    restocking = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { restocking = null }) { Text("Cancelar") } }
        )
    }
}
