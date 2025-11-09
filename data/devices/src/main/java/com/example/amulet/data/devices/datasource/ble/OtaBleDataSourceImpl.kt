package com.example.amulet.data.devices.datasource.ble

import android.util.Base64
import com.example.amulet.core.ble.AmuletBleManager
import com.example.amulet.core.ble.model.FirmwareInfo
import com.example.amulet.core.ble.model.OtaProgress
import com.example.amulet.shared.domain.devices.model.AmuletCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Реализация источника данных для OTA обновлений через BLE.
 */
class OtaBleDataSourceImpl @Inject constructor(
    private val bleManager: AmuletBleManager
) : OtaBleDataSource {
    
    override fun startBleOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress> {
        return bleManager.startOtaUpdate(firmwareInfo)
    }
    
    override fun startWifiOtaUpdate(
        ssid: String,
        password: String,
        firmwareInfo: FirmwareInfo
    ): Flow<OtaProgress> {
        return bleManager.startWifiOtaUpdate(firmwareInfo)
            .onStart {
                // Настроить Wi-Fi перед запуском OTA
                val ssidBase64 = Base64.encodeToString(
                    ssid.toByteArray(),
                    Base64.NO_WRAP
                )
                val passwordBase64 = Base64.encodeToString(
                    password.toByteArray(),
                    Base64.NO_WRAP
                )
                
                val wifiCommand = AmuletCommand.SetWifiCred(ssidBase64, passwordBase64)
                bleManager.sendCommand(wifiCommand)
            }
    }
}
