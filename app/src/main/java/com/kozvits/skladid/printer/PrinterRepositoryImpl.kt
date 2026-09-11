package com.kozvits.skladid.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat
import com.kozvits.skladid.domain.model.BluetoothPrinterInfo
import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterConnectionType
import com.kozvits.skladid.domain.repository.PrinterRepository
import com.kozvits.skladid.domain.repository.PrinterSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothConnection: BluetoothPrinterConnection,
    private val networkConnection: NetworkPrinterConnection
) : PrinterRepository {

    @SuppressLint("MissingPermission") // Guarded by the SDK-version + permission check below
    override fun listPairedBluetoothDevices(): Result<List<BluetoothPrinterInfo>> = runCatching {
        val needsRuntimeCheck = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        if (needsRuntimeCheck &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Нет разрешения BLUETOOTH_CONNECT")
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            ?: throw IllegalStateException("Bluetooth недоступен на устройстве")
        val adapter = bluetoothManager.adapter ?: return@runCatching emptyList()

        adapter.bondedDevices.map { device ->
            BluetoothPrinterInfo(name = device.name ?: device.address, address = device.address)
        }
    }

    override suspend fun printLabel(
        bitmap: Bitmap,
        printerSettings: PrinterSettings,
        labelSettings: LabelSettings
    ): Result<Unit> {
        val commands = TsplCommandBuilder.build(bitmap, labelSettings.widthMm, labelSettings.heightMm)

        return when (printerSettings.connectionType) {
            PrinterConnectionType.BLUETOOTH -> {
                val address = printerSettings.bluetoothDeviceAddress
                    ?: return Result.failure(IllegalStateException("Принтер Bluetooth не выбран в настройках"))
                bluetoothConnection.send(address, commands)
            }
            PrinterConnectionType.NETWORK -> {
                val ip = printerSettings.networkIp
                    ?: return Result.failure(IllegalStateException("IP-адрес принтера не задан в настройках"))
                networkConnection.send(ip, printerSettings.networkPort, commands)
            }
        }
    }
}
