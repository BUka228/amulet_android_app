# Архитектурное управление: автоматизация проверки и эволюции архитектуры

## 1. Введение: Наша философия поддержания архитектуры

**Цель:** Этот документ описывает инструменты и процессы, которые мы используем для автоматической проверки, обеспечения и эволюции архитектуры, описанной в этом репозитории. Наша цель — сделать соблюдение архитектуры автоматическим, а ее изменение — осознанным и задокументированным.

Архитектура Amulet приложения основана на Clean Architecture с четким разделением на слои и модули. Каждый модуль имеет свои ответственности и зависимости, которые должны строго соблюдаться. Автоматизация проверки архитектуры позволяет нам:

- **Предотвращать архитектурные нарушения** на стадии разработки
- **Обеспечивать консистентность** кода во всех модулях
- **Упрощать ревью кода** за счет автоматических проверок
- **Документировать архитектурные решения** и их обоснования
- **Облегчать онбординг** новых разработчиков

## 2. Detekt: Наш страж чистоты кода и архитектуры

**Роль:** Detekt — наш основной инструмент статического анализа. Он выполняет две функции:
1. Поддержание общего качества и стиля кода (сложность, форматирование).
2. **Проверка соблюдения архитектурных контрактов на уровне файлов.**

**Процесс:** `gradlew detekt` является обязательным шагом в CI, и PR с ошибками не будет принят. Настоятельно рекомендуется установка плагина для IDE для проверки во время разработки.

### Ключевые архитектурные правила

Эта таблица напрямую связывает наши архитектурные документы с конкретными кастомными правилами Detekt:

| Правило Detekt | Описание | Связанный документ |
| :--- | :--- | :--- |
| **`ViewModelDependsOnRepositoryRule`** | Проверяет, что ViewModel не зависит напрямую от Repository, а только от UseCase | `15_CONTRACTS/03_PRESENTATION_LAYER_CONTRACTS.md` |
| **`UseCaseNamingRule`** | Гарантирует, что все UseCase'ы имеют суффикс `UseCase` | `15_CONTRACTS/02_DOMAIN_LAYER_CONTRACTS.md` |
| **`FeatureDependsOnDataRule`** | Запрещает зависимость модуля `:feature:*` от модуля `:data:*` | `10_ARCHITECTURE/02_MODULARIZATION.md` |
| **`DomainLayerAndroidDependencyRule`** | Проверяет, что в модуле `:shared` нет импортов из `android.*` | `10_ARCHITECTURE/01_ARCHITECTURE_OVERVIEW.md` |
| **`RepositoryNamingRule`** | Проверяет соблюдение именования репозиториев (`*Repository` интерфейсы в `:shared`, `*RepositoryImpl` в `:data:*`) | `15_CONTRACTS/01_DATA_LAYER_CONTRACTS.md` |
| **`StateFlowExposureRule`** | Проверяет, что ViewModel экспонирует `StateFlow` для состояния, а не `MutableStateFlow` | `40_PRESENTATION_LAYER/01_UI_STATE_MANAGEMENT.md` |
| **`ErrorHandlingRule`** | Проверяет использование `Result<T, AppError>` вместо исключений в публичных API репозиториев | `50_CROSS_CUTTING_CONCERNS/01_ERROR_HANDLING.md` |
| **`KmpCompatibilityRule`** | Проверяет, что модуль `:shared` не использует платформенные зависимости | `10_ARCHITECTURE/01_ARCHITECTURE_OVERVIEW.md` |
| **`PatternElementNamingRule`** | Проверяет правильное именование элементов паттернов, включая `PatternElementSequence` | Архитектурный обзор (BLE/Pattern секции) |
| **`NavigationContractRule`** | Проверяет использование типобезопасных маршрутов вместо строковых констант | `10_ARCHITECTURE/04_NAVIGATION_STRATEGY.md` |

### Работа с Baseline

**Политика Baseline:** Baseline используется только для существующего легаси-кода. Весь новый код должен быть чистым. Baseline должен только уменьшаться со временем.

