package com.example.amulet.feature.devices.scanner

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Менеджер для работы с NFC тегами.
 * 
 * Обрабатывает чтение NDEF записей с пейрингом данными устройства.
 * Работает в режиме foreground dispatch для надежного перехвата NFC интентов.
 */
class NfcManager @Inject constructor() {
    
    private var nfcAdapter: NfcAdapter? = null
    
    /**
     * Инициализация NFC адаптера.
     * 
     * @param activity Activity для получения NFC адаптера
     * @return true если NFC поддерживается и включен
     */
    fun initialize(activity: Activity): Boolean {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * Проверка доступности NFC.
     */
    fun isNfcAvailable(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * Включить foreground dispatch для перехвата NFC интентов.
     * Вызывать в onResume() активности.
     * 
     * @param activity Activity для регистрации foreground dispatch
     */
    fun enableForegroundDispatch(activity: Activity) {
        val adapter = nfcAdapter ?: return
        
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType("application/json")
                } catch (e: IntentFilter.MalformedMimeTypeException) {
                    // Ignore
                }
            }
        )
        
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, null)
    }
    
    /**
     * Отключить foreground dispatch.
     * Вызывать в onPause() активности.
     * 
     * @param activity Activity для отмены foreground dispatch
     */
    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }
    
    /**
     * Обработка NFC интента и извлечение данных.
     * 
     * @param intent Intent от NFC события
     * @return Данные из NDEF записи или null если не удалось прочитать
     */
    fun handleNfcIntent(intent: Intent): String? {
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED &&
            intent.action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            intent.action != NfcAdapter.ACTION_TECH_DISCOVERED) {
            return null
        }
        
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return null
        
        return readNdefTag(tag)
    }
    
    /**
     * Чтение NDEF данных из NFC тега.
     */
    private fun readNdefTag(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        
        return try {
            ndef.connect()
            val ndefMessage: NdefMessage = ndef.ndefMessage ?: return null
            
            // Извлекаем первую запись (обычно содержит JSON payload)
            val record = ndefMessage.records.firstOrNull() ?: return null
            val payload = record.payload
            
            // NDEF Text Record начинается с language code length (1 byte) + language code
            // Пропускаем первый байт (length) и сам language code
            val languageCodeLength = payload[0].toInt() and 0x3F
            val textPayload = payload.copyOfRange(1 + languageCodeLength, payload.size)
            
            String(textPayload, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        } finally {
            try {
                ndef.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    /**
     * Flow для наблюдения за NFC событиями.
     * Использовать совместно с enableForegroundDispatch/disableForegroundDispatch.
     * 
     * @return Flow с данными из NFC тегов
     */
    fun observeNfcTags(
        activity: Activity,
        onNewIntent: Flow<Intent>
    ): Flow<String> = callbackFlow {
        onNewIntent.collect { intent ->
            handleNfcIntent(intent)?.let { payload ->
                trySend(payload)
            }
        }
        
        awaitClose { }
    }
}
