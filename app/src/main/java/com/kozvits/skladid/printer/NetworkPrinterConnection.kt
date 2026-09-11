package com.kozvits.skladid.printer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

private const val CONNECT_TIMEOUT_MS = 5000

@Singleton
class NetworkPrinterConnection @Inject constructor() {

    suspend fun send(ip: String, port: Int, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(ip.isNotBlank()) { "IP-адрес принтера не задан" }
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS)
                socket.getOutputStream().apply {
                    write(data)
                    flush()
                }
            }
        }
    }
}