**Правила для Baseline:**
- Добавление новых нарушений в baseline запрещено
- При исправлении легаси кода соответствующие правила удаляются из baseline
- Цель — полная очистка baseline в течение релизных циклов
- Baseline ревьюится командой архитекторов ежемесячно

**Конфигурация Detekt:**
```yaml
# detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    LongParameterList: 1
    LongMethod: 1

processors:
  active: true
  exclude:
    - 'DetektProgressListener'

console-reports:
  active: true
  exclude:
    - 'ProjectStatisticsReport'

output-reports:
  active: true
  exclude:
    - 'TxtOutputReport'

custom-rules:
  active: true
  rules:
    ViewModelDependsOnRepositoryRule:
      active: true
    UseCaseNamingRule:
      active: true
    FeatureDependsOnDataRule:
      active: true
    DomainLayerAndroidDependencyRule:
      active: true
    RepositoryNamingRule:
      active: true
    StateFlowExposureRule:
      active: true
    ErrorHandlingRule:
      active: true
    KmpCompatibilityRule:
      active: true
    PatternElementNamingRule:
      active: true
    NavigationContractRule:
      active: true
```

## 3. ArchUnit: Гарант межмодульных границ

**Роль:** Если Detekt — это проверка на уровне файлов, то ArchUnit — это наш инструмент для написания **тестов для всей архитектуры**. Он дает 100% гарантию, что зависимости между слоями и модулями не нарушены.

**Расположение тестов:** Эти тесты находятся в отдельном модуле `:architecture-test`, чтобы не смешиваться с обычными тестами.

### Пример канонического теста

Этот пример демонстрирует проверку правил модульности из `02_MODULARIZATION.md`:

```kotlin
@Test
fun layers_dependencies_are_respected() {
    layeredArchitecture()
        .consideringAllDependencies()
        .layer("App").definedBy("..app..")
        .layer("Feature").definedBy("..feature..")
        .layer("Shared").definedBy("..shared..")
        .layer("Data").definedBy("..data..")
        .layer("Core").definedBy("..core..")

        .whereLayer("Feature").mayOnlyBeAccessedByLayers("App")
        .whereLayer("Data").mayOnlyBeAccessedByLayers("App")
        .whereLayer("Shared").mayOnlyBeAccessedByLayers("App", "Feature", "Data")
        .whereLayer("Core").mayOnlyBeAccessedByLayers("App", "Data")
        
        .whereLayer("Feature").mayNotAccessLayers("Data")
        .whereLayer("Shared").mayNotAccessLayers("App", "Feature", "Data", "Core")
        
        .check(importedClasses)
}

@Test
fun feature_modules_follow_dependency_rules() {
    classes()
        .that().resideInAPackage("..feature..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..shared..",
            "..core.design..",
            "kotlin..",
            "kotlinx..",
            "java..",
            "javax..",
            "androidx.compose..",
            "androidx.lifecycle..",
            "dagger.hilt.."
        )
        .check(importedClasses)
}

@Test
fun shared_module_is_kmp_compatible() {
    classes()
        .that().resideInAPackage("..shared..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "kotlin..",
            "kotlinx..",
            "java..",
            "javax..",
            "org.jetbrains.annotations.."
        )
        .andShould().notDependOnClassesThat()
        .resideInAnyPackage(
            "android..",
            "androidx..",
            "retrofit2..",
            "okhttp3..",
            "androidx.room.."
        )
        .check(importedClasses)
}

@Test
fun viewmodels_follow_contracts() {
    classes()
        .that().haveSimpleNameEndingWith("ViewModel")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..shared..",  // UseCase и домейные модели
            "androidx.lifecycle..",
            "kotlinx.coroutines..",
            "dagger.hilt..",
            "kotlin..",
            "javax.inject.."
        )
        .andShould().notDependOnClassesThat()
        .resideInAnyPackage(
            "..data..",     // Прямая зависимость запрещена
            "..core.network..",
            "..core.database..",
            "retrofit2..",
            "androidx.room.."
        )
        .check(importedClasses)
}

@Test
fun repositories_implementation_follow_contracts() {
    classes()
        .that().haveSimpleNameEndingWith("RepositoryImpl")
        .should().resideInAPackage("..data..")
        .andShould().implement(classes().that().haveSimpleNameEndingWith("Repository"))
        .andShould().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..shared..",
            "..core.network..",
            "..core.database..",
            "..core.ble..",
            "kotlin..",
            "kotlinx..",
            "retrofit2..",
            "androidx.room..",
            "dagger.hilt..",
            "javax.inject.."
        )
        .check(importedClasses)
}

@Test
fun usecases_follow_naming_and_dependency_rules() {
    classes()
        .that().haveSimpleNameEndingWith("UseCase")
        .should().resideInAPackage("..shared..")
        .andShould().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..shared..",     // Другие UseCase и репозитории
            "kotlin..",
            "kotlinx..",
            "javax.inject.."
        )
        .andShould().notDependOnClassesThat()
        .resideInAnyPackage(
            "..data..",
            "..core..",
            "..feature..",
            "android..",
            "androidx..",
            "retrofit2..",
            "androidx.room.."
        )
        .check(importedClasses)
}

@Test
fun error_handling_follows_contracts() {
    methods()
        .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Repository")
        .and().arePublic()
        .should().haveRawReturnType("com.michaelbull.result.Result")
        .orShould().haveRawReturnType("kotlinx.coroutines.flow.Flow")
        .check(importedClasses)
}
```

