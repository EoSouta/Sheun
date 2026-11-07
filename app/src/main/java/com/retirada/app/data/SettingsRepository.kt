package com.retirada.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.retirada.app.domain.AppSettings
import com.retirada.app.domain.SettingsSource
import com.retirada.app.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "retirada_settings")

class SettingsRepository(private val context: Context) : SettingsSource {

    private object Keys {
        val operator = stringPreferencesKey("operator")
        val host = stringPreferencesKey("host")
        val port = stringPreferencesKey("port")
        val theme = stringPreferencesKey("theme")
    }

    override val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            operatorName = p[Keys.operator]?.takeIf { it.isNotBlank() } ?: "Operador",
            host = p[Keys.host].orEmpty(),
            port = p[Keys.port].orEmpty(),
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.theme] ?: "") }.getOrDefault(ThemeMode.DARK)
        )
    }

    suspend fun saveDevice(host: String, port: String) {
        context.dataStore.edit { it[Keys.host] = host.trim(); it[Keys.port] = port.trim() }
    }

    suspend fun saveOperator(name: String) {
        context.dataStore.edit { it[Keys.operator] = name.trim() }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.name }
    }
}
