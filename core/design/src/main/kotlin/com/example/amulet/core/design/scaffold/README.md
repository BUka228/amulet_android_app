# Scaffold System - Single Scaffold with Shared State

Enterprise-паттерн для централизованного управления Scaffold в Jetpack Compose приложении.

## Проблема

В типичном подходе каждый экран создаёт свой `Scaffold` с `TopAppBar` и другими элементами:

```kotlin
// ❌ ПЛОХО: Вложенные Scaffold
@Composable
fun MainScaffold(content: @Composable (Modifier) -> Unit) {
    Scaffold(bottomBar = { BottomBar() }) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}

@Composable
fun MyScreen() {
    Scaffold(topBar = { TopAppBar() }) { paddingValues ->
        // Проблема: двойной padding, некорректные insets
        Content(Modifier.padding(paddingValues))
    }
}
```

**Проблемы:**
- Вложенные Scaffold → двойной padding и неправильная работа с system bars
- Сложности с анимациями переходов между экранами
- Дублирование кода для управления scaffold элементами
- Невозможность динамически управлять topBar/bottomBar из nested композиций

## Решение

**Single Scaffold with Shared State** - один Scaffold на уровне приложения, экраны управляют его конфигурацией через shared state.

```kotlin
// ✅ ХОРОШО: Один Scaffold, централизованное управление
@Composable
fun MainScaffold(content: @Composable () -> Unit) {
    val scaffoldState = rememberScaffoldState()
    
    Scaffold(
        topBar = scaffoldState.config.topBar,
        bottomBar = scaffoldState.config.bottomBar,
        floatingActionButton = scaffoldState.config.floatingActionButton
    ) { paddingValues ->
        ProvideScaffoldState(scaffoldState) {
            content()
        }
    }
}

@Composable
fun MyScreen() {
    val scaffoldState = LocalScaffoldState.current
    
    // Декларативно настраиваем TopBar
    scaffoldState.ShowOnlyTopBar {
        TopAppBar(title = { Text("My Screen") })
    }
    
    // Контент без padding - padding уже применён в MainScaffold
    Content()
}
```

## Архитектура

### Компоненты

1. **`ScaffoldConfig`** - immutable data class с конфигурацией scaffold
2. **`ScaffoldState`** - state holder для управления конфигурацией
3. **`LocalScaffoldState`** - CompositionLocal для доступа к state из любого экрана
4. **Extension functions** - декларативные хелперы для настройки scaffold

### Поток данных

```
Screen → LocalScaffoldState.current → ScaffoldState.updateConfig() → 
→ ScaffoldConfig → MainScaffold.Scaffold → UI Update
```

## Использование

### Базовый пример: Экран с TopBar

```kotlin
@Composable
fun DeviceDetailsScreen(onNavigateBack: () -> Unit) {
    val scaffoldState = LocalScaffoldState.current
    
    // Настраиваем только TopBar (bottomBar автоматически скрывается)
    scaffoldState.ShowOnlyTopBar {
        TopAppBar(
            title = { Text("Device Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }
    
    // Контент экрана
    Column(modifier = Modifier.fillMaxSize()) {
        // ... ваш UI
    }
}
```

### Экран с TopBar и FAB

```kotlin
@Composable
fun DevicesListScreen(onAddDevice: () -> Unit) {
    val scaffoldState = LocalScaffoldState.current
    
    // TopBar
    scaffoldState.ShowOnlyTopBar {
        TopAppBar(title = { Text("Devices") })
    }
    
    // FAB
    scaffoldState.SetupFAB {
        FloatingActionButton(onClick = onAddDevice) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // ... список устройств
    }
}
```

### Динамическое обновление на основе state

```kotlin
@Composable
fun EditableScreen(viewModel: EditViewModel) {
    val scaffoldState = LocalScaffoldState.current
    val isEditMode by viewModel.isEditMode.collectAsState()
    
    // Обновляем TopBar при изменении isEditMode
    scaffoldState.Configure(isEditMode) {
        copy(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditMode) "Edit" else "View") },
                    actions = {
                        if (isEditMode) {
                            IconButton(onClick = { viewModel.save() }) {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                            }
                        }
                    }
                )
            }
        )
    }
    
    Content()
}
```