**Процесс:** Эти тесты запускаются вместе с остальными интеграционными тестами в CI и должны проходить перед мержем каждого PR.

## 4. ADR (Architectural Decision Records): Наш журнал архитектурных решений

**Роль:** ADR — это способ задокументировать **ПОЧЕМУ** мы приняли то или иное важное архитектурное решение. Это наш способ борьбы с потерей контекста и повторным обсуждением одних и тех же вопросов.

### Процесс работы с ADR

#### 4.1. Когда создавать ADR?

ADR создается при принятии любого решения, имеющего долгосрочные последствия для архитектуры:
- Выбор DI фреймворка (Hilt vs Koin)
- Изменение правил модульности
- Добавление новой крупной технологии
- Стратегия обработки ошибок
- Выбор архитектурных паттернов
- Принципы навигации между экранами
- Стратегии тестирования

#### 4.2. Как создавать ADR?

1. **Создание файла:** Создается новый файл `NNN-short-decision-title.md` в папке `docs/adr/`
2. **Обсуждение:** ADR пишется по шаблону и обсуждается командой в рамках Pull Request
3. **Принятие:** После мержа решение считается принятым
4. **Эволюция:** ADR может быть пересмотрен и заменен новым

#### 4.3. Шаблон ADR

```markdown
# ADR-001: Выбор DI фреймворков Hilt и Koin

* **Статус:** Принято
* **Дата:** 2025-10-02
* **Теги:** `dependency-injection`, `hilt`, `koin`, `kmp`
* **Связанные ADR:** ADR-002 (KMP стратегия)

## Контекст

Нам нужен DI-фреймворк для Android-части и для KMP-модуля `:shared`. Hilt — стандарт для Android, но он не работает в KMP. Koin — популярен в KMP сообществе.

### Рассматриваемые варианты:
1. Только Hilt (потребует обходных решений для `:shared`)
2. Только Koin (потеря интеграции с Android Jetpack)
3. Гибридный подход: Hilt + Koin

### Критерии выбора:
- Совместимость с KMP
- Интеграция с Android Jetpack
- Производительность
- Простота использования
- Поддержка сообщества

## Решение

Мы решили использовать гибридный подход: Hilt как основной фреймворк в Android-слоях (`:app`, `:feature`, `:data`) и Koin внутри `:shared`. В модуле `:app` будет создан "мост" для предоставления Hilt-зависимостей в Koin-граф и наоборот.

### Архитектура решения:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule {
    @Provides
    @Singleton
    fun provideKoin(app: Application): Koin {
        val koinApp = startKoin {
            androidContext(app)
            modules(sharedKoinModules() + androidDataModules)
        }
        return koinApp.koin
    }

    @Provides
    fun provideSendHugUseCase(koin: Koin): SendHugUseCase = koin.get()
}
```

