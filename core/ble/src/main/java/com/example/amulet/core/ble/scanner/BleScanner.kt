package com.example.amulet.core.ble.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.example.amulet.core.ble.internal.GattConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сканер для обнаружения BLE устройств амулета.
 */
@Singleton
class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    /**
     * Сканировать BLE устройства амулета.
     * Возвращает поток списков всех найденных устройств.
     * 
     * @param timeoutMs Таймаут сканирования (0 = бесконечно)
     * @return Flow со списками найденных устройств
     */
    @SuppressLint("MissingPermission")
    fun scanForAmulets(
        timeoutMs: Long = 10_000L
    ): Flow<List<ScannedDevice>> = callbackFlow {
        val scanner = bleScanner ?: run {
            close(IllegalStateException("Bluetooth LE scanner not available"))
            return@callbackFlow
        }
        
        val foundDevices = mutableMapOf<String, ScannedDevice>()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = ScannedDevice(
                    name = result.device.name ?: "Unknown Amulet",
                    address = result.device.address,
                    rssi = result.rssi
                )
                
                foundDevices[device.address] = device
                trySend(foundDevices.values.toList())
            }
            
            override fun onScanFailed(errorCode: Int) {
                close(Exception("Scan failed with error code: $errorCode"))
            }
        }
        
        // Фильтр по Amulet Device Service
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(GattConstants.AMULET_DEVICE_SERVICE_UUID))
            .build()
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        
        if (timeoutMs > 0) {
            kotlinx.coroutines.delay(timeoutMs)
            scanner.stopScan(scanCallback)
            close()
        }
        
        awaitClose {
            scanner.stopScan(scanCallback)
        }
    }
}

/**
 * Информация о найденном устройстве.
 */
data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as ScannedDevice
        
        return address == other.address
    }
    
    override fun hashCode(): Int {
        return address.hashCode()
    }
}