### Fullscreen экран (без topBar и bottomBar)

```kotlin
@Composable
fun SplashScreen() {
    val scaffoldState = LocalScaffoldState.current
    
    // Скрываем все элементы scaffold
    scaffoldState.HideAll()
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ... splash content
    }
}
```

### Только bottomBar (главный экран)

```kotlin
@Composable
fun DashboardScreen() {
    val scaffoldState = LocalScaffoldState.current
    
    // MainScaffold автоматически управляет bottomBar на основе route
    // Экран просто отображает контент
    
    Column(modifier = Modifier.fillMaxSize()) {
        // ... dashboard content
    }
}
```

## API Reference

### Extension Functions

#### `ShowOnlyTopBar(topBar: @Composable () -> Unit)`
Показывает только top bar, скрывая все остальные элементы. Автоматически очищает при выходе с экрана.

#### `ShowOnlyBottomBar(bottomBar: @Composable () -> Unit)`
Показывает только bottom bar, скрывая все остальные элементы.

#### `SetupTopBar(topBar: @Composable () -> Unit)`
Устанавливает top bar, не трогая другие элементы.

#### `SetupBottomBar(bottomBar: @Composable () -> Unit)`
Устанавливает bottom bar, не трогая другие элементы.

#### `SetupFAB(position: FabPosition = FabPosition.End, fab: @Composable () -> Unit)`
Устанавливает floating action button с указанной позицией.

#### `SetupScaffold(config: ScaffoldConfig.() -> ScaffoldConfig)`
Полная настройка scaffold через конфигурацию.

#### `Configure(vararg key: Any?, config: ScaffoldConfig.() -> ScaffoldConfig)`
Императивное обновление конфигурации в ответ на изменение state (в LaunchedEffect).

#### `HideAll()`
Скрывает все элементы scaffold (fullscreen режим).

### ScaffoldState Methods

#### `updateConfig(update: ScaffoldConfig.() -> ScaffoldConfig)`
Обновить конфигурацию через функцию трансформации.

#### `reset()`
Сбросить конфигурацию к пустой (ScaffoldConfig.Empty).

#### `updateTopBar(topBar: @Composable () -> Unit)`
Обновить только top bar императивно.

#### `updateBottomBar(bottomBar: @Composable () -> Unit)`
Обновить только bottom bar императивно.

#### `updateFab(fab: @Composable () -> Unit)`
Обновить только FAB императивно.

## Best Practices

### ✅ DO

- Используйте extension functions (`ShowOnlyTopBar`, `SetupFAB`) для декларативной настройки
- Полагайтесь на автоматическую очистку через `DisposableEffect`
- Используйте `Configure` для динамических обновлений на основе state
- Держите scaffold конфигурацию в самом экране, а не в ViewModel

### ❌ DON'T

- Не создавайте вложенные `Scaffold` в feature экранах
- Не забывайте про автоматическую очистку (extensions делают это за вас)
- Не храните scaffold state в ViewModel (это UI state, должен быть в композиции)
- Не применяйте `.padding(paddingValues)` в экранах - padding уже применён в MainScaffold

## Преимущества

1. **Нет вложенных Scaffold** - правильная работа с padding и window insets
2. **Гибкость** - любой экран может управлять любым элементом scaffold
3. **Масштабируемость** - легко добавлять новые элементы (snackbar host, navigation rail, etc)
4. **Анимации** - полная поддержка Material 3 transitions и shared element transitions
5. **Тестируемость** - ScaffoldState можно легко мокировать в тестах
6. **Type-safe** - compile-time проверка корректности конфигурации

## Миграция

### До (старый код)

```kotlin
@Composable
fun PairingScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Pairing") }) }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            // content
        }
    }
}
```

### После (новый код)

```kotlin
@Composable
fun PairingScreen() {
    val scaffoldState = LocalScaffoldState.current
    
    scaffoldState.ShowOnlyTopBar {
        TopAppBar(title = { Text("Pairing") })
    }
    
    Column(Modifier.fillMaxSize()) {
        // content
    }
}
```

## Дополнительно

Этот паттерн используется в крупных production приложениях:
- Google Play Store
- Google Photos
- Airbnb
- Twitter

Реализовано согласно Material 3 Design Guidelines и Android Architecture Best Practices.