## Последствия

### Плюсы:
- ✅ Используем нативные, стандартные инструменты для каждой платформы
- ✅ `:shared` остается полностью независимым от Android
- ✅ Полная интеграция с Jetpack (ViewModel, WorkManager, etc.)
- ✅ Готовность к добавлению iOS

### Минусы:
- ❌ Некоторое усложнение DI-графа из-за необходимости поддерживать "мост"
- ❌ Требует от разработчиков понимания обоих фреймворков
- ❌ Дополнительный слой абстракции

### Риски и митигации:
- **Риск:** Сложность отладки DI проблем
  **Митигация:** Четкие конвенции именования и документация моста
- **Риск:** Производительность из-за двойного разрешения зависимостей
  **Митигация:** Кэширование в мосте, lazy инициализация

## Альтернативы

### Только Hilt
- **Плюсы:** Простота, стандарт Android
- **Минусы:** Проблемы с KMP, сложные обходные пути

### Только Koin  
- **Плюсы:** Единая система, KMP поддержка
- **Минусы:** Потеря Jetpack интеграций, reflection overhead

## Применение

- **Немедленно:** Настроить Hilt в `:app` и Android модулях
- **Этап 1:** Настроить Koin в `:shared`
- **Этап 2:** Реализовать мост в `:app`
- **Этап 3:** Обновить документацию и примеры

## Мониторинг

Следующие метрики будут отслеживаться:
- Время старта приложения (влияние инициализации DI)
- Количество DI-связанных багов
- Время онбординга новых разработчиков

## Пересмотр

Это решение будет пересмотрено через 6 месяцев или при возникновении значительных проблем.
```

#### 4.4. Дополнительные примеры ADR

**ADR-002: Стратегия обработки ошибок**
```markdown
# ADR-002: Использование Result<T, AppError> для обработки ошибок

* **Статус:** Принято
* **Дата:** 2025-10-02
* **Теги:** `error-handling`, `result`, `typed-errors`
* **Связанные ADR:** ADR-001 (DI strategy)

## Контекст

Стандартный Kotlin `Result<T>` использует `Throwable`, что не позволяет типизированно обрабатывать доменные ошибки. Нужна стратегия для унифицированной обработки ошибок.

## Решение

Используем библиотеку [kotlin-result](https://github.com/michaelbull/kotlin-result) с типизированными ошибками `Result<T, AppError>`.

## Последствия

### Плюсы:
- ✅ Типобезопасная обработка ошибок
- ✅ Функциональный стиль программирования
- ✅ KMP совместимость
- ✅ Отсутствие exception'ов в публичных API

### Минусы:
- ❌ Дополнительная зависимость
- ❌ Необходимость обучения команды
```

**ADR-003: Стратегия навигации**
```markdown
# ADR-003: Типобезопасная навигация с Navigation Compose

* **Статус:** Принято  
* **Дата:** 2025-10-02
* **Теги:** `navigation`, `compose`, `type-safety`

## Контекст

Стандартная навигация через строковые пути подвержена ошибкам и плохо рефакторится.

## Решение

Используем type-safe routes с генерацией helper-функций и централизованными destination объектами.

## Последствия

### Плюсы:
- ✅ Compile-time безопасность
- ✅ Легкий рефакторинг
- ✅ Централизованное управление маршрутами

### Минусы:
- ❌ Больше boilerplate кода
- ❌ Более сложная настройка deep links
```

## 5. Интеграция инструментов в CI/CD

### 5.1. GitHub Actions Pipeline

```yaml
name: Architecture Governance

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main ]

