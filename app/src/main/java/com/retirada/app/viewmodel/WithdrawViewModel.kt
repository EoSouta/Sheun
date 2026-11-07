package com.retirada.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retirada.app.data.ConnectionChecker
import com.retirada.app.domain.ConnectionState
import com.retirada.app.domain.InventoryRepository
import com.retirada.app.domain.Item
import com.retirada.app.domain.MAX_DESCRIPTION
import com.retirada.app.domain.Priority
import com.retirada.app.domain.SettingsSource
import com.retirada.app.utils.isSameDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WithdrawForm(
    val itemId: String? = null,
    val quantity: Int = 1,
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val imageUri: String? = null,
    val error: String? = null,
    val success: String? = null
)

data class HomeSummary(val available: Int, val withdrawalsToday: Int, val lowStock: Int)

class WithdrawViewModel(
    private val repository: InventoryRepository,
    private val settings: SettingsSource,
    private val checker: ConnectionChecker
) : ViewModel() {

    val items: StateFlow<List<Item>> = repository.items

    private val _form = MutableStateFlow(WithdrawForm())
    val form: StateFlow<WithdrawForm> = _form.asStateFlow()

    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    val operatorName: StateFlow<String> = settings.settings
        .map { it.operatorName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Operador")

    val summary: StateFlow<HomeSummary> = combine(repository.items, repository.withdrawals) { items, w ->
        val now = System.currentTimeMillis()
        HomeSummary(
            available = items.sumOf { it.stock },
            withdrawalsToday = w.count { isSameDay(it.timestampMillis, now) },
            lowStock = items.count { it.isLow }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSummary(0, 0, 0))

    init { checkConnection() }

    fun selectItem(id: String) = _form.update { it.copy(itemId = id, quantity = 1, error = null, success = null) }

    fun changeQuantity(delta: Int) = _form.update { f ->
        val max = items.value.firstOrNull { it.id == f.itemId }?.stock ?: 1
        f.copy(quantity = (f.quantity + delta).coerceIn(1, maxOf(1, max)), error = null, success = null)
    }

    fun setPriority(p: Priority) = _form.update { it.copy(priority = p) }

    fun setDescription(text: String) = _form.update {
        it.copy(description = text.take(MAX_DESCRIPTION), error = null, success = null)
    }

    fun setImage(uri: String?) = _form.update { it.copy(imageUri = uri) }

    fun confirm() {
        val f = _form.value
        val id = f.itemId
        if (id == null) {
            _form.update { it.copy(error = "Selecione um item.", success = null) }
            return
        }
        viewModelScope.launch {
            val operator = settings.settings.first().operatorName
            repository.withdraw(id, f.quantity, f.priority, f.description, f.imageUri, operator).fold(
                onSuccess = {
                    _form.value = WithdrawForm(
                        success = "Retirada registrada localmente (demonstração). Nada foi enviado ao ESP32."
                    )
                },
                onFailure = { e -> _form.update { it.copy(error = e.message ?: "Erro ao registrar.", success = null) } }
            )
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            val s = settings.settings.first()
            val port = s.port.toIntOrNull()
            if (s.host.isBlank() || port == null) {
                _connection.value = ConnectionState.NotConfigured
            } else {
                _connection.value = ConnectionState.Checking
                _connection.value = checker.check(s.host.trim(), port)
            }
        }
    }
}
