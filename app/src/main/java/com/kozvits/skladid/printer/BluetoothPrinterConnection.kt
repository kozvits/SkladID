package com.kozvits.skladid.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@Singleton
class BluetoothPrinterConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // Guarded by hasConnectPermission()
    suspend fun send(deviceAddress: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!hasConnectPermission()) {
                throw SecurityException("Нет разрешения BLUETOOTH_CONNECT")
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: throw IllegalStateException("Bluetooth недоступен на устройстве")
            val adapter = bluetoothManager.adapter
                ?: throw IllegalStateException("Bluetooth адаптер не найден")
            if (!adapter.isEnabled) {
                throw IllegalStateException("Bluetooth выключен")
            }

            val device = adapter.getRemoteDevice(deviceAddress)
            adapter.cancelDiscovery()

            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.use {
                it.connect()
                it.outputStream.write(data)
                it.outputStream.flush()
            }
        }
    }
}