jobs:
  detekt:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      
      - name: Run Detekt
        run: ./gradlew detekt
      
      - name: Upload Detekt Reports
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: detekt-reports
          path: build/reports/detekt/

  archunit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      
      - name: Run Architecture Tests
        run: ./gradlew :architecture-test:test
      
      - name: Upload ArchUnit Reports
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: archunit-reports
          path: architecture-test/build/reports/tests/

  dependency-guard:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Verify Module Dependencies
        run: ./gradlew dependencyGuard
```

### 5.2. Quality Gates

**Обязательные проверки для мержа PR:**
1. ✅ Все тесты ArchUnit проходят
2. ✅ Detekt не находит критических нарушений
3. ✅ Нет новых добавлений в baseline
4. ✅ Code coverage > 80% для новых файлов
5. ✅ Успешная сборка всех модулей

**Настройка Branch Protection Rules:**
```json
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Architecture Governance / detekt",
      "Architecture Governance / archunit", 
      "Architecture Governance / dependency-guard"
    ]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true
  }
}
```

## 6. Monitoring и Metrics

### 6.1. Архитектурные метрики

**Отслеживаемые метрики:**
- Количество архитектурных нарушений по типам
- Размер baseline файла Detekt
- Скорость исправления архитектурных технических долгов
- Время выполнения архитектурных тестов
- Количество inter-module зависимостей

**Dashboard метрик:**
```markdown
## Архитектурные Метрики (Обновляется еженедельно)

📊 **Качество архитектуры:**
- Detekt violations: 0 critical, 5 major, 12 minor
- ArchUnit tests: 45/45 passing ✅
- Baseline size: 89 (-3 from last week) 📉

📈 **Тренды:**
- Module coupling: 0.15 (target: <0.2) ✅  
- Test coverage: 87% (target: >80%) ✅
- Build time: 3m 45s (target: <5m) ✅

🎯 **Цели на следующий спринт:**
- Убрать 10 legacy violations из baseline
- Добавить 3 новых ArchUnit теста
- Улучшить документацию ADR
```

### 6.2. Автоматические отчеты

**Еженедельный отчет команде:**
```kotlin
// Скрипт генерации отчета (Gradle task)
task generateArchitectureReport {
    doLast {
        def detektResults = parseDetektReport()
        def archunitResults = parseArchUnitReport()
        def dependencyResults = analyzeDependencies()
        
        def report = """
        # Weekly Architecture Report
        
        ## Detekt Results
        - Critical: ${detektResults.critical}
        - Major: ${detektResults.major}  
        - Minor: ${detektResults.minor}
        
        ## ArchUnit Results
        - Tests passing: ${archunitResults.passing}/${archunitResults.total}
        - New violations: ${archunitResults.newViolations}
        
        ## Dependency Analysis
        - Cyclic dependencies: ${dependencyResults.cycles}
        - Unstable modules: ${dependencyResults.unstable}
        """.stripIndent()
        
        // Отправка в Slack/Teams
        sendToTeamChannel(report)
    }
}
```

## 7. Обучение и Онбординг

### 7.1. Архитектурный Checklist для новых разработчиков

**Before you start coding:**
- [ ] Прочитать `README.md` и архитектурный обзор
- [ ] Установить Detekt плагин в IDE
- [ ] Запустить `./gradlew detekt` локально
- [ ] Изучить примеры кода в каждом слое
- [ ] Понимать принципы модульности

**Before submitting PR:**
- [ ] Все ArchUnit тесты проходят локально
- [ ] Detekt не находит новых нарушений
- [ ] Созданы unit тесты для новой логики
- [ ] Обновлена документация при необходимости
- [ ] ADR создан для архитектурных решений

### 7.2. Code Review Guidelines

**Для ревьюеров:**

**Архитектурные аспекты для проверки:**
- ✅ Соблюдение принципов Clean Architecture
- ✅ Правильное размещение кода по модулям
- ✅ Использование типобезопасных контрактов
- ✅ Соблюдение стратегии обработки ошибок
- ✅ Следование паттернам MVVM+ в UI

**Red flags (блокирующие проблемы):**
- ❌ Direct зависимости между feature модулями
- ❌ Android зависимости в `:shared` модуле
- ❌ Repository напрямую в ViewModel
- ❌ Исключения вместо Result в публичных API
- ❌ Нарушение инкапсуляции модулей

## 8. Эволюция архитектуры

### 8.1. Процесс изменения архитектуры

**Шаги для значительных архитектурных изменений:**

1. **Proposal Phase**
   - Создание RFC (Request for Comments) в виде GitHub Issue
   - Обсуждение с командой архитекторов
   - Анализ влияния на существующий код

2. **Design Phase**  
   - Создание draft ADR с детальным планом
   - Proof of Concept реализация
   - Обновление ArchUnit тестов

3. **Implementation Phase**
   - Поэтапная миграция (feature flags при необходимости)
   - Обновление документации и примеров
   - Обновление Detekt правил

4. **Validation Phase**
   - Проверка всех архитектурных тестов
   - Performance testing
   - Финализация ADR

### 8.2. Deprecation Process

**Для устаревших архитектурных паттернов:**

1. **Announce** - объявление deprecation с timeline
2. **Migrate** - предоставление migration guide
3. **Warn** - добавление Detekt правил с warning level
4. **Error** - поднятие warning до error level
5. **Remove** - удаление поддержки

**Пример migration guide:**
```markdown
# Migration Guide: Repository → UseCase Pattern

