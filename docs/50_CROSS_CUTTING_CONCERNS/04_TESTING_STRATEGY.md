# Стратегия тестирования

Данный документ описывает комплексную стратегию тестирования для приложения Amulet, основанную на архитектуре Clean Architecture с многослойной структурой модулей. Стратегия охватывает все уровни тестирования от unit-тестов до E2E-сценариев.

## 1. Пирамида тестирования

```
        /\
       /  \
      / E2E \     <- Меньше всего тестов, критичные пользовательские сценарии
     /______\
    /        \
   /Integration\ <- Тестирование взаимодействия между слоями
  /____________\
 /              \
/    Unit Tests   \ <- Основа пирамиды, максимальное покрытие
/__________________\
```

## 2. Unit-тесты

### 2.1. Где размещаются

**Модуль `:shared` (Domain Layer):**
- `src/commonTest/kotlin/` - UseCase'ы, бизнес-логика, мапперы
- `src/commonTest/kotlin/` - Редьюсеры состояний, валидаторы

**Модули `:data:*`:**
- `src/test/kotlin/` - Реализации репозиториев, мапперы DTO ↔ Domain

**Модули `:feature:*`:**
- `src/test/kotlin/` - ViewModel'и, обработчики событий

### 2.2. Библиотеки и инструменты

```kotlin
// В :shared/build.gradle.kts
dependencies {
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    commonTestImplementation("app.cash.turbine:turbine:1.0.0") // Для тестирования Flow
    commonTestImplementation("io.mockk:mockk:1.13.8") // Моки
}

// В Android модулях
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")
}
```

### 2.3. Тестирование UseCase'ов

```kotlin
// Пример: SendHugUseCaseTest
class SendHugUseCaseTest {
    
    @Test
    fun `should send hug successfully`() = runTest {
        // Given
        val request = SendHugRequest(recipientId = "user2", patternId = "pattern1")
        val expectedHug = Hug(id = "hug1", fromUserId = "user1", toUserId = "user2")
        
        val hugsRepository = mockk<HugsRepository> {
            coEvery { sendHug(request) } returns Result.success(expectedHug)
        }
        
        val useCase = SendHugUseCase(hugsRepository)
        
        // When
        val result = useCase(request)
        
        // Then
        assertThat(result.isSuccess()).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedHug)
    }
    
    @Test
    fun `should handle rate limit error`() = runTest {
        // Given
        val request = SendHugRequest(recipientId = "user2", patternId = "pattern1")
        val hugsRepository = mockk<HugsRepository> {
            coEvery { sendHug(request) } returns Result.failure(AppError.RateLimited)
        }
        
        val useCase = SendHugUseCase(hugsRepository)
        
        // When
        val result = useCase(request)
        
        // Then
        assertThat(result.isFailure()).isTrue()
        assertThat(result.errorOrNull()).isInstanceOf(AppError.RateLimited::class.java)
    }
}
```

### 2.4. Тестирование ViewModel'ей

```kotlin
// Пример: ProfileViewModelTest
class ProfileViewModelTest {
    
    @Test
    fun `should load profile successfully`() = runTest {
        // Given
        val user = User(id = "user1", displayName = "Test User")
        val getUserProfileUseCase = mockk<GetUserProfileUseCase> {
            coEvery { invoke() } returns Result.success(user)
        }
        
        val viewModel = ProfileViewModel(getUserProfileUseCase, mockk())
        
        // When
        viewModel.handleEvent(ProfileEvent.LoadProfile)
        
        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            
            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.user).isEqualTo(user)
            assertThat(finalState.error).isNull()
        }
    }
    
    @Test
    fun `should emit side effect on successful save`() = runTest {
        // Given
        val updateUseCase = mockk<UpdateUserProfileUseCase> {
            coEvery { invoke(any()) } returns Result.success(mockk())
        }
        
        val viewModel = ProfileViewModel(mockk(), updateUseCase)
        
        // When
        viewModel.handleEvent(ProfileEvent.StartEditing)
        viewModel.handleEvent(ProfileEvent.UpdateName("New Name"))
        viewModel.handleEvent(ProfileEvent.SaveChanges)
        
        // Then
        viewModel.sideEffects.test {
            val sideEffect = awaitItem()
            assertThat(sideEffect).isInstanceOf(ProfileSideEffect.ShowSnackbar::class.java)
        }
    }
}
```

### 2.5. Тестирование мапперов

