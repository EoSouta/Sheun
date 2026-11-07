package com.retirada.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retirada.app.data.ConnectionChecker
import com.retirada.app.data.SettingsRepository
import com.retirada.app.domain.AppSettings
import com.retirada.app.domain.ConnectionState
import com.retirada.app.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val checker: ConnectionChecker
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun validPort(port: String): Int? = port.trim().toIntOrNull()?.takeIf { it in 1..65535 }

    fun saveDevice(host: String, port: String) {
        when {
            host.isBlank() -> _error.value = "Informe o endereço IP ou nome do dispositivo."
            validPort(port) == null -> _error.value = "Porta inválida (use 1 a 65535)."
            else -> { _error.value = null; viewModelScope.launch { repository.saveDevice(host, port) } }
        }
    }

    fun testConnection(host: String, port: String) {
        val p = validPort(port)
        if (host.isBlank() || p == null) {
            _error.value = "Preencha endereço e porta válidos para testar."
            return
        }
        _error.value = null
        viewModelScope.launch {
            _connection.value = ConnectionState.Checking
            _connection.value = checker.check(host.trim(), p)
        }
    }

    fun saveOperator(name: String) { viewModelScope.launch { repository.saveOperator(name) } }
    fun setTheme(mode: ThemeMode) { viewModelScope.launch { repository.setTheme(mode) } }
}
