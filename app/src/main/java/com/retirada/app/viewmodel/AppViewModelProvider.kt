package com.retirada.app.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.retirada.app.MainApplication

private fun CreationExtras.container() =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication).container

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = container()
            WithdrawViewModel(c.inventoryRepository, c.settingsRepository, c.connectionChecker)
        }
        initializer { HistoryViewModel(container().inventoryRepository) }
        initializer { StockViewModel(container().inventoryRepository) }
        initializer { ReportsViewModel(container().inventoryRepository) }
        initializer {
            val c = container()
            SettingsViewModel(c.settingsRepository, c.connectionChecker)
        }
    }
}