```kotlin
// Пример: HugMapperTest
class HugMapperTest {
    
    @Test
    fun `should map HugDto to Hug domain model`() {
        // Given
        val dto = HugDto(
            id = "hug1",
            fromUserId = "user1",
            toUserId = "user2",
            createdAt = Timestamp(1234567890)
        )
        
        // When
        val domain = dto.toDomain()
        
        // Then
        assertThat(domain.id).isEqualTo("hug1")
        assertThat(domain.fromUserId).isEqualTo("user1")
        assertThat(domain.toUserId).isEqualTo("user2")
        assertThat(domain.createdAt).isEqualTo(1234567890L)
    }
}
```

## 3. Интеграционные тесты

### 3.1. Где размещаются

**Модули `:data:*`:**
- `src/androidTest/kotlin/` - Тестирование репозиториев с реальными источниками данных

**Модуль `:core:database`:**
- `src/androidTest/kotlin/` - Тестирование DAO, миграций БД

**Модуль `:core:network`:**
- `src/test/kotlin/` - Тестирование API с MockWebServer

### 3.2. Библиотеки и инструменты

```kotlin
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.5.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
```

### 3.3. Тестирование репозиториев с in-memory БД

```kotlin
// Пример: HugsRepositoryImplTest
@RunWith(AndroidJUnit4::class)
class HugsRepositoryImplTest {
    
    private lateinit var database: AmuletDatabase
    private lateinit var repository: HugsRepositoryImpl
    private lateinit var mockWebServer: MockWebServer
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AmuletDatabase::class.java
        ).build()
        
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        val apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HugsApiService::class.java)
        
        repository = HugsRepositoryImpl(
            hugsDao = database.hugsDao(),
            apiService = apiService,
            mapper = HugMapper()
        )
    }
    
    @After
    fun tearDown() {
        database.close()
        mockWebServer.shutdown()
    }
    
    @Test
    fun `should fetch hugs from API and cache in database`() = runTest {
        // Given
        val apiResponse = HugsResponse(
            hugs = listOf(
                HugDto(id = "hug1", fromUserId = "user1", toUserId = "user2")
            )
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(apiResponse))
        )
        
        // When
        val result = repository.getHugs()
        
        // Then
        assertThat(result.isSuccess()).isTrue()
        val hugs = result.getOrNull()!!
        assertThat(hugs).hasSize(1)
        assertThat(hugs[0].id).isEqualTo("hug1")
        
        // Verify cached in database
        val cachedHugs = database.hugsDao().getAllHugs()
        assertThat(cachedHugs).hasSize(1)
    }
}
```

### 3.4. Тестирование API с MockWebServer

```kotlin
// Пример: HugsApiServiceTest
class HugsApiServiceTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: HugsApiService
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HugsApiService::class.java)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `should send hug successfully`() = runTest {
        // Given
        val request = SendHugRequest(recipientId = "user2", patternId = "pattern1")
        val response = SendHugResponse(
            hug = HugDto(id = "hug1", fromUserId = "user1", toUserId = "user2")
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(response))
        )
        
        // When
        val result = apiService.sendHug(request)
        
        // Then
        assertThat(result.isSuccessful).isTrue()
        assertThat(result.body()?.hug?.id).isEqualTo("hug1")
    }
    
    @Test
    fun `should handle rate limit error`() = runTest {
        // Given
        val request = SendHugRequest(recipientId = "user2", patternId = "pattern1")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "60")
        )
        
        // When
        val result = apiService.sendHug(request)
        
        // Then
        assertThat(result.isSuccessful).isFalse()
        assertThat(result.code()).isEqualTo(429)
    }
}
```

### 3.5. Тестирование миграций БД

```kotlin
// Пример: DatabaseMigrationTest
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    @Test
    fun `should migrate from version 1 to 2`() {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AmuletDatabase::class.java
        )
        
        // Create database at version 1
        val db = helper.createDatabase(TEST_DB, 1)
        db.close()
        
        // Migrate to version 2
        helper.runMigrationsAndValidate(TEST_DB, 2, true)
        
        // Verify migration
        val migratedDb = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AmuletDatabase::class.java,
            TEST_DB
        ).build()
        
        // Test that new columns exist
        val cursor = migratedDb.query("SELECT * FROM hugs LIMIT 1")
        assertThat(cursor.columnNames).contains("new_column")
        
        migratedDb.close()
    }
}
```

## 4. UI-тесты (Compose)

### 4.1. Где размещаются

**Модули `:feature:*`:**
- `src/androidTest/kotlin/` - Тестирование отдельных экранов и компонентов

**Модуль `:core:design`:**
- `src/androidTest/kotlin/` - Тестирование дизайн-системы

### 4.2. Библиотеки и инструменты

```kotlin
dependencies {
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
```

### 4.3. Тестирование отдельных компонентов

