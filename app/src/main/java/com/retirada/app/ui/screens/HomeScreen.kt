package com.retirada.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Add
import com.retirada.app.domain.MAX_DESCRIPTION
import com.retirada.app.domain.Priority
import com.retirada.app.ui.components.ConnectionChip
import com.retirada.app.ui.components.DemoNotice
import com.retirada.app.ui.components.ScreenTitle
import com.retirada.app.ui.components.SectionCard
import com.retirada.app.ui.components.SectionLabel
import com.retirada.app.ui.components.rememberThumbnail
import com.retirada.app.viewmodel.AppViewModelProvider
import com.retirada.app.viewmodel.WithdrawViewModel

@Composable
fun priorityColor(p: Priority): Color = when (p) {
    Priority.LOW -> MaterialTheme.colorScheme.tertiary
    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
    Priority.HIGH -> MaterialTheme.colorScheme.error
}

@Composable
fun HomeScreen(vm: WithdrawViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val form by vm.form.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val summary by vm.summary.collectAsStateWithLifecycle()
    val connection by vm.connection.collectAsStateWithLifecycle()
    val operator by vm.operatorName.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.setImage(uri.toString())
    }
    val thumbnail by rememberThumbnail(form.imageUri)
    val selected = items.firstOrNull { it.id == form.itemId }
    var menuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        ScreenTitle("Nova retirada", "Operador: $operator")
        Column(
            Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionChip(connection)

            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat("Disponíveis", summary.available.toString(), MaterialTheme.colorScheme.tertiary)
                    Stat("Retiradas hoje", summary.withdrawalsToday.toString(), MaterialTheme.colorScheme.primary)
                    Stat(
                        "Estoque baixo", summary.lowStock.toString(),
                        if (summary.lowStock > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            DemoNotice()

            SectionCard {
                SectionLabel("Item")
                Column {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selected?.name ?: "Selecionar item", Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.name} · ${item.stock} un.") },
                                enabled = item.stock > 0,
                                onClick = { vm.selectItem(item.id); menuOpen = false }
                            )
                        }
                    }
                }

                SectionLabel("Quantidade")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { vm.changeQuantity(-1) }, enabled = selected != null) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir")
                    }
                    Text(
                        form.quantity.toString(),
                        modifier = Modifier.width(56.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    FilledTonalIconButton(onClick = { vm.changeQuantity(1) }, enabled = selected != null) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                    }
                    Spacer(Modifier.weight(1f))
                    if (selected != null) {
                        Text(
                            "Restante: ${selected.stock - form.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                SectionLabel("Prioridade")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = form.priority == p,
                            onClick = { vm.setPriority(p) },
                            label = { Text(p.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priorityColor(p).copy(alpha = 0.25f)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = form.description,
                    onValueChange = vm::setDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descrição da atividade *") },
                    placeholder = { Text("Ex.: Manutenção preventiva no laboratório") },
                    supportingText = { Text("${form.description.length} / $MAX_DESCRIPTION") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (form.imageUri == null) "Anexar imagem (opcional)" else "Trocar imagem")
                    }
                    Spacer(Modifier.weight(1f))
                    if (form.imageUri != null) {
                        thumbnail?.let {
                            Image(
                                bitmap = it, contentDescription = "Imagem anexada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                            )
                        }
                        IconButton(onClick = { vm.setImage(null) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remover imagem")
                        }
                    }
                }
            }

            form.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            form.success?.let {
                Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = vm::confirm,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Confirmar retirada", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
