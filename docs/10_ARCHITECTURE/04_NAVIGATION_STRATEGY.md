## Стратегия навигации (Navigation Strategy)

### 1) Инструмент

Мы используем Navigation Compose как стандартный механизм навигации в Jetpack Compose. Он интегрируется с жизненным циклом, `ViewModel`, `SavedStateHandle`, поддерживает deep links и анимации переходов.

Причины выбора:
- Нативная поддержка Compose, минимальная связность кода UI и навигации
- Совместимость с `Hilt`/`ViewModel`/`SavedStateHandle`
- Поддержка nested graphs, deep links, результат‑навигирования


### 2) Типобезопасные маршруты (Type‑Safe Routes)

Требование: избегать «хрупких» строковых путей вида `"profile/{userId}"` в приложении. Вместо этого — единая точка объявления маршрутов и хелперы для построения/парсинга аргументов.

Подход:
- Для простых аргументов (ID) используем value‑классы и фабрики маршрутов
- Для сложных структур намеренно не используем передачу через аргументы (см. раздел ниже)
- Где нужен сериализуемый составной аргумент (редко, только в системных местах) — используем `kotlinx.serialization` + URI‑safe кодирование

Базовые примитивы:

```kotlin
// Централизованное описание destination'ов
sealed interface AppDestination {
    val baseRoute: String
}

@JvmInline
value class UserId(val value: String)

object ProfileDestination : AppDestination { override val baseRoute = "profile" }

// Декларация type-safe API для навигации
fun NavController.navigateToProfile(userId: UserId) {
    navigate("${ProfileDestination.baseRoute}/${Uri.encode(userId.value)}")
}

// Регистрация destination с явным аргументом
fun NavGraphBuilder.profileScreen(
    onBack: () -> Unit
) {
    composable(
        route = "${ProfileDestination.baseRoute}/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
    ) { backStackEntry ->
        val userId = UserId(backStackEntry.arguments!!.getString("userId")!!)
        ProfileScreen(userId = userId, onBack = onBack)
    }
}
```

Обработка аргументов во ViewModel (через `SavedStateHandle`)

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: String = requireNotNull(savedStateHandle.get<String>("userId")) {
        "Missing required nav argument: userId"
    }

    val state: StateFlow<ProfileState> = flow {
        emit(loadState())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState(isLoading = true))

    private suspend fun loadState(): ProfileState {
        return getUserProfile(UserId(userId))
            .fold(
                onSuccess = { ProfileState(user = it) },
                onFailure = { ProfileState(error = it) }
            )
    }
}

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(state = uiState, onBack = onBack)
}
```
Когда ViewModel извлекает аргументы сама, Composable не парсит `BackStackEntry`; это упрощает вызов и концентрирует валидацию/дефолты в одном месте.

Пример c `kotlinx.serialization` (точечно, если требуется несколько полей):

```kotlin
@Serializable
data class PracticeEntryArgs(val practiceId: String, val ref: String? = null)

object PracticeDestination : AppDestination { override val baseRoute = "practice" }

fun NavController.navigateToPractice(args: PracticeEntryArgs) {
    val encoded = Uri.encode(Json.encodeToString(args))
    navigate("${PracticeDestination.baseRoute}?payload=$encoded")
}

fun NavGraphBuilder.practiceScreen(onBack: () -> Unit) {
    composable(
        route = "${PracticeDestination.baseRoute}?payload={payload}",
        arguments = listOf(navArgument("payload") { nullable = true })
    ) { entry ->
        val payload = entry.arguments?.getString("payload")
        val args = payload?.let { Json.decodeFromString<PracticeEntryArgs>(Uri.decode(it)) }
        PracticeScreen(practiceId = args?.practiceId, onBack = onBack)
    }
}
```


### 3) Передача данных между экранами

- Простые аргументы: передаём только идентификаторы (`patternId`, `hugId`, `userId`). Экран загружает и кэширует полные данные сам через репозиторий, сохраняя Single Source of Truth.

- Сложные объекты: не передаём через аргументы навигации. Причины:
  - Ограничения размера Bundle/бэктрека, риск `TransactionTooLargeException`
  - Хрупкость схем при эволюции моделей
  - Дублирование состояния (нарушение SoT)

Паттерн для сложных данных: `SharedViewModel`, привязанная к навграфу

```kotlin
// Владелец состояния — граф фичи (scope = navGraph)
@HiltViewModel
class SendHugSharedViewModel @Inject constructor(
    private val repository: HugsRepository
) : ViewModel() {
    // общее состояние мастера
}