```kotlin
// Пример: HugItemTest
@RunWith(AndroidJUnit4::class)
class HugItemTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `should display hug information correctly`() {
        // Given
        val hug = Hug(
            id = "hug1",
            fromUserId = "user1",
            toUserId = "user2",
            createdAt = 1234567890L
        )
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                HugItem(
                    hug = hug,
                    onClick = {}
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("From: user1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("To: user2")
            .assertIsDisplayed()
    }
    
    @Test
    fun `should call onClick when clicked`() {
        // Given
        val hug = Hug(id = "hug1", fromUserId = "user1", toUserId = "user2")
        var clicked = false
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                HugItem(
                    hug = hug,
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("From: user1")
            .performClick()
        
        // Then
        assertThat(clicked).isTrue()
    }
}
```

### 4.4. Тестирование целых экранов

```kotlin
// Пример: ProfileScreenTest
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `should display loading state initially`() {
        // Given
        val viewModel = mockk<ProfileViewModel> {
            every { uiState } returns MutableStateFlow(
                ProfileState(isLoading = true)
            ).asStateFlow()
        }
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Loading...")
            .assertIsDisplayed()
    }
    
    @Test
    fun `should display user profile when loaded`() {
        // Given
        val user = User(id = "user1", displayName = "Test User")
        val viewModel = mockk<ProfileViewModel> {
            every { uiState } returns MutableStateFlow(
                ProfileState(isLoading = false, user = user)
            ).asStateFlow()
        }
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Test User")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Loading...")
            .assertDoesNotExist()
    }
    
    @Test
    fun `should handle edit button click`() {
        // Given
        val user = User(id = "user1", displayName = "Test User")
        val viewModel = mockk<ProfileViewModel> {
            every { uiState } returns MutableStateFlow(
                ProfileState(isLoading = false, user = user)
            ).asStateFlow()
            every { handleEvent(any()) } just Runs
        }
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        composeTestRule
            .onNodeWithText("Edit")
            .performClick()
        
        // Then
        verify { viewModel.handleEvent(ProfileEvent.StartEditing) }
    }
}
```

### 4.5. Тестирование навигации

```kotlin
// Пример: NavigationTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `should navigate to profile screen`() {
        // Given
        val navController = TestNavHostController(LocalContext.current)
        
        // When
        composeTestRule.setContent {
            AmuletTheme {
                AmuletNavHost(navController = navController)
            }
        }
        
        composeTestRule
            .onNodeWithText("Profile")
            .performClick()
        
        // Then
        assertThat(navController.currentDestination?.route).isEqualTo("profile")
    }
}
```

## 5. E2E-тесты

### 5.1. Где размещаются

**Модуль `:app`:**
- `src/androidTest/kotlin/e2e/` - Сквозные пользовательские сценарии

### 5.2. Библиотеки и инструменты

```kotlin
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
```

### 5.3. Тестирование полного флоу отправки объятия

```kotlin
// Пример: SendHugE2ETest
@RunWith(AndroidJUnit4::class)
class SendHugE2ETest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var mockWebServer: MockWebServer
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        // Настройка mock API
        setupMockApi()
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `should complete full hug sending flow`() {
        // 1. Пользователь открывает экран объятий
        onView(withText("Hugs"))
            .perform(click())
        
        // 2. Нажимает кнопку "Отправить объятие"
        onView(withId(R.id.fab_send_hug))
            .perform(click())
        
        // 3. Выбирает получателя
        onView(withText("John Doe"))
            .perform(click())
        
        // 4. Выбирает паттерн
        onView(withText("Warm Hug"))
            .perform(click())
        
        // 5. Нажимает "Отправить"
        onView(withText("Send"))
            .perform(click())
        
        // 6. Проверяет успешное сообщение
        onView(withText("Hug sent successfully!"))
            .check(matches(isDisplayed()))
        
        // 7. Проверяет, что объятие появилось в истории
        onView(withText("Sent to John Doe"))
            .check(matches(isDisplayed()))
    }
    
    private fun setupMockApi() {
        // Mock для получения списка пользователей
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "users": [
                            {"id": "user2", "displayName": "John Doe"}
                        ]
                    }
                """.trimIndent())
        )
        
        // Mock для отправки объятия
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "hug": {
                            "id": "hug1",
                            "fromUserId": "user1",
                            "toUserId": "user2",
                            "createdAt": 1234567890
                        }
                    }
                """.trimIndent())
        )
    }
}
```

### 5.4. Тестирование BLE подключения

