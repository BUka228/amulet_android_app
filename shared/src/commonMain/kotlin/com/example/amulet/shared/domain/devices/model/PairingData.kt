package com.example.amulet.shared.domain.devices.model

/**
 * Данные для паринга устройства, полученные через QR код или NFC.
 * 
 * Эти данные предоставляются внешними модулями (например, :feature:onboarding)
 * и используются для:
 * 1. Идентификации конкретного устройства при сканировании BLE
 * 2. Клейма устройства через API (/devices.claim)
 */
data class PairingData(
    /**
     * Серийный номер устройства (уникальный идентификатор).
     * Передается через BLE в Device Info характеристике.
     */
    val serialNumber: String,
    
    /**
     * Токен для привязки устройства к аккаунту пользователя.
     * Используется в запросе /devices.claim вместе с serialNumber.
     */
    val claimToken: String,
    
    /**
     * Дополнительная информация (опционально).
     */
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Парсинг QR кода в формате: "amulet://pair?serial=XXX&token=YYY"
         * 
         * Эта функция вызывается из модуля, который работает с QR сканером.
         */
        fun fromQrCode(qrContent: String): PairingData? {
            // Пример: amulet://pair?serial=AM-001-ABC123&token=eyJhbGc...
            if (!qrContent.startsWith("amulet://pair?")) return null
            
            val params = qrContent.substringAfter("?")
                .split("&")
                .associate {
                    val (key, value) = it.split("=")
                    key to value
                }
            
            val serial = params["serial"] ?: return null
            val token = params["token"] ?: return null
            
            return PairingData(serial, token)
        }
        
        /**
         * Парсинг NFC NDEF записи.
         * 
         * Эта функция вызывается из модуля, который работает с NFC.
         * NFC метка содержит JSON: {"serial": "XXX", "token": "YYY"}
         */
        fun fromNfcPayload(payload: String): PairingData? {
            // Простой парсинг JSON (в реальности использовать kotlinx.serialization)
            // Пример: {"serial":"AM-001-ABC123","token":"eyJhbGc..."}
            
            val serialRegex = """"serial"\s*:\s*"([^"]+)"""".toRegex()
            val tokenRegex = """"token"\s*:\s*"([^"]+)"""".toRegex()
            
            val serial = serialRegex.find(payload)?.groupValues?.get(1) ?: return null
            val token = tokenRegex.find(payload)?.groupValues?.get(1) ?: return null
            
            return PairingData(serial, token)
        }
    }
}