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
     * Фильтрует по Amulet Device Service UUID.
     * 
     * @param timeoutMs Таймаут сканирования (0 = бесконечно)
     * @param serialNumberFilter Фильтр по серийному номеру (для паринга конкретного устройства)
     * @return Flow с найденными устройствами
     */
    @SuppressLint("MissingPermission")
    fun scanForAmulets(
        timeoutMs: Long = 10_000L,
        serialNumberFilter: String? = null
    ): Flow<ScannedDevice> = callbackFlow {
        val scanner = bleScanner ?: run {
            close(IllegalStateException("Bluetooth LE scanner not available"))
            return@callbackFlow
        }
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Извлечь serial number из Manufacturer Data или Device Name
                val serialNumber = extractSerialNumber(result)
                
                // Фильтрация по serial number если указан
                if (serialNumberFilter != null && serialNumber != serialNumberFilter) {
                    return
                }
                
                val device = ScannedDevice(
                    name = result.device.name ?: "Unknown",
                    address = result.device.address,
                    rssi = result.rssi,
                    scanRecord = result.scanRecord?.bytes,
                    serialNumber = serialNumber
                )
                trySend(device)
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
    
    /**
     * Извлечь serial number из BLE Scan Result.
     * 
     * Serial может быть закодирован в:
     * 1. Device Name (например, "Amulet-AM001ABC123")
     * 2. Manufacturer Data (Company ID + Serial)
     * 3. Service Data (AMULET_DEVICE_SERVICE_UUID + Serial)
     */
    @SuppressLint("MissingPermission")
    private fun extractSerialNumber(result: ScanResult): String? {
        val scanRecord = result.scanRecord ?: return null
        
        // Вариант 1: Из имени устройства
        val deviceName = result.device.name
        if (deviceName?.startsWith("Amulet-") == true) {
            return deviceName.substringAfter("Amulet-")
        }
        
        // Вариант 2: Из Service Data (предпочтительный способ)
        val serviceData = scanRecord.getServiceData(
            android.os.ParcelUuid(GattConstants.AMULET_DEVICE_SERVICE_UUID)
        )
        if (serviceData != null && serviceData.isNotEmpty()) {
            return String(serviceData, Charsets.UTF_8).trim()
        }
        
        // Вариант 3: Из Manufacturer Data
        // Company ID для Amulet (пример: 0xFFFF)
        val manufacturerData = scanRecord.getManufacturerSpecificData(0xFFFF)
        if (manufacturerData != null && manufacturerData.isNotEmpty()) {
            // Формат: [Company ID: 2 bytes][Serial: N bytes]
            return String(manufacturerData, Charsets.UTF_8).trim()
        }
        
        return null
    }
}

/**
 * Информация о найденном устройстве.
 */
data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val scanRecord: ByteArray? = null,
    val serialNumber: String? = null
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
