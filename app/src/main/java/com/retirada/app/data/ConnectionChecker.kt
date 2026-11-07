package com.retirada.app.data

import com.retirada.app.domain.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/** Testa só se host:porta aceitam conexão TCP. Não fala nenhum protocolo com o ESP32. */
class ConnectionChecker {
    suspend fun check(host: String, port: Int, timeoutMs: Int = 3000): ConnectionState =
        withContext(Dispatchers.IO) {
            val start = System.nanoTime()
            try {
                Socket().use { it.connect(InetSocketAddress(host, port), timeoutMs) }
                ConnectionState.Reachable((System.nanoTime() - start) / 1_000_000)
            } catch (e: Exception) {
                ConnectionState.Unreachable(e.message ?: "Falha ao conectar")
            }
        }
}
