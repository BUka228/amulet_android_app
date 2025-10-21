# BLE модуль Amulet

Модуль для управления Bluetooth Low Energy подключением к физическому амулету.

## Архитектура

Модуль реализует протокол из `docs/20_DATA_LAYER/03_BLE_PROTOCOL.md` с полной поддержкой:

### Основные компоненты

- **AmuletBleManager** - главный интерфейс для работы с BLE
- **AmuletBleManagerImpl** - реализация с Android Bluetooth API
- **FlowControlManager** - управление потоком данных для OTA и анимаций
- **ReconnectPolicy** - автоматическое переподключение с экспоненциальной задержкой
- **RetryPolicy** - повторы команд при ошибках
- **BleScanner** - сканирование устройств амулета

### GATT профиль

#### Сервисы и характеристики:

1. **Battery Service** (0x180F)
   - Battery Level (0x2A19) - Read, Notify

2. **Nordic UART Service** (6E400001-...)
   - TX (6E400002-...) - Write - отправка команд
   - RX (6E400003-...) - Notify - получение ответов

3. **Amulet Device Service** (12345678-...)
   - Device Info (ABD) - Read/Write - серийный номер, версия
   - Device Status (ABE) - Read/Notify - статус устройства
   - OTA Commands (ABF) - Write - команды OTA
   - Animation Load (AC0) - Write/Notify - загрузка анимаций

## Использование

### Подключение к устройству

```kotlin
@Inject lateinit var bleManager: AmuletBleManager

// Подключение с автоматическим переподключением
bleManager.connect(
    deviceAddress = "00:11:22:AA:BB:CC",
    autoReconnect = true
)

// Наблюдение за состоянием
bleManager.connectionState.collect { state ->
    when (state) {
        ConnectionState.ServicesDiscovered -> {
            // Готов к работе
        }
        is ConnectionState.Failed -> {
            // Обработка ошибки
        }
    }
}
```

### Отправка команд

```kotlin
// Простая анимация дыхания
val command = AmuletCommand.Breathing(
    color = Rgb(0, 255, 0),
    durationMs = 8000
)
val result = bleManager.sendCommand(command)

// Пульсация
bleManager.sendCommand(
    AmuletCommand.Pulse(
        color = Rgb(255, 0, 0),
        intervalMs = 500,
        repeats = 10
    )
)

// Встроенная анимация
bleManager.sendCommand(
    AmuletCommand.Play("breath_square")
)
```

### Загрузка анимации

```kotlin
val plan = AnimationPlan(
    id = "custom_animation_001",
    commands = listOf(
        AmuletCommand.SetLed(0, Rgb(255, 0, 255)),
        AmuletCommand.Delay(150),
        AmuletCommand.SetLed(0, Rgb(0, 0, 0)),
        AmuletCommand.Delay(100)
    ),
    estimatedDurationMs = 1000,
    hardwareVersion = 200
)

bleManager.uploadAnimation(plan).collect { progress ->
    when (progress.state) {
        UploadState.Uploading -> {
            println("Progress: ${progress.percent}%")
        }
        UploadState.Completed -> {
            println("Animation uploaded!")
        }
        is UploadState.Failed -> {
            println("Error: ${progress.state.cause}")
        }
    }
}
```

### OTA обновление

```kotlin
// OTA через BLE
val firmwareInfo = FirmwareInfo(
    version = "2.1.0",
    url = "https://api.amulet.com/firmware/2.1.0.bin",
    checksum = "a1b2c3d4e5f6",
    size = 512000,
    hardwareVersion = 200
)

bleManager.startOtaUpdate(firmwareInfo).collect { progress ->
    println("OTA: ${progress.percent}% - ${progress.state}")
}

// OTA через Wi-Fi
bleManager.sendCommand(
    AmuletCommand.SetWifiCred(
        ssidBase64 = "TXlXaUZp",
        passwordBase64 = "TGV0TWVJbg=="
    )
)

bleManager.startWifiOtaUpdate(firmwareInfo).collect { progress ->
    // Прогресс отслеживается через уведомления
}
```

### Мониторинг устройства

```kotlin
// Уровень батареи
bleManager.batteryLevel.collect { level ->
    println("Battery: $level%")
}

// Статус устройства
bleManager.deviceStatus.collect { status ->
    status?.let {
        println("Serial: ${it.serialNumber}")
        println("Firmware: ${it.firmwareVersion}")
        println("Battery: ${it.batteryLevel}%")
    }
}

// Уведомления
bleManager.observeNotifications().collect { notification ->
    when {
        notification.startsWith("NOTIFY:BATTERY:") -> {
            val level = notification.substringAfter("NOTIFY:BATTERY:")
        }
        notification.startsWith("NOTIFY:WIFI_OTA:PROGRESS:") -> {
            val progress = notification.substringAfter("NOTIFY:WIFI_OTA:PROGRESS:")
        }
    }
}
```

### Сканирование устройств

