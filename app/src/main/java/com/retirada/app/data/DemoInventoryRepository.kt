package com.retirada.app.data

import com.retirada.app.domain.InventoryRepository
import com.retirada.app.domain.Item
import com.retirada.app.domain.MAX_DESCRIPTION
import com.retirada.app.domain.Priority
import com.retirada.app.domain.Withdrawal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * DADOS DE DEMONSTRAÇÃO, apenas em memória (perdem-se ao fechar o app).
 * Nada é enviado a ESP32 ou a qualquer servidor.
 */
class DemoInventoryRepository(
    private val clock: () -> Long = System::currentTimeMillis
) : InventoryRepository {

    private val _items = MutableStateFlow(
        listOf(
            Item("demo-1", "Notebook técnico (exemplo)", stock = 10, minStock = 2),
            Item("demo-2", "Multímetro (exemplo)", stock = 6, minStock = 2),
            Item("demo-3", "Cabo de rede (exemplo)", stock = 25, minStock = 5),
            Item("demo-4", "Fonte 12 V (exemplo)", stock = 3, minStock = 3),
            Item("demo-5", "Kit de ferramentas (exemplo)", stock = 4, minStock = 1)
        )
    )
    override val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<Withdrawal>>(emptyList())
    override val withdrawals: StateFlow<List<Withdrawal>> = _withdrawals.asStateFlow()

    private var nextId = 1L

    @Synchronized
    override fun withdraw(
        itemId: String,
        quantity: Int,
        priority: Priority,
        description: String,
        imageUri: String?,
        operator: String
    ): Result<Withdrawal> {
        val item = _items.value.firstOrNull { it.id == itemId }
            ?: return Result.failure(IllegalArgumentException("Selecione um item."))
        if (quantity < 1) return Result.failure(IllegalArgumentException("A quantidade mínima é 1."))
        if (quantity > item.stock) {
            return Result.failure(IllegalArgumentException("Estoque insuficiente (disponível: ${item.stock})."))
        }
        val text = description.trim()
        if (text.isEmpty()) return Result.failure(IllegalArgumentException("Descreva a atividade."))
        if (text.length > MAX_DESCRIPTION) {
            return Result.failure(IllegalArgumentException("Descrição muito longa (máx. $MAX_DESCRIPTION)."))
        }

        val record = Withdrawal(
            id = nextId++, itemId = item.id, itemName = item.name, quantity = quantity,
            priority = priority, description = text, imageUri = imageUri,
            operator = operator, timestampMillis = clock()
        )
        _items.update { list -> list.map { if (it.id == itemId) it.copy(stock = it.stock - quantity) else it } }
        _withdrawals.update { listOf(record) + it }
        return Result.success(record)
    }

    @Synchronized
    override fun restock(itemId: String, quantity: Int): Result<Unit> {
        if (quantity < 1) return Result.failure(IllegalArgumentException("Informe uma quantidade maior que zero."))
        if (_items.value.none { it.id == itemId }) {
            return Result.failure(IllegalArgumentException("Item não encontrado."))
        }
        _items.update { list -> list.map { if (it.id == itemId) it.copy(stock = it.stock + quantity) else it } }
        return Result.success(Unit)
    }
}
