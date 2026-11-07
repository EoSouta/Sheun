package com.retirada.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retirada.app.domain.ConnectionState
import com.retirada.app.domain.ThemeMode
import com.retirada.app.ui.components.ConnectionChip
import com.retirada.app.ui.components.ScreenTitle
import com.retirada.app.ui.components.SectionCard
import com.retirada.app.ui.components.SectionLabel
import com.retirada.app.viewmodel.AppViewModelProvider
import com.retirada.app.viewmodel.SettingsViewModel

private val featureStatus = listOf(
    "Retirada, histórico, estoque e relatórios" to "Implementada e funcional (local, em memória)",
    "Operador, endereço do ESP32, tema" to "Implementada e funcional",
    "Teste de conexão (TCP)" to "Implementada e funcional",
    "Comunicação com o ESP32" to "Dependente de definição do protocolo",
    "Armazenamento permanente das retiradas" to "Dependente de decisão (Room local ou servidor)"
)

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val connection by vm.connection.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var operator by remember(settings.operatorName) { mutableStateOf(settings.operatorName) }
    var host by remember(settings.host) { mutableStateOf(settings.host) }
    var port by remember(settings.port) { mutableStateOf(settings.port) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle("Configurações")
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            SectionCard {
                SectionLabel("Operador")
                OutlinedTextField(
                    value = operator, onValueChange = { operator = it.take(60) },
                    label = { Text("Nome do operador") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { vm.saveOperator(operator) }) { Text("Salvar") }
            }

            SectionCard {
                SectionLabel("Dispositivo (ESP32)")
                OutlinedTextField(
                    value = host, onValueChange = { host = it },
                    label = { Text("Endereço IP ou nome") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = port, onValueChange = { port = it.filter(Char::isDigit).take(5) },
                    label = { Text("Porta") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.saveDevice(host, port) }) { Text("Salvar") }
                    OutlinedButton(onClick = { vm.testConnection(host, port) }) { Text("Testar conexão") }
                }
                ConnectionChip(connection)
                (connection as? ConnectionState.Unreachable)?.let {
                    Text(it.reason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "O teste só verifica se a porta aceita conexão. A comunicação com o ESP32 ainda não foi implementada.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard {
                SectionLabel("Aparência")
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().selectable(
                            selected = settings.themeMode == mode,
                            onClick = { vm.setTheme(mode) },
                            role = Role.RadioButton
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.themeMode == mode, onClick = null)
                        Text(mode.label, Modifier.padding(start = 8.dp))
                    }
                }
            }

            SectionCard {
                SectionLabel("Estado das funcionalidades")
                featureStatus.forEach { (name, status) ->
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SectionCard {
                SectionLabel("Sobre")
                Text("Sistema de Retirada e Recarga de Itens", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Versão 0.1.0. Retiradas e estoque funcionam apenas localmente (em memória) e são perdidos ao fechar o app.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
