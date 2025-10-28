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
import com.example.amulet.shared.core.logging.Logger
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
     * @param useFilter Использовать фильтр по Service UUID (для отладки можно отключить)
     * @return Flow со списками найденных устройств
     */
    @SuppressLint("MissingPermission")
    fun scanForAmulets(
        timeoutMs: Long = 10_000L,
        useFilter: Boolean = true  // ⚠️ ОТЛАДКА: false = сканировать ВСЕ устройства
    ): Flow<List<ScannedDevice>> = callbackFlow {
        val scanner = bleScanner ?: run {
            Logger.e("Bluetooth LE scanner not available", tag = TAG)
            close(IllegalStateException("Bluetooth LE scanner not available"))
            return@callbackFlow
        }
        
        Logger.d("Starting BLE scan with timeout: ${timeoutMs}ms", tag = TAG)
        val foundDevices = mutableMapOf<String, ScannedDevice>()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = ScannedDevice(
                    name = result.device.name ?: "Unknown Amulet",
                    address = result.device.address,
                    rssi = result.rssi
                )
                
                Logger.d("Device found: ${device.name} (${device.address}) RSSI: ${device.rssi}", tag = TAG)
                foundDevices[device.address] = device
                trySend(foundDevices.values.toList())
            }
            
            override fun onScanFailed(errorCode: Int) {
                Logger.e("Scan failed with error code: $errorCode", tag = TAG)
                close(Exception("Scan failed with error code: $errorCode"))
            }
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        if (useFilter) {
            // Сканирование с фильтром по Amulet Service UUID
            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(GattConstants.AMULET_DEVICE_SERVICE_UUID))
                .build()
            
            Logger.d("Starting scan WITH filter UUID: ${GattConstants.AMULET_DEVICE_SERVICE_UUID}", tag = TAG)
            scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        } else {
            // ОТЛАДКА: Сканирование БЕЗ фильтра - найдёт ВСЕ BLE устройства поблизости
            Logger.d("⚠️ DEBUG MODE: Starting scan WITHOUT filter - will find ALL BLE devices", tag = TAG)
            scanner.startScan(null, scanSettings, scanCallback)
        }
        
        if (timeoutMs > 0) {
            kotlinx.coroutines.delay(timeoutMs)
            Logger.d("Scan timeout reached, stopping scan", tag = TAG)
            scanner.stopScan(scanCallback)
            Logger.d("Scan completed. Found ${foundDevices.size} devices", tag = TAG)
            close()
        }
        
        awaitClose {
            Logger.d("Stopping BLE scan", tag = TAG)
            scanner.stopScan(scanCallback)
        }
    }
    
    companion object {
        private const val TAG = "BleScanner"
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
