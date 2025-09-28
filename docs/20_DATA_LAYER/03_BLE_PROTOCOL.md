# BLE Протокол для Amulet

Данный документ описывает Bluetooth Low Energy (BLE) протокол для взаимодействия между мобильным приложением Amulet и физическим устройством амулета. Протокол спроектирован как масштабируемый и эффективный, обеспечивающий надежную связь для всех функций приложения.

## Содержание

1. [Часть 1: Низкоуровневый протокол (Контракт с прошивкой)](#часть-1-низкоуровневый-протокол-контракт-с-прошивкой)
2. [Часть 2: Высокоуровневый API (Контракт для остального приложения)](#часть-2-высокоуровневый-api-контракт-для-остального-приложения)

---

## Часть 1: Низкоуровневый протокол (Контракт с прошивкой)

### GATT Profile

Таблица сервисов и характеристик для амулета:

| Service UUID | Characteristic UUID | Свойства | Назначение |
|--------------|---------------------|----------|------------|
| `180F` (Battery Service) | `2A19` | Read, Notify | Уровень батареи (0-100%) |
| `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` (Nordic UART Service) | `6E400002-B5A3-F393-E0A9-E50E24DCCA9E` | Write | Командная характеристика (отправка команд) |
| `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` (Nordic UART Service) | `6E400003-B5A3-F393-E0A9-E50E24DCCA9E` | Notify | Уведомления от устройства |
| `12345678-1234-1234-1234-123456789ABC` (Amulet Device Service) | `12345678-1234-1234-1234-123456789ABD` | Read, Write | Информация об устройстве (серийный номер, версия) |
| `12345678-1234-1234-1234-123456789ABC` (Amulet Device Service) | `12345678-1234-1234-1234-123456789ABE` | Read, Notify | Статус устройства |
| `12345678-1234-1234-1234-123456789ABC` (Amulet Device Service) | `12345678-1234-1234-1234-123456789ABF` | Write | OTA команды |
| `12345678-1234-1234-1234-123456789ABC` (Amulet Device Service) | `12345678-1234-1234-1234-123456789AC0` | Write, Notify | Загрузка анимаций |

### Формат команд

#### Базовые команды

**Формат:** `COMMAND:PARAMETERS`

**Примеры команд:**

1. **Дыхательная практика:**
   ```
   BREATHING:00FF00:8000ms
   ```
   - `00FF00` - цвет в HEX (зеленый)
   - `8000ms` - длительность в миллисекундах

2. **Пульсация:**
   ```
   PULSE:FF0000:500ms:10
   ```
   - `FF0000` - цвет (красный)
   - `500ms` - интервал между пульсами
   - `10` - количество повторений

3. **Бегущие огни:**
   ```
   CHASE:00FF00:CW:500
   ```
   - `00FF00` - цвет
   - `CW` - направление (по часовой стрелке)
   - `500` - скорость (мс между шагами)

4. **Заполнение кольца:**
   ```
   FILL:FF0000:2000
   ```
   - `FF0000` - цвет
   - `2000` - длительность заполнения

5. **Спиннер:**
   ```
   SPINNER:00FF00,FF0000:100
   ```
   - `00FF00,FF0000` - два цвета
   - `100` - скорость вращения

6. **Прогресс-бар:**
   ```
   PROGRESS:00FF00:5
   ```
   - `00FF00` - цвет
   - `5` - количество активных диодов (из 8)

#### Команды управления светодиодами

7. **Установка кольца (SET_RING):**
   ```
   SET_RING:#FF0000:#000000:#000000:#000000:#000000:#000000:#000000:#000000
   ```
   - Устанавливает цвет для всех 8 светодиодов
   - Формат: `#RRGGBB` для каждого диода (0-7)

8. **Установка отдельного светодиода (SET_LED):**
   ```
   SET_LED:0:#FF0000
   ```
   - `0` - индекс светодиода (0-7)
   - `#FF0000` - цвет в HEX формате
   - **Эффективно** для управления одним диодом

9. **Очистка всех светодиодов (CLEAR_ALL):**
   ```
   CLEAR_ALL
   ```
   - Выключает все светодиоды

10. **Задержка (DELAY):**
    ```
    DELAY:1000
    ```
    - `1000` - задержка в миллисекундах
    - **Критически важно** для создания временных пауз в анимациях

#### OTA команды

1. **Начать OTA обновление:**
   ```
   START_OTA:2.1.0:1234567890abcdef
   ```
   - `2.1.0` - версия прошивки
   - `1234567890abcdef` - контрольная сумма

2. **Отправить чанк (с ожиданием подтверждения):**
   ```
   OTA_CHUNK:1:256:data...
   ```
   - `1` - номер чанка
   - `256` - размер чанка
   - `data...` - данные чанка
   - **ВАЖНО:** Отправляется только после получения `STATE:READY_FOR_DATA`

3. **Подтвердить чанк:**
   ```
   OTA_ACK:1
   ```

4. **Завершить OTA:**
   ```
   OTA_COMMIT
   ```

#### Команды анимации

**Важно:** Существует два типа анимаций с разными способами запуска:

##### Встроенные системные анимации (PLAY)

1. **Воспроизведение встроенной анимации:**
   ```
   PLAY:breath_square
   ```
   - `breath_square` - ID встроенной анимации, зашитой в прошивку
   - **Назначение:** Быстрый запуск предустановленных анимаций
   - **Примеры ID:** `breath_square`, `pulse_red`, `chase_blue`, `spinner_rainbow`

##### Пользовательские динамические анимации (PLAN_)

2. **Начать план анимации:**
   ```
   BEGIN_PLAN:unique_id
   ```

3. **Добавить команду в план (с ожиданием подтверждения):**
   ```
   ADD_COMMAND:1:BREATHING:00FF00:5000ms
   ```
   - `1` - порядковый номер команды
   - Остальное - команда
   - **ВАЖНО:** Отправляется только после получения `STATE:READY_FOR_DATA`

4. **Зафиксировать план:**
   ```
   COMMIT_PLAN:unique_id
   ```

5. **Отменить план:**
   ```
   ROLLBACK_PLAN:unique_id
   ```

**Различие между PLAY и PLAN_:**
- **`PLAY`** - для встроенных анимаций (быстрый доступ, известные ID)
- **`PLAN_`** - для пользовательских анимаций (динамическое создание, полный контроль)

#### Формат ответов

**Успешные ответы:**
```
OK:COMMAND_ID
```

**Ошибки:**
```
ERROR:COMMAND_ID:ERROR_CODE:DESCRIPTION
```

**Уведомления:**
```
NOTIFY:TYPE:DATA
```

**Примеры уведомлений:**
- `NOTIFY:BATTERY:85` - уровень батареи 85%
- `NOTIFY:STATUS:CHARGING` - устройство заряжается
- `NOTIFY:OTA:PROGRESS:50` - прогресс OTA 50%
- `NOTIFY:ANIMATION:COMPLETE` - анимация завершена

#### Управление потоком данных (Flow Control)

**Критически важно:** Для предотвращения переполнения буфера устройства и потери данных необходимо использовать механизм подтверждения готовности.

**Состояния устройства:**
- `STATE:READY_FOR_DATA` - устройство готово принять следующий пакет данных
- `STATE:PROCESSING` - устройство обрабатывает полученные данные
- `STATE:BUSY` - устройство занято, ожидание завершения операции
- `STATE:ERROR` - ошибка обработки, требуется повторная отправка

**Протокол Flow Control:**
1. Приложение отправляет пакет данных (OTA_CHUNK или ADD_COMMAND)
2. Устройство переходит в состояние `PROCESSING` и начинает обработку
3. После завершения обработки устройство отправляет `STATE:READY_FOR_DATA`
4. Приложение получает подтверждение и отправляет следующий пакет
5. При ошибке устройство отправляет `STATE:ERROR` с кодом ошибки

### Ключевые сценарии (Sequence Diagrams)

#### Подключение и "рукопожатие"

```mermaid
sequenceDiagram
    participant App as Мобильное приложение
    participant Device as Амулет
    
    App->>Device: Подключение по BLE
    Device-->>App: Подключено
    
    App->>Device: Чтение серийного номера
    Device-->>App: SERIAL:AMU-200-XYZ-001
    
    App->>Device: Чтение версии прошивки
    Device-->>App: FIRMWARE:2.0.0
    
    App->>Device: Чтение HARDWARE_VERSION
    Device-->>App: HARDWARE:200
    
    App->>Device: Тестовая команда (мигание)
    Device->>Device: Выполнение анимации
    Device-->>App: OK:TEST
    
    Note over App,Device: Устройство успешно идентифицировано
```

#### OTA-обновление с Flow Control

```mermaid
sequenceDiagram
    participant App as Мобильное приложение
    participant Device as Амулет
    participant Server as Сервер
    
    App->>Server: Проверка обновлений
    Server-->>App: Доступна версия 2.1.0
    
    App->>Device: START_OTA:2.1.0:checksum
    Device-->>App: OK:START_OTA
    Device-->>App: STATE:READY_FOR_DATA
    
    loop Для каждого чанка
        App->>Device: OTA_CHUNK:1:256:data
        Device-->>App: STATE:PROCESSING
        Device->>Device: Сохранение чанка
        Device-->>App: OTA_ACK:1
        Device-->>App: STATE:READY_FOR_DATA
    end
    
    App->>Device: OTA_COMMIT
    Device->>Device: Установка прошивки
    Device-->>App: NOTIFY:OTA:PROGRESS:100
    
    Device->>Device: Перезагрузка
    Device-->>App: NOTIFY:OTA:COMPLETE
```

#### Загрузка анимации с Flow Control

```mermaid
sequenceDiagram
    participant App as Мобильное приложение
    participant Device as Амулет
    
    App->>Device: BEGIN_PLAN:plan_123
    Device-->>App: OK:BEGIN_PLAN
    Device-->>App: STATE:READY_FOR_DATA
    
    App->>Device: ADD_COMMAND:1:BREATHING:00FF00:5000ms
    Device-->>App: STATE:PROCESSING
    Device-->>App: OK:ADD_COMMAND:1
    Device-->>App: STATE:READY_FOR_DATA
    
    App->>Device: ADD_COMMAND:2:PULSE:FF0000:1000ms:3
    Device-->>App: STATE:PROCESSING
    Device-->>App: OK:ADD_COMMAND:2
    Device-->>App: STATE:READY_FOR_DATA
    
    App->>Device: COMMIT_PLAN:plan_123
    Device->>Device: Выполнение анимации
    Device-->>App: OK:COMMIT_PLAN
    
    Device-->>App: NOTIFY:ANIMATION:COMPLETE
```

---

## Часть 2: Высокоуровневый API (Контракт для остального приложения)

### Интерфейс `AmuletBleManager`

```kotlin
interface AmuletBleManager {
    // Состояние подключения
    val connectionState: StateFlow<ConnectionState>
    
    // Уровень батареи
    val batteryLevel: Flow<Int>
    
    // Статус устройства
    val deviceStatus: Flow<DeviceStatus>
    
    // Состояние готовности устройства (Flow Control)
    val deviceReadyState: Flow<DeviceReadyState>
    
    // Подключение/отключение
    suspend fun connect(deviceId: String, autoReconnect: Boolean = true)
    suspend fun disconnect()
    
    // Отправка команд
    suspend fun sendCommand(command: AmuletCommand): BleResult
    
    // Загрузка анимации (с Flow Control)
    suspend fun uploadAnimation(plan: AnimationPlan): Flow<UploadProgress>
    
    // OTA обновления (с Flow Control)
    suspend fun startOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress>
    
    // Наблюдение за уведомлениями
    fun observeNotifications(type: NotificationType): Flow<ByteArray>
}
```

### Доменные модели

#### ConnectionState

```kotlin
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
    data object ServicesDiscovered : ConnectionState
    data class Reconnecting(val attempt: Int) : ConnectionState
    data class Failed(val cause: Throwable?) : ConnectionState
}
```

#### AmuletCommand

```kotlin
sealed interface AmuletCommand {
    data class Breathing(
        val color: Rgb,
        val durationMs: Int
    ) : AmuletCommand
    
    data class Pulse(
        val color: Rgb,
        val intervalMs: Int,
        val repeats: Int
    ) : AmuletCommand
    
    data class Chase(
        val color: Rgb,
        val direction: ChaseDirection,
        val speedMs: Int
    ) : AmuletCommand
    
    data class Fill(
        val color: Rgb,
        val durationMs: Int
    ) : AmuletCommand
    
    data class Spinner(
        val colors: List<Rgb>,
        val speedMs: Int
    ) : AmuletCommand
    
    data class Progress(
        val color: Rgb,
        val activeLeds: Int
    ) : AmuletCommand
    
    data class SetRing(
        val colors: List<Rgb>
    ) : AmuletCommand
    
    data class SetLed(
        val index: Int,
        val color: Rgb
    ) : AmuletCommand
    
    data object ClearAll : AmuletCommand
    
    data class Delay(
        val durationMs: Int
    ) : AmuletCommand
    
    data class Play(
        val patternId: String
    ) : AmuletCommand
    
    data class Custom(
        val command: String,
        val parameters: Map<String, String>
    ) : AmuletCommand
}

enum class ChaseDirection {
    CLOCKWISE, COUNTER_CLOCKWISE
}

data class Rgb(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    fun toHex(): String = "#%02X%02X%02X".format(red, green, blue)
}
```

#### UploadProgress

```kotlin
data class UploadProgress(
    val totalChunks: Int,
    val sentChunks: Int,
    val percent: Int,
    val state: UploadState
)

sealed interface UploadState {
    data object Preparing : UploadState
    data object Uploading : UploadState
    data object Committing : UploadState
    data object Completed : UploadState
    data class Failed(val cause: Throwable?) : UploadState
}
```

#### DeviceStatus

```kotlin
data class DeviceStatus(
    val serialNumber: String,
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean,
    val lastSeen: Long
)
```

#### AnimationPlan

```kotlin
data class AnimationPlan(
    val id: String,
    val commands: List<AmuletCommand>,
    val estimatedDurationMs: Long,
    val hardwareVersion: Int
)
```

#### BleResult

```kotlin
sealed interface BleResult {
    data object Success : BleResult
    data class Error(val code: String, val message: String) : BleResult
}
```

#### DeviceReadyState (Flow Control)

```kotlin
sealed interface DeviceReadyState {
    data object ReadyForData : DeviceReadyState
    data object Processing : DeviceReadyState
    data object Busy : DeviceReadyState
    data class Error(val code: String, val message: String) : DeviceReadyState
}
```

#### FlowControlManager

```kotlin
class FlowControlManager {
    private val _readyState = MutableStateFlow<DeviceReadyState>(DeviceReadyState.Busy)
    val readyState: StateFlow<DeviceReadyState> = _readyState.asStateFlow()
    
    private val pendingOperations = mutableListOf<() -> Unit>()
    private var isProcessing = false
    
    suspend fun waitForReady(): DeviceReadyState {
        return readyState.first { it is DeviceReadyState.ReadyForData }
    }
    
    suspend fun executeWithFlowControl(operation: suspend () -> Unit) {
        waitForReady()
        _readyState.value = DeviceReadyState.Processing
        
        try {
            operation()
        } finally {
            _readyState.value = DeviceReadyState.ReadyForData
        }
    }
    
    fun handleDeviceState(state: String) {
        when (state) {
            "STATE:READY_FOR_DATA" -> _readyState.value = DeviceReadyState.ReadyForData
            "STATE:PROCESSING" -> _readyState.value = DeviceReadyState.Processing
            "STATE:BUSY" -> _readyState.value = DeviceReadyState.Busy
            "STATE:ERROR" -> _readyState.value = DeviceReadyState.Error("DEVICE_ERROR", "Device reported error")
        }
    }
}
```

### Политики отказоустойчивости

#### Автоматическое переподключение

```kotlin
class AmuletBleManagerImpl : AmuletBleManager {
    private val reconnectPolicy = ReconnectPolicy(
        maxAttempts = 5,
        baseDelayMs = 1000,
        maxDelayMs = 30000,
        backoffMultiplier = 2.0
    )
    
    private suspend fun attemptReconnection() {
        var attempt = 0
        var delay = reconnectPolicy.baseDelayMs
        
        while (attempt < reconnectPolicy.maxAttempts) {
            try {
                connect(deviceId, autoReconnect = false)
                return // Успешное подключение
            } catch (e: Exception) {
                attempt++
                if (attempt < reconnectPolicy.maxAttempts) {
                    delay = minOf(
                        delay * reconnectPolicy.backoffMultiplier,
                        reconnectPolicy.maxDelayMs
                    )
                    delay(delay)
                }
            }
        }
        
        // Все попытки исчерпаны
        _connectionState.value = ConnectionState.Failed(
            cause = Exception("Failed to reconnect after ${reconnectPolicy.maxAttempts} attempts")
        )
    }
}
```

#### Таймауты команд

```kotlin
class CommandTimeoutPolicy {
    companion object {
        const val DEFAULT_TIMEOUT_MS = 10000L
        const val OTA_TIMEOUT_MS = 30000L
        const val ANIMATION_TIMEOUT_MS = 60000L
    }
    
    fun getTimeoutForCommand(command: AmuletCommand): Long {
        return when (command) {
            is AmuletCommand.Breathing -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Pulse -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Chase -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Fill -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Spinner -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Progress -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.SetRing -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.SetLed -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.ClearAll -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Delay -> command.durationMs.toLong() + 1000L // Время задержки + буфер
            is AmuletCommand.Play -> DEFAULT_TIMEOUT_MS
            is AmuletCommand.Custom -> DEFAULT_TIMEOUT_MS
        }
    }
}
```

#### Обработка ошибок

```kotlin
sealed interface BleError : AppError {
    data object DeviceNotFound : BleError
    data object ConnectionFailed : BleError
    data object ServiceDiscoveryFailed : BleError
    data object WriteFailed : BleError
    data object ReadFailed : BleError
    data class CommandTimeout(val command: String) : BleError
    data object DeviceDisconnected : BleError
    data object InsufficientSpace : BleError
    data object InvalidCommand : BleError
}
```

#### Политики повторов

```kotlin
class RetryPolicy {
    companion object {
        const val MAX_RETRIES = 3
        const val BASE_DELAY_MS = 1000L
        const val MAX_DELAY_MS = 5000L
    }
    
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = MAX_RETRIES
    ): T {
        var lastException: Exception? = null
        var delay = BASE_DELAY_MS
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(delay)
                    delay = minOf(delay * 2, MAX_DELAY_MS)
                }
            }
        }
        
        throw lastException ?: Exception("Operation failed after $maxRetries attempts")
    }
}
```

#### Flow Control с повторными попытками

```kotlin
class FlowControlRetryPolicy(
    private val flowControlManager: FlowControlManager,
    private val retryPolicy: RetryPolicy
) {
    suspend fun <T> executeWithFlowControlAndRetry(
        operation: suspend () -> T,
        maxRetries: Int = 3
    ): T {
        return retryPolicy.executeWithRetry(
            operation = {
                flowControlManager.executeWithFlowControl {
                    operation()
                }
            },
            maxRetries = maxRetries
        )
    }
    
    suspend fun sendDataWithFlowControl(
        data: ByteArray,
        sendOperation: suspend (ByteArray) -> Unit
    ) {
        flowControlManager.executeWithFlowControl {
            sendOperation(data)
        }
    }
}
```

#### Примеры использования PLAY команд

```kotlin
// Быстрый запуск встроенной анимации дыхания
val breathingCommand = AmuletCommand.Play("breath_square")
bleManager.sendCommand(breathingCommand)

// Запуск встроенной пульсации
val pulseCommand = AmuletCommand.Play("pulse_red")
bleManager.sendCommand(pulseCommand)

// Запуск встроенного спиннера
val spinnerCommand = AmuletCommand.Play("spinner_rainbow")
bleManager.sendCommand(spinnerCommand)
```

#### Последовательность команд для "секретного кода"

```
BEGIN_PLAN:secret_code_001
STATE:READY_FOR_DATA
ADD_COMMAND:1:SET_LED:0:#FF0000
STATE:READY_FOR_DATA
ADD_COMMAND:2:DELAY:500
STATE:READY_FOR_DATA
ADD_COMMAND:3:SET_LED:0:#000000
STATE:READY_FOR_DATA
ADD_COMMAND:4:DELAY:500
STATE:READY_FOR_DATA
ADD_COMMAND:5:SET_LED:0:#FF0000
STATE:READY_FOR_DATA
ADD_COMMAND:6:DELAY:500
STATE:READY_FOR_DATA
ADD_COMMAND:7:SET_LED:0:#000000
STATE:READY_FOR_DATA
ADD_COMMAND:8:DELAY:1000
STATE:READY_FOR_DATA
ADD_COMMAND:9:SET_LED:4:#00FF00
STATE:READY_FOR_DATA
ADD_COMMAND:10:DELAY:1000
STATE:READY_FOR_DATA
ADD_COMMAND:11:SET_LED:4:#000000
STATE:READY_FOR_DATA
COMMIT_PLAN:secret_code_001
```

### Интеграция с архитектурой

#### Использование в UseCase с Flow Control

```kotlin
class SendAnimationUseCase(
    private val bleManager: AmuletBleManager,
    private val patternCompiler: PatternCompiler,
    private val flowControlRetryPolicy: FlowControlRetryPolicy
) {
    suspend fun execute(pattern: PatternSpec, deviceId: String): Result<Unit, AppError> {
        return try {
            val plan = patternCompiler.compile(pattern, deviceId)
            
            // Загрузка анимации с Flow Control
            bleManager.uploadAnimation(plan).collect { progress ->
                when (progress.state) {
                    is UploadState.Preparing -> {
                        // Ожидание готовности устройства
                        flowControlRetryPolicy.executeWithFlowControlAndRetry {
                            // Подготовка к загрузке
                        }
                    }
                    is UploadState.Uploading -> {
                        // Отправка данных с контролем потока
                        flowControlRetryPolicy.sendDataWithFlowControl(
                            data = progress.data,
                            sendOperation = { data -> 
                                bleManager.sendCommand(AmuletCommand.Custom("ADD_COMMAND", mapOf("data" to data.toString())))
                            }
                        )
                    }
                    is UploadState.Completed -> {
                        // Анимация успешно загружена
                    }
                    is UploadState.Failed -> {
                        throw Exception("Upload failed: ${progress.state.cause}")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapExceptionToAppError(e))
        }
    }
}
```

### Конфигурация и настройки

#### Настройки BLE

```kotlin
data class BleConfiguration(
    val connectionTimeoutMs: Long = 10000,
    val serviceDiscoveryTimeoutMs: Long = 5000,
    val commandTimeoutMs: Long = 10000,
    val maxRetries: Int = 3,
    val autoReconnect: Boolean = true,
    val reconnectDelayMs: Long = 1000,
    val maxReconnectAttempts: Int = 5
)
```

#### Логирование и мониторинг

```kotlin
class BleLogger {
    fun logConnection(deviceId: String, success: Boolean) {
        Log.d("BLE", "Connection to $deviceId: ${if (success) "success" else "failed"}")
    }
    
    fun logCommand(command: AmuletCommand, result: BleResult) {
        Log.d("BLE", "Command ${command::class.simpleName}: $result")
    }
    
    fun logError(error: BleError, context: String) {
        Log.e("BLE", "Error in $context: $error")
    }
}
```

### Тестирование

#### Unit тесты

```kotlin
class AmuletBleManagerTest {
    @Test
    fun `should connect to device successfully`() = runTest {
        val mockBleClient = MockBleClient()
        val manager = AmuletBleManagerImpl(mockBleClient)
        
        manager.connect("test-device")
        
        assertThat(manager.connectionState.value).isInstanceOf(ConnectionState.Connected::class.java)
    }
    
    @Test
    fun `should retry connection on failure`() = runTest {
        val mockBleClient = MockBleClient().apply {
            connectionResult = Result.failure(Exception("Connection failed"))
        }
        val manager = AmuletBleManagerImpl(mockBleClient)
        
        manager.connect("test-device")
        
        // Проверяем, что была попытка переподключения
        assertThat(mockBleClient.connectionAttempts).isGreaterThan(1)
    }
    
    @Test
    fun `should send LED commands correctly`() = runTest {
        val mockBleClient = MockBleClient()
        val manager = AmuletBleManagerImpl(mockBleClient)
        
        val setLedCommand = AmuletCommand.SetLed(0, Rgb(255, 0, 0))
        val result = manager.sendCommand(setLedCommand)
        
        assertThat(result).isInstanceOf(BleResult.Success::class.java)
        assertThat(mockBleClient.lastCommand).isEqualTo("SET_LED:0:#FF0000")
    }
    
    @Test
    fun `should handle delay commands with correct timeout`() = runTest {
        val mockBleClient = MockBleClient()
        val manager = AmuletBleManagerImpl(mockBleClient)
        
        val delayCommand = AmuletCommand.Delay(2000)
        val timeout = TimeoutPolicy.getTimeoutForCommand(delayCommand)
        
        assertThat(timeout).isEqualTo(3000L) // 2000ms + 1000ms buffer
    }
    
    @Test
    fun `should create secret code animation plan`() = runTest {
        val secretCodePlan = AnimationPlan(
            id = "secret_code_test",
            commands = listOf(
                AmuletCommand.SetLed(0, Rgb(255, 0, 0)), // Красный верхний диод
                AmuletCommand.Delay(500),
                AmuletCommand.SetLed(0, Rgb(0, 0, 0)),   // Погасить
                AmuletCommand.Delay(500),
                AmuletCommand.SetLed(0, Rgb(255, 0, 0)), // Красный снова
                AmuletCommand.Delay(500),
                AmuletCommand.SetLed(0, Rgb(0, 0, 0)),   // Погасить
                AmuletCommand.Delay(1000),               // Пауза между последовательностями
                AmuletCommand.SetLed(4, Rgb(0, 255, 0)), // Зеленый нижний диод
                AmuletCommand.Delay(1000),
                AmuletCommand.SetLed(4, Rgb(0, 0, 0))    // Погасить
            )
        )
        
        assertThat(secretCodePlan.commands).hasSize(10)
        assertThat(secretCodePlan.id).isEqualTo("secret_code_test")
    }
    
    @Test
    fun `should send PLAY commands correctly`() = runTest {
        val mockBleClient = MockBleClient()
        val manager = AmuletBleManagerImpl(mockBleClient)
        
        val playCommand = AmuletCommand.Play("breath_square")
        val result = manager.sendCommand(playCommand)
        
        assertThat(result).isInstanceOf(BleResult.Success::class.java)
        assertThat(mockBleClient.lastCommand).isEqualTo("PLAY:breath_square")
    }
    
    @Test
    fun `should distinguish between PLAY and PLAN commands`() = runTest {
        // PLAY команда - для встроенных анимаций
        val playCommand = AmuletCommand.Play("pulse_red")
        assertThat(playCommand.patternId).isEqualTo("pulse_red")
        
        // PLAN команды - для пользовательских анимаций
        val planCommand = AmuletCommand.Custom("BEGIN_PLAN", mapOf("id" to "user_animation_001"))
        assertThat(planCommand.command).isEqualTo("BEGIN_PLAN")
        assertThat(planCommand.parameters["id"]).isEqualTo("user_animation_001")
    }
}

class FlowControlManagerTest {
    @Test
    fun `should wait for ready state before executing operation`() = runTest {
        val flowControlManager = FlowControlManager()
        
        // Изначально устройство занято
        assertThat(flowControlManager.readyState.value).isInstanceOf(DeviceReadyState.Busy::class.java)
        
        // Симулируем готовность устройства
        flowControlManager.handleDeviceState("STATE:READY_FOR_DATA")
        
        var operationExecuted = false
        flowControlManager.executeWithFlowControl {
            operationExecuted = true
        }
        
        assertThat(operationExecuted).isTrue()
    }
    
    @Test
    fun `should handle device error state`() = runTest {
        val flowControlManager = FlowControlManager()
        
        flowControlManager.handleDeviceState("STATE:ERROR")
        
        assertThat(flowControlManager.readyState.value).isInstanceOf(DeviceReadyState.Error::class.java)
    }
}
```

#### Интеграционные тесты

```kotlin
class BleIntegrationTest {
    @Test
    fun `should upload animation successfully`() = runTest {
        val manager = createRealBleManager()
        val plan = createTestAnimationPlan()
        
        val progressFlow = manager.uploadAnimation(plan)
        val progressList = progressFlow.toList()
        
        assertThat(progressList.last().state).isInstanceOf(UploadState.Completed::class.java)
    }
}
```

---

## Заключение

Данный BLE протокол обеспечивает:

1. **Масштабируемость** - поддержка различных версий оборудования и прошивок
2. **Эффективность** - оптимизированные команды и минимальный трафик
3. **Надежность** - автоматические повторы, таймауты и обработка ошибок
4. **Гибкость** - поддержка как простых команд, так и сложных анимаций
5. **Безопасность** - валидация команд и защита от некорректных данных
6. **Критически важно: Flow Control** - механизм управления потоком данных предотвращает переполнение буфера устройства и потерю данных при OTA-обновлениях и загрузке анимаций
7. **Точное управление светодиодами** - команды `SET_LED` и `DELAY` для создания сложных анимаций и "секретных кодов"
8. **Временной контроль** - возможность создания пауз и синхронизации анимаций

### Ключевые особенности Flow Control:

- **Предотвращение потери данных** - устройство подтверждает готовность перед получением следующего пакета
- **Стабильная работа в реальных условиях** - протокол работает не только в идеальных лабораторных условиях
- **Автоматическое восстановление** - при ошибках устройство может запросить повторную отправку данных
- **Мониторинг состояния** - приложение всегда знает текущее состояние устройства

### Возможности для сложных анимаций:

- **Индивидуальное управление светодиодами** через `SET_LED:index:color`
- **Точные временные паузы** через `DELAY:duration_ms`
- **Эффективная загрузка анимаций** через механизм `PLAN_` команд
- **Реализация "секретных кодов"** - сложные последовательности с точным временным контролем

### Два типа анимаций:

- **Встроенные анимации (PLAY)** - быстрый доступ к предустановленным анимациям через `PLAY:pattern_id`
- **Пользовательские анимации (PLAN_)** - динамическое создание сложных анимаций через `BEGIN_PLAN/ADD_COMMAND/COMMIT_PLAN`

### Архитектурные принципы:

- **Инкапсуляция сложности** - `FlowControlManager` является внутренней деталью `AmuletBleManagerImpl`
- **Простой публичный API** - UseCase взаимодействует только с `AmuletBleManager` через простой интерфейс
- **Скрытие реализации** - Flow Control автоматически управляется внутри менеджера
- **Четкое разделение ответственности** - UseCase не знает о внутренних деталях BLE протокола

Протокол спроектирован с учетом требований архитектуры Clean Architecture и обеспечивает четкое разделение между низкоуровневым взаимодействием с устройством и высокоуровневой бизнес-логикой приложения. **Flow Control является критически важным компонентом для стабильной работы протокола в продакшене, но его сложность полностью скрыта от UseCase-ов.**