## Context
Direct Repository usage in ViewModels is deprecated in favor of UseCase pattern.

## Before (Deprecated)
```kotlin
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // ❌ Deprecated
)
```

## After (Recommended)  
```kotlin
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase // ✅ Recommended
)
```

## Migration Steps
1. Create UseCase for business logic
2. Update ViewModel dependencies  
3. Update DI module bindings
4. Run architecture tests to verify
```

## 9. Troubleshooting

### 9.1. Частые проблемы и решения

**Problem:** Detekt ложные срабатывания
```bash
# Solution: Обновить baseline или исключить файл
./gradlew detektBaseline  # Осторожно! Только для legacy кода
```

**Problem:** ArchUnit тесты падают после добавления зависимости
```kotlin
// Solution: Проверить правила модульности
// Возможно нужно обновить разрешенные пакеты
.resideInAnyPackage(
    "..shared..",
    "..core.design..",
    "new.allowed.package.." // Добавить новый пакет
)
```

**Problem:** CI падает, но локально все работает
```bash
# Solution: Проверить версии и clean build
./gradlew clean
./gradlew detekt --stacktrace
./gradlew :architecture-test:test --stacktrace
```

### 9.2. Performance Issues

**Медленные ArchUnit тесты:**
- Используйте `@AnalyzeClasses` с конкретными пакетами
- Кэшируйте результаты анализа классов
- Разделите большие тесты на более специфичные

**Медленная работа Detekt:**
- Исключите build директории из анализа
- Используйте параллельное выполнение
- Настройте type resolution только где необходимо

## 10. Заключение

Архитектурное управление в Amulet проекте строится на трех столпах:

1. **Автоматизация** - Detekt и ArchUnit обеспечивают автоматическую проверку
2. **Документирование** - ADR фиксируют решения и их обоснования  
3. **Эволюция** - Процессы позволяют безопасно изменять архитектуру

Этот подход гарантирует, что архитектура остается чистой, понятной и соответствует заявленным принципам на протяжении всего жизненного цикла проекта.

**Следующие шаги:**
1. Настроить Detekt с кастомными правилами
2. Создать модуль `:architecture-test` с ArchUnit тестами
3. Настроить CI/CD pipeline
4. Создать первые ADR для принятых решений
5. Провести обучение команды

---

*Документ обновлен: 02.10.2025*
*Версия: 1.0*
*Ответственный: Архитектурная команда*
