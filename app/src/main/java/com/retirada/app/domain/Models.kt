package com.retirada.app.domain

/** Item de estoque. Os itens atuais são de DEMONSTRAÇÃO, não vêm de nenhum sistema real. */
data class Item(
    val id: String,
    val name: String,
    val stock: Int,
    val minStock: Int
) {
    val isLow: Boolean get() = stock <= minStock
}

enum class Priority(val label: String) { LOW("Baixa"), MEDIUM("Média"), HIGH("Alta") }

data class Withdrawal(
    val id: Long,
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val priority: Priority,
    val description: String,
    val imageUri: String?,
    val operator: String,
    val timestampMillis: Long
)

enum class ThemeMode(val label: String) { DARK("Escuro"), LIGHT("Claro"), SYSTEM("Sistema") }

data class AppSettings(
    val operatorName: String = "Operador",
    val host: String = "",
    val port: String = "",
    val themeMode: ThemeMode = ThemeMode.DARK
)

/** Resultado do teste de alcance TCP. Não prova que o dispositivo responde a nenhum protocolo. */
sealed interface ConnectionState {
    data object Unknown : ConnectionState
    data object NotConfigured : ConnectionState
    data object Checking : ConnectionState
    data class Reachable(val latencyMs: Long) : ConnectionState
    data class Unreachable(val reason: String) : ConnectionState
}

const val MAX_DESCRIPTION = 500

/** Fonte das preferências do app (permite testar ViewModels sem Android). */
interface SettingsSource {
    val settings: kotlinx.coroutines.flow.Flow<AppSettings>
}