// Получение одной и той же VM из нескольких экранов одного nested graph
@Composable
fun rememberSendHugSharedViewModel(navBackStackEntry: NavBackStackEntry): SendHugSharedViewModel {
    return hiltViewModel(navBackStackEntry)
}
```

Явная привязка к вложенному графу с Hilt (`@HiltViewModel`) и `hiltViewModel(navBackStackEntry)`

```kotlin
// Предположим, что nested graph объявлен как route = "hugs"
fun NavGraphBuilder.hugsGraph(navController: NavController) {
    navigation(startDestination = "hugs/list", route = "hugs") {
        composable("hugs/list") { entry ->
            // Получаем BackStackEntry самого графа, а не конкретного экрана
            val graphEntry = remember(navController) { navController.getBackStackEntry("hugs") }
            val sharedVm: SendHugSharedViewModel = hiltViewModel(graphEntry)
            HugsListScreen(sharedVm)
        }

        composable("hugs/details/{hugId}") { entry ->
            val graphEntry = remember(navController) { navController.getBackStackEntry("hugs") }
            val sharedVm: SendHugSharedViewModel = hiltViewModel(graphEntry)
            HugDetailsScreen(sharedVm)
        }
    }
}

// В Compose это эквивалент паттерну navGraphViewModels из Fragment API:
// вместо navGraphViewModels(graphId) мы явно передаём navBackStackEntry в hiltViewModel(...)
```

Возврат результата экраном B на экран A: `SavedStateHandle`

```kotlin
// Экран A запускает B и подписывается на результат
const val RESULT_KEY_PATTERN_PICKED = "result_pattern_picked"

fun NavController.navigateToPatternPicker() {
    navigate("patterns/picker")
}

@Composable
fun ScreenA(navController: NavController) {
    // Идиоматично: использовать StateFlow + collectAsStateWithLifecycle
    val handle = navController.currentBackStackEntry?.savedStateHandle
    val resultFlow = remember(handle) {
        handle?.getStateFlow<String?>(RESULT_KEY_PATTERN_PICKED, null)
    }
    val result by resultFlow?.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(result) {
        result?.let { patternId ->
            // обработать выбранный паттерн
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>(RESULT_KEY_PATTERN_PICKED)
        }
    }
}

// Экран B устанавливает результат и уходит назад
fun NavController.returnPatternResult(patternId: String) {
    previousBackStackEntry?.savedStateHandle?.set(RESULT_KEY_PATTERN_PICKED, patternId)
    popBackStack()
}
```

Альтернативы и примечания:
- Если в актуальной версии Navigation Compose доступен официальный API возврата результата без `SavedStateHandle`, используем его (при его стабильности) в духе рекомендаций Jetpack; данная страница будет обновлена при миграции.


### 4) Структура навигационного графа

- Главный `NavHost` живёт в модуле `:app` внутри `MainActivity`.
- Каждая фича (`:feature:*`) экспортирует расширения `NavGraphBuilder` для регистрации своих экранов и/или nested‑graph.
- `:app` собирает корневой граф, подключая фичи через эти расширения (или через DI‑поставщики).

Скелет в `:app`:

```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = RootDestination.baseRoute
) {
    NavHost(navController = navController, startDestination = startDestination) {
        dashboardGraph(navController)      // из :feature:dashboard
        hugsGraph(navController)           // из :feature:hugs
        practicesGraph(navController)      // из :feature:practices
        devicesGraph(navController)        // из :feature:devices
        settingsGraph(navController)       // из :feature:settings
        profileScreen(onBack = { navController.popBackStack() }) // одиночный экран
    }
}
```

Контракт фичи:

```kotlin
// Внутри :feature:hugs
fun NavGraphBuilder.hugsGraph(navController: NavController) {
    navigation(startDestination = "hugs/list", route = "hugs") {
        composable("hugs/list") { HugsListScreen(
            onOpenDetails = { hugId -> navController.navigate("hugs/details/$hugId") }
        ) }

        composable(
            route = "hugs/details/{hugId}",
            arguments = listOf(navArgument("hugId") { type = NavType.StringType })
        ) { entry ->
            val hugId = entry.arguments!!.getString("hugId")!!
            HugDetailsScreen(hugId = hugId, onBack = { navController.popBackStack() })
        }
    }
}
```


### 5) Deep Links

Выбранная схема: `amulet://` для внутренних ссылок и зеркала на HTTPS‑домен (например, `https://amulet.app/…`) для источников вне приложения (почта, веб, пуши).