```kotlin
@Inject lateinit var bleScanner: BleScanner

// Сканирование всех амулетов поблизости
bleScanner.scanForAmulets(timeoutMs = 10_000).collect { device ->
    println("Found: ${device.name} (${device.address})")
    println("Serial: ${device.serialNumber}")
    // Подключиться к устройству
    bleManager.connect(device.address)
}

// Сканирование конкретного амулета по serial number (для паринга)
bleScanner.scanForAmulets(
    timeoutMs = 15_000,
    serialNumberFilter = "AM-001-ABC123"
).collect { device ->
    println("Found target device: ${device.address}")
    bleManager.connect(device.address)
}
```

### Паринг через QR код или NFC

Когда рядом несколько амулетов, используется паринг по серийному номеру:

```kotlin
@Inject lateinit var pairingHelper: DevicePairingHelper

// Шаг 1: Получить данные паринга из QR или NFC (в другом модуле)
// Пример QR: "amulet://pair?serial=AM-001-ABC123&token=eyJhbGc..."
// Пример NFC: {"serial":"AM-001-ABC123","token":"eyJhbGc..."}

val pairingData = PairingData(
    serialNumber = "AM-001-ABC123",
    claimToken = "eyJhbGc..."
)

// Шаг 2: Найти и подключиться к конкретному устройству
try {
    val deviceAddress = pairingHelper.findAndConnectDevice(
        pairingData = pairingData,
        scanTimeoutMs = 15_000
    )
    
    println("Connected to device: $deviceAddress")
    
    // Шаг 3: Проверить serial number
    val isValid = pairingHelper.verifyConnectedDevice(pairingData)
    
    if (isValid) {
        // Шаг 4: Заклеймить устройство через API
        // devicesRepository.claimDevice(pairingData.serialNumber, pairingData.claimToken)
    }
    
} catch (e: DevicePairingException.DeviceNotFound) {
    println("Устройство ${e.message} не найдено поблизости")
} catch (e: DevicePairingException.ConnectionFailed) {
    println("Не удалось подключиться: ${e.cause}")
}
```

#### Интеграция с QR сканером (в :feature:onboarding)

```kotlin
// В модуле :feature:onboarding реализовать QrPairingReader
class QrPairingReaderImpl @Inject constructor() : QrPairingReader {
    override suspend fun scanQrCode(): PairingData? {
        // Использовать CameraX + ML Kit Barcode Scanner
        // Вернуть PairingData.fromQrCode(qrContent)
    }
}

// Использование в UI
val qrReader: QrPairingReader = ...
val pairingData = qrReader.scanQrCode()
if (pairingData != null) {
    pairingHelper.findAndConnectDevice(pairingData)
}
```

#### Интеграция с NFC (в :core:nfc)

```kotlin
// В модуле :core:nfc реализовать NfcPairingReader
class NfcPairingReaderImpl @Inject constructor() : NfcPairingReader {
    override suspend fun readPairingData(intent: Intent): PairingData? {
        // Прочитать NDEF запись из NFC метки
        // Вернуть PairingData.fromNfcPayload(payload)
    }
}

// Использование в Activity/Fragment с NFC Intent
override fun onNewIntent(intent: Intent) {
    val nfcReader: NfcPairingReader = ...
    val pairingData = nfcReader.readPairingData(intent)
    if (pairingData != null) {
        lifecycleScope.launch {
            pairingHelper.findAndConnectDevice(pairingData)
        }
    }
}
```

## Flow Control

Модуль реализует механизм Flow Control для предотвращения переполнения буфера устройства:

1. Приложение отправляет пакет данных (OTA_CHUNK или ADD_COMMAND)
2. Устройство переходит в состояние `STATE:PROCESSING`
3. После обработки устройство отправляет `STATE:READY_FOR_DATA`
4. Приложение отправляет следующий пакет

Это обеспечивает надежную передачу больших объемов данных (OTA прошивки, анимации).

## Политики отказоустойчивости

### Автоматическое переподключение

- До 5 попыток переподключения
- Экспоненциальная задержка (1s → 2s → 4s → 8s → 16s)
- Максимальная задержка 30 секунд

### Повторы команд

- До 3 попыток для каждой команды
- Экспоненциальная задержка (1s → 2s → 4s)
- Максимальная задержка 5 секунд

## Таймауты

- Подключение: 15 секунд
- Обычная команда: 10 секунд
- OTA чанк: 30 секунд
- Discovery сервисов: 10 секунд

## Разрешения

### Android 12+
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### Android 11 и ниже
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Dependency Injection

Модуль использует Hilt для DI. Просто инжектируйте `AmuletBleManager` в ваши компоненты:

```kotlin
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val bleManager: AmuletBleManager
) : ViewModel() {
    // ...
}
```

## Тестирование

Для тестирования создайте mock реализацию `AmuletBleManager` или используйте тестовый дубль.

## См. также

- `docs/20_DATA_LAYER/03_BLE_PROTOCOL.md` - полная спецификация протокола
- `docs/10_ARCHITECTURE/01_ARCHITECTURE_OVERVIEW.md` - архитектура приложения
