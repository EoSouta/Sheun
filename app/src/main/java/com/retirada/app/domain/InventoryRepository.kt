package com.retirada.app.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Fonte dos dados de estoque e retiradas.
 * Hoje só existe a implementação local em memória (DemoInventoryRepository).
 * A comunicação com o ESP32 ainda NÃO existe: o protocolo/endpoints precisam ser definidos antes.
 */
interface InventoryRepository {
    val items: StateFlow<List<Item>>
    val withdrawals: StateFlow<List<Withdrawal>>

    fun withdraw(
        itemId: String,
        quantity: Int,
        priority: Priority,
        description: String,
        imageUri: String?,
        operator: String
    ): Result<Withdrawal>

    fun restock(itemId: String, quantity: Int): Result<Unit>
}