```kotlin
// Пример: BleConnectionE2ETest
@RunWith(AndroidJUnit4::class)
class BleConnectionE2ETest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun `should connect to amulet device`() {
        // 1. Пользователь открывает настройки устройства
        onView(withText("Devices"))
            .perform(click())
        
        // 2. Нажимает "Подключить устройство"
        onView(withText("Connect Device"))
            .perform(click())
        
        // 3. Проверяет, что появился список устройств
        onView(withText("Amulet-200-XYZ-001"))
            .check(matches(isDisplayed()))
        
        // 4. Выбирает устройство
        onView(withText("Amulet-200-XYZ-001"))
            .perform(click())
        
        // 5. Проверяет процесс подключения
        onView(withText("Connecting..."))
            .check(matches(isDisplayed()))
        
        // 6. Проверяет успешное подключение
        onView(withText("Connected"))
            .check(matches(isDisplayed()))
        
        // 7. Проверяет отображение уровня батареи
        onView(withText("Battery: 85%"))
            .check(matches(isDisplayed()))
    }
}
```

### 5.5. Тестирование практик с BLE

```kotlin
// Пример: PracticeSessionE2ETest
@RunWith(AndroidJUnit4::class)
class PracticeSessionE2ETest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun `should complete breathing practice with device`() {
        // 1. Пользователь открывает библиотеку практик
        onView(withText("Library"))
            .perform(click())
        
        // 2. Выбирает категорию "Breathing"
        onView(withText("Breathing"))
            .perform(click())
        
        // 3. Выбирает практику "Square Breathing"
        onView(withText("Square Breathing"))
            .perform(click())
        
        // 4. Нажимает "Start Practice"
        onView(withText("Start Practice"))
            .perform(click())
        
        // 5. Проверяет, что началась сессия
        onView(withText("Practice in progress..."))
            .check(matches(isDisplayed()))
        
        // 6. Проверяет отображение прогресса
        onView(withId(R.id.progress_bar))
            .check(matches(isDisplayed()))
        
        // 7. Ждет завершения практики (или нажимает "Stop")
        onView(withText("Stop"))
            .perform(click())
        
        // 8. Проверяет экран завершения
        onView(withText("Practice completed!"))
            .check(matches(isDisplayed()))
        
        // 9. Проверяет статистику
        onView(withText("Duration: 5:00"))
            .check(matches(isDisplayed()))
    }
}
```

## 6. Тестирование производительности

### 6.1. Бенчмарки

```kotlin
// Пример: DatabaseBenchmark
@RunWith(AndroidJUnit4::class)
class DatabaseBenchmark {
    
    private lateinit var database: AmuletDatabase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AmuletDatabase::class.java
        ).build()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `should insert hugs efficiently`() {
        val hugs = (1..1000).map { i ->
            HugEntity(
                id = "hug$i",
                fromUserId = "user1",
                toUserId = "user2",
                createdAt = System.currentTimeMillis()
            )
        }
        
        val startTime = System.currentTimeMillis()
        
        hugs.forEach { hug ->
            database.hugsDao().insertHug(hug)
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Проверяем, что вставка 1000 записей занимает менее 1 секунды
        assertThat(duration).isLessThan(1000)
    }
}
```

## 7. Конфигурация CI/CD

### 7.1. GitHub Actions

```yaml
# .github/workflows/test.yml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run integration tests
        run: ./gradlew connectedAndroidTest

  ui-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run UI tests
        run: ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.amulet.ui.*
```

## 8. Метрики и покрытие

### 8.1. Цели покрытия

- **Unit-тесты:** ≥ 80% покрытие кода
- **Интеграционные тесты:** ≥ 60% покрытие критичных путей
- **UI-тесты:** Покрытие всех основных пользовательских сценариев
- **E2E-тесты:** Покрытие критичных бизнес-процессов

### 8.2. Отчеты покрытия

```kotlin
// В build.gradle.kts
android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
}

// Генерация отчетов
./gradlew jacocoTestReport
```

## 9. Рекомендации по реализации

### 9.1. Приоритеты тестирования

1. **Критичные бизнес-процессы** (отправка объятий, BLE подключение)
2. **Пользовательские сценарии** (онбординг, основные функции)
3. **Интеграции** (API, БД, BLE)
4. **UI компоненты** (дизайн-система, экраны)

### 9.2. Лучшие практики

- Используйте `runTest` для тестирования корутин
- Мокайте внешние зависимости
- Тестируйте как успешные, так и ошибочные сценарии
- Следите за производительностью тестов
- Автоматизируйте запуск тестов в CI/CD

### 9.3. Антипаттерны

- Не тестируйте детали реализации
- Не создавайте хрупкие тесты
- Не игнорируйте медленные тесты
- Не тестируйте сторонние библиотеки

Эта стратегия обеспечивает комплексное тестирование всех компонентов приложения Amulet, гарантируя качество и надежность продукта.


