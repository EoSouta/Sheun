package com.retirada.app

import com.retirada.app.data.ConnectionChecker
import com.retirada.app.data.DemoInventoryRepository
import com.retirada.app.domain.ConnectionState
import com.retirada.app.domain.Priority
import com.retirada.app.viewmodel.HistoryViewModel
import com.retirada.app.viewmodel.ReportsViewModel
import com.retirada.app.viewmodel.StockViewModel
import com.retirada.app.viewmodel.WithdrawViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithdrawViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private fun vm(repo: DemoInventoryRepository = DemoInventoryRepository()) =
        WithdrawViewModel(repo, FakeSettings(), ConnectionChecker())

    @Test fun `sem item selecionado mostra erro`() {
        val vm = vm()
        vm.confirm()
        assertEquals("Selecione um item.", vm.form.value.error)
    }

    @Test fun `quantidade fica entre 1 e o estoque`() {
        val repo = DemoInventoryRepository()
        val vm = vm(repo)
        val item = repo.items.value.first()
        vm.selectItem(item.id)
        vm.changeQuantity(-5)
        assertEquals(1, vm.form.value.quantity)
        vm.changeQuantity(1000)
        assertEquals(item.stock, vm.form.value.quantity)
    }

    @Test fun `descricao e limitada a 500 caracteres`() {
        val vm = vm()
        vm.setDescription("a".repeat(600))
        assertEquals(500, vm.form.value.description.length)
    }

    @Test fun `retirada sem descricao falha e nao altera estoque`() {
        val repo = DemoInventoryRepository()
        val vm = vm(repo)
        val item = repo.items.value.first()
        vm.selectItem(item.id)
        vm.confirm()
        assertNotNull(vm.form.value.error)
        assertEquals(item.stock, repo.items.value.first().stock)
    }

    @Test fun `retirada valida limpa o formulario e baixa o estoque`() {
        val repo = DemoInventoryRepository()
        val vm = vm(repo)
        val item = repo.items.value.first()
        vm.selectItem(item.id)
        vm.changeQuantity(2)
        vm.setPriority(Priority.HIGH)
        vm.setDescription("Manutenção")
        vm.confirm()
        assertNull(vm.form.value.error)
        assertNotNull(vm.form.value.success)
        assertEquals("", vm.form.value.description)
        assertEquals(item.stock - 3, repo.items.value.first().stock)
        assertEquals(1, repo.withdrawals.value.size)
    }

    @Test fun `sem host configurado a conexao fica como nao configurada`() {
        assertEquals(ConnectionState.NotConfigured, vm().connection.value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    private fun DemoInventoryRepository.addWithdrawals() {
        val id = items.value.first().id
        withdraw(id, 1, Priority.LOW, "a", null, "Ana")
        withdraw(id, 2, Priority.HIGH, "b", null, "Ana")
        withdraw(id, 1, Priority.HIGH, "c", null, "Ana")
    }

    @Test fun `historico filtra por prioridade`() = runTest {
        val repo = DemoInventoryRepository().apply { addWithdrawals() }
        val vm = HistoryViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.withdrawals.collect {} }
        assertEquals(3, vm.withdrawals.value.size)
        vm.setFilter(Priority.HIGH)
        assertEquals(2, vm.withdrawals.value.size)
        assertTrue(vm.withdrawals.value.all { it.priority == Priority.HIGH })
    }

    @Test fun `relatorio soma retiradas e unidades`() = runTest {
        val repo = DemoInventoryRepository().apply { addWithdrawals() }
        val vm = ReportsViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.report.collect {} }
        val r = vm.report.value
        assertEquals(3, r.totalWithdrawals)
        assertEquals(4, r.totalQuantity)
        assertEquals(2, r.byPriority.first { it.priority == Priority.HIGH }.count)
        assertEquals(4, r.topItems.single().quantity)
    }

    @Test fun `reposicao invalida gera mensagem`() {
        val repo = DemoInventoryRepository()
        val vm = StockViewModel(repo)
        vm.restock(repo.items.value.first().id, 0)
        assertNotNull(vm.message.value)
        vm.dismissMessage()
        assertNull(vm.message.value)
    }
}
