package com.retirada.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retirada.app.domain.InventoryRepository
import com.retirada.app.domain.Item
import com.retirada.app.domain.Priority
import com.retirada.app.domain.Withdrawal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repository: InventoryRepository) : ViewModel() {
    private val _filter = MutableStateFlow<Priority?>(null)
    val filter: StateFlow<Priority?> = _filter.asStateFlow()

    val withdrawals: StateFlow<List<Withdrawal>> = combine(repository.withdrawals, _filter) { all, f ->
        if (f == null) all else all.filter { it.priority == f }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(p: Priority?) { _filter.value = p }
}

class StockViewModel(private val repository: InventoryRepository) : ViewModel() {
    val items: StateFlow<List<Item>> = repository.items

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun restock(itemId: String, quantity: Int) {
        repository.restock(itemId, quantity).fold(
            onSuccess = { _message.value = "Reposição registrada localmente (demonstração)." },
            onFailure = { _message.value = it.message ?: "Erro ao repor." }
        )
    }

    fun dismissMessage() { _message.value = null }
}

data class PriorityStat(val priority: Priority, val count: Int)
data class ItemStat(val name: String, val quantity: Int)
data class ReportData(
    val totalWithdrawals: Int,
    val totalQuantity: Int,
    val byPriority: List<PriorityStat>,
    val topItems: List<ItemStat>
)

class ReportsViewModel(repository: InventoryRepository) : ViewModel() {
    val report: StateFlow<ReportData> = repository.withdrawals.map { list ->
        ReportData(
            totalWithdrawals = list.size,
            totalQuantity = list.sumOf { it.quantity },
            byPriority = Priority.entries.map { p -> PriorityStat(p, list.count { it.priority == p }) },
            topItems = list.groupBy { it.itemName }
                .map { (name, w) -> ItemStat(name, w.sumOf { it.quantity }) }
                .sortedByDescending { it.quantity }
                .take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportData(0, 0, emptyList(), emptyList()))
}