Доступные deep link’и (минимальный набор):
- Профиль партнёра: `amulet://profile/{userId}` и `https://amulet.app/profile/{userId}`
- Объятие (детали): `amulet://hugs/{hugId}` и `https://amulet.app/hugs/{hugId}`
- Практика (детали/запуск): `amulet://practices/{practiceId}` и `https://amulet.app/practices/{practiceId}`
- Подключение устройства (мастер): `amulet://devices/setup`

Регистрация deep link’ов:

```kotlin
composable(
    route = "${ProfileDestination.baseRoute}/{userId}",
    arguments = listOf(navArgument("userId") { type = NavType.StringType }),
    deepLinks = listOf(
        navDeepLink { uriPattern = "amulet://profile/{userId}" },
        navDeepLink { uriPattern = "https://amulet.app/profile/{userId}" }
    )
) { /* ... */ }
```

Взаимодействие с пуш‑уведомлениями:
- FCM data‑message содержит тип и идентификатор: например, `{ type: "hug_received", hugId: "…" }`
- Нотификационный клик формирует deep link `amulet://hugs/{hugId}`
- Навхост автоматически открывает нужный экран; экран догружает данные по `hugId`


### 6) Дополнительно: соглашения и правила

- Любой экран, который требует параметров, должен иметь:
  - объект‑описатель destination с `baseRoute`
  - функцию‑расширение `NavController.navigateToXxx(args)`
  - функцию регистрации `NavGraphBuilder.xxxScreen(...)`
- В коде UI запрещено вызывать `navigate("raw/strings")`. Разрешены только type‑safe хелперы.
- Возврат результатов — только через `SavedStateHandle`/`previousBackStackEntry` (не через глобальные синглтоны).
- Сложные объекты — только через `SharedViewModel` в scope nested graph.
- Все deep link’и должны проходить проверку авторизации/фичефлагов до открытия экрана.


### 7) Пример целостного сценария

Список «объятий» → Детали «объятия» → Выбор паттерна и возврат результата:

```kotlin
// A: HugsList
AmuletButton(text = "Выбрать паттерн") {
    navController.navigate("patterns/picker")
}

LaunchedEffect(Unit) {
    navController.currentBackStackEntry?.savedStateHandle
        ?.getLiveData<String>(RESULT_KEY_PATTERN_PICKED)
        ?.observeForever { patternId ->
            viewModel.onPatternPicked(patternId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>(RESULT_KEY_PATTERN_PICKED)
        }
}

// B: PatternPicker
fun onPick(patternId: String) {
    navController.returnPatternResult(patternId)
}
```

Такой контракт обеспечивает предсказуемость, тестируемость и минимальную связанность между экранами и слоями приложения.

### 8) Условная навигация (Conditional Navigation)

Коротко: корневой `MainScreen` наблюдает `AuthState` и выбирает граф: `authGraph` (гость) или основной граф (авторизован). При смене состояния стек сбрасывается.

```kotlin
sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val userId: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(repo: AuthRepository) : ViewModel() {
    val state: StateFlow<AuthState> = repo.authState // Loading → LoggedOut/LoggedIn
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsState()

    when (authState) {
        AuthState.Loading -> SplashScreen()
        AuthState.LoggedOut -> androidx.compose.runtime.key(AuthState.LoggedOut::class) {
            NavHost(navController, startDestination = "auth") { authGraph(navController) }
        }
        is AuthState.LoggedIn -> androidx.compose.runtime.key(AuthState.LoggedIn::class) {
            NavHost(navController, startDestination = RootDestination.baseRoute) {
                dashboardGraph(navController)
                hugsGraph(navController)
                practicesGraph(navController)
                devicesGraph(navController)
                settingsGraph(navController)
            }
        }
    }
}

// Альтернатива: сбросить стек после успешного логина
navController.navigate(RootDestination.baseRoute) {
    popUpTo(0)
    launchSingleTop = true
}
```

Рекомендации:
- Проверку `isLoggedIn` и восстановление сессии выполнять в `AuthViewModel`, пока UI показывает `Loading`
- Все deep link’и валидировать: если нужна авторизация — временно отправлять в `authGraph` и запоминать целевой маршрут для редиректа после логина
- Пользователь/токены не передаются через аргументы; источники истины — репозитории/`DataStore`


