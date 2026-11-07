package com.retirada.app

import com.retirada.app.data.DemoInventoryRepository
import com.retirada.app.domain.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoInventoryRepositoryTest {

    private fun repo() = DemoInventoryRepository(clock = { 1_000L })

    @Test fun `retirada valida reduz estoque e registra historico`() {
        val r = repo()
        val id = r.items.value.first().id
        val before = r.items.value.first().stock
        val res = r.withdraw(id, 2, Priority.HIGH, "  Manutenção  ", null, "Ana")
        assertTrue(res.isSuccess)
        assertEquals(before - 2, r.items.value.first { it.id == id }.stock)
        assertEquals("Manutenção", r.withdrawals.value.single().description)
    }

    @Test fun `rejeita quantidade maior que o estoque`() {
        val r = repo()
        val item = r.items.value.first()
        assertTrue(r.withdraw(item.id, item.stock + 1, Priority.LOW, "x", null, "Ana").isFailure)
        assertTrue(r.withdrawals.value.isEmpty())
    }

    @Test fun `rejeita descricao vazia e quantidade zero`() {
        val r = repo()
        val id = r.items.value.first().id
        assertTrue(r.withdraw(id, 1, Priority.LOW, "   ", null, "Ana").isFailure)
        assertTrue(r.withdraw(id, 0, Priority.LOW, "x", null, "Ana").isFailure)
    }

    @Test fun `reposicao soma ao estoque e valida quantidade`() {
        val r = repo()
        val item = r.items.value.first()
        assertTrue(r.restock(item.id, 5).isSuccess)
        assertEquals(item.stock + 5, r.items.value.first { it.id == item.id }.stock)
        assertTrue(r.restock(item.id, 0).isFailure)
        assertTrue(r.restock("inexistente", 1).isFailure)
    }
}
