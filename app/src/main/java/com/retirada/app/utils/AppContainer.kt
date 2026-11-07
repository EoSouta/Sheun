package com.retirada.app.utils

import android.content.Context
import com.retirada.app.data.ConnectionChecker
import com.retirada.app.data.DemoInventoryRepository
import com.retirada.app.data.SettingsRepository
import com.retirada.app.domain.InventoryRepository

/** Injeção de dependência manual (sem Hilt/Koin, para manter o app leve). */
class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context.applicationContext)
    val connectionChecker = ConnectionChecker()

    // PENDENTE: trocar por implementação que fale com o ESP32 quando o protocolo for definido.
    val inventoryRepository: InventoryRepository = DemoInventoryRepository()
}
