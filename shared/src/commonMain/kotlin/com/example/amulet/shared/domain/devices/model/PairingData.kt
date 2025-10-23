package com.example.amulet.shared.domain.devices.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

/**
 * Данные для паринга устройства, полученные через QR код или NFC.
 * 
 * Эти данные предоставляются внешними модулями (например, :feature:onboarding)
 * и используются для:
 * 1. Идентификации конкретного устройства при сканировании BLE
 * 2. Клейма устройства через API (/devices.claim)
 * 
 * Формат QR: amulet://pair?serial=AMU-200-XYZ-001&token=eyJ...&hw=200&name=My+Amulet
 * Формат NFC: {"serial":"AMU-200-XYZ-001","token":"eyJ...","hw":200,"name":"My Amulet"}
 * 
 * См. docs/20_DATA_LAYER/03_BLE_PROTOCOL.md раздел "QR/NFC Паринг устройства"
 */
data class PairingData(
    /**
     * Серийный номер устройства (уникальный идентификатор).
     * Формат: AMU-{HW}-{BATCH}-{SEQ}, например AMU-200-XYZ-001
     * Передается через BLE в Device Info характеристике.
     */
    val serialNumber: String,
    
    /**
     * Токен для привязки устройства к аккаунту пользователя.
     * Используется в запросе /devices.claim вместе с serialNumber.
     * Одноразовый, инвалидируется после успешного claim.
     */
    val claimToken: String,
    
    /**
     * Hardware version устройства (100 или 200).
     * Если не указан, извлекается из serialNumber.
     */
    val hardwareVersion: Int? = null,
    
    /**
     * Предзаполненное имя устройства для улучшения UX.
     */
    val deviceName: String? = null,
    
    /**
     * Дополнительная информация (опционально).
     */
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Парсинг QR кода в формате: "amulet://pair?serial=XXX&token=YYY"
         * 
         * Пример: amulet://pair?serial=AMU-200-XYZ-001&token=eyJ...&hw=200&name=My+Amulet
         * 
         * Эта функция вызывается из модуля, который работает с QR сканером.
         */
        fun fromQrCode(qrContent: String): PairingData? {
            if (!qrContent.startsWith("amulet://pair?")) return null
            
            val params = qrContent.substringAfter("?")
                .split("&")
                .associate {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else null
                }
                .filterNotNull()
                .mapValues { java.net.URLDecoder.decode(it.value, "UTF-8") }
            
            val serial = params["serial"] ?: return null
            val token = params["token"] ?: return null
            val hw = params["hw"]?.toIntOrNull()
            val name = params["name"]
            
            return PairingData(
                serialNumber = serial,
                claimToken = token,
                hardwareVersion = hw,
                deviceName = name
            )
        }
        
        /**
         * Парсинг NFC NDEF записи.
         * 
         * Пример JSON: {"serial":"AMU-200-XYZ-001","token":"eyJ...","hw":200,"name":"My Amulet"}
         * 
         * Эта функция вызывается из модуля, который работает с NFC.
         */
        fun fromNfcPayload(payload: String): PairingData? {
            return try {
                val nfcData = json.decodeFromString<NfcPairingPayload>(payload)
                PairingData(
                    serialNumber = nfcData.serial,
                    claimToken = nfcData.token,
                    hardwareVersion = nfcData.hw,
                    deviceName = nfcData.name
                )
            } catch (e: Exception) {
                null
            }
        }
        
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}

/**
 * DTO для десериализации NFC NDEF payload.
 * Использует kotlinx.serialization для надежного парсинга JSON.
 */
@Serializable
private data class NfcPairingPayload(
    @SerialName("serial")
    val serial: String,
    
    @SerialName("token")
    val token: String,
    
    @SerialName("hw")
    val hw: Int? = null,
    
    @SerialName("name")
    val name: String? = null
)