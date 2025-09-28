# Дизайн-система Amulet

Данный документ описывает дизайн-систему для мобильного приложения Amulet, основанную на Jetpack Compose. Система обеспечивает консистентность, масштабируемость и эффективность разработки UI-компонентов.

## Содержание

1. [Философия дизайна](#философия-дизайна)
2. [Цветовая палитра](#цветовая-палитра)
3. [Типографика](#типографика)
4. [Компоненты](#компоненты)
5. [Анимации](#анимации)
6. [Правила нейминга](#правила-нейминга)
7. [Архитектура темизации](#архитектура-темизации)
8. [Адаптивность](#адаптивность)

---

## Философия дизайна

### Принципы

**1. Эмоциональная связь**
- Дизайн должен передавать тепло и заботу, характерные для концепции "объятий"
- Использование мягких форм и плавных переходов
- Цвета, ассоциирующиеся с уютом и спокойствием

**2. Минимализм с функциональностью**
- Чистый интерфейс без визуального шума
- Каждый элемент имеет четкое назначение
- Фокус на контенте и действиях пользователя

**3. Тактильность**
- Визуальные элементы должны напоминать физические объекты
- Использование теней и глубины для создания ощущения материальности
- Анимации, имитирующие физические взаимодействия

**4. Доступность**
- Соответствие принципам Material Design Accessibility
- Поддержка различных размеров экранов
- Учет особенностей пользователей с ограниченными возможностями

### Целевая аудитория

Дизайн-система учитывает потребности трех основных сегментов пользователей:

- **Осознанные профессионалы** (25-45 лет) - нуждаются в спокойном, не отвлекающем интерфейсе
- **Соединенные сердца** - требуют эмоционально насыщенного, но интуитивного дизайна
- **Техно-эстеты** - ценят красоту, функциональность и современные решения

---

## Цветовая палитра

### Основные цвета

#### Primary (Основной)
```kotlin
val Primary = Color(0xFF6B73FF) // Мягкий фиолетовый
val PrimaryVariant = Color(0xFF5A63E8) // Темнее для акцентов
val PrimaryLight = Color(0xFF8B94FF) // Светлее для фонов
```

#### Secondary (Вторичный)
```kotlin
val Secondary = Color(0xFFFF6B9D) // Теплый розовый
val SecondaryVariant = Color(0xFFE85A8B) // Темнее для акцентов
val SecondaryLight = Color(0xFFFF8BB4) // Светлее для фонов
```

#### Accent (Акцентный)
```kotlin
val Accent = Color(0xFFFFD93D) // Золотистый желтый
val AccentVariant = Color(0xFFE8C235) // Темнее для текста
```

### Семантические цвета

#### Успех и позитив
```kotlin
val Success = Color(0xFF4CAF50) // Зеленый
val SuccessLight = Color(0xFF81C784) // Светло-зеленый
val SuccessDark = Color(0xFF388E3C) // Темно-зеленый
```

#### Предупреждения
```kotlin
val Warning = Color(0xFFFF9800) // Оранжевый
val WarningLight = Color(0xFFFFB74D) // Светло-оранжевый
val WarningDark = Color(0xFFF57C00) // Темно-оранжевый
```

#### Ошибки
```kotlin
val Error = Color(0xFFF44336) // Красный
val ErrorLight = Color(0xFFEF5350) // Светло-красный
val ErrorDark = Color(0xFFD32F2F) // Темно-красный
```

#### Информация
```kotlin
val Info = Color(0xFF2196F3) // Синий
val InfoLight = Color(0xFF64B5F6) // Светло-синий
val InfoDark = Color(0xFF1976D2) // Темно-синий
```

### Нейтральные цвета

#### Серые тона
```kotlin
val Gray50 = Color(0xFFFAFAFA) // Очень светло-серый
val Gray100 = Color(0xFFF5F5F5) // Светло-серый
val Gray200 = Color(0xFFEEEEEE) // Серый
val Gray300 = Color(0xFFE0E0E0) // Средне-серый
val Gray400 = Color(0xFFBDBDBD) // Темно-серый
val Gray500 = Color(0xFF9E9E9E) // Серый
val Gray600 = Color(0xFF757575) // Темно-серый
val Gray700 = Color(0xFF616161) // Очень темно-серый
val Gray800 = Color(0xFF424242) // Почти черный
val Gray900 = Color(0xFF212121) // Черный
```

#### Черный и белый
```kotlin
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val BlackAlpha = Color(0x80000000) // Полупрозрачный черный
val WhiteAlpha = Color(0x80FFFFFF) // Полупрозрачный белый
```

### Цвета для состояний амулета

#### Световые состояния
```kotlin
val AmuletBreathing = Color(0xFF4CAF50) // Зеленый для дыхания
val AmuletPulse = Color(0xFFFF6B9D) // Розовый для пульсации
val AmuletChase = Color(0xFF6B73FF) // Фиолетовый для бегущих огней
val AmuletSpinner = Color(0xFFFFD93D) // Желтый для спиннера
val AmuletProgress = Color(0xFF2196F3) // Синий для прогресса
```

#### Эмоциональные цвета
```kotlin
val EmotionLove = Color(0xFFE91E63) // Розовый для любви
val EmotionCalm = Color(0xFF4CAF50) // Зеленый для спокойствия
val EmotionJoy = Color(0xFFFFD93D) // Желтый для радости
val EmotionSadness = Color(0xFF2196F3) // Синий для грусти
val EmotionEnergy = Color(0xFFFF9800) // Оранжевый для энергии
```

---

## Типографика

### Шрифты

#### Основной шрифт
```kotlin
val FontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)
```

**Inter** - современный, читаемый шрифт без засечек, идеально подходящий для мобильных интерфейсов.

### Стили текста

#### Заголовки
```kotlin
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    h4 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    h5 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    h6 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
)
```

#### Основной текст
```kotlin
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    overline = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    )
)
```

#### Специальные стили
```kotlin
val Typography = Typography(
    button = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
)
```

### Правила использования типографики

1. **Иерархия**: Используйте заголовки для создания четкой иерархии информации
2. **Читаемость**: Минимальный размер текста - 12sp для основного контента
3. **Контрастность**: Обеспечивайте достаточный контраст между текстом и фоном
4. **Длина строки**: Оптимальная длина строки - 45-75 символов

---

## Компоненты

### Базовые компоненты

#### AmuletButton
Основная кнопка приложения с поддержкой различных состояний.

```kotlin
@Composable
fun AmuletButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
)

enum class ButtonVariant {
    Primary,    // Основная кнопка
    Secondary,  // Вторичная кнопка
    Outline,    // Контурная кнопка
    Text,       // Текстовая кнопка
    Ghost       // Прозрачная кнопка
}

enum class ButtonSize {
    Small,      // 32dp высота
    Medium,     // 40dp высота
    Large       // 48dp высота
}
```

#### AmuletCard
Карточка для группировки контента с поддержкой различных стилей.

```kotlin
@Composable
fun AmuletCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = CardElevation.Default,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
)

enum class CardElevation {
    None,       // Без тени
    Low,        // 2dp тень
    Default,    // 4dp тень
    High        // 8dp тень
}
```

#### AmuletTextField
Поле ввода с поддержкой различных состояний и валидации.

```kotlin
@Composable
fun AmuletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
)
```

### Специализированные компоненты

#### AmuletAvatar
Аватар пользователя с поддержкой различных размеров и состояний.

```kotlin
@Composable
fun AmuletAvatar(
    imageUrl: String? = null,
    name: String? = null,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium,
    status: AvatarStatus? = null,
    onClick: (() -> Unit)? = null
)

enum class AvatarSize {
    Small,      // 32dp
    Medium,     // 40dp
    Large,      // 56dp
    ExtraLarge  // 80dp
}

enum class AvatarStatus {
    Online,     // Зеленый индикатор
    Offline,    // Серый индикатор
    Away,       // Желтый индикатор
    Busy        // Красный индикатор
}
```

#### AmuletDeviceStatus
Компонент для отображения статуса амулета.

```kotlin
@Composable
fun AmuletDeviceStatus(
    device: Device,
    modifier: Modifier = Modifier,
    showBattery: Boolean = true,
    showConnection: Boolean = true,
    onClick: (() -> Unit)? = null
)

data class Device(
    val id: String,
    val name: String,
    val batteryLevel: Int,
    val isConnected: Boolean,
    val isCharging: Boolean,
    val hardwareVersion: Int
)
```

#### AmuletPatternPreview
Предварительный просмотр анимации амулета.

```kotlin
@Composable
fun AmuletPatternPreview(
    pattern: Pattern,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {},
    onStopClick: () -> Unit = {}
)

data class Pattern(
    val id: String,
    val name: String,
    val description: String,
    val type: PatternType,
    val colors: List<Color>,
    val duration: Long
)

enum class PatternType {
    Breathing,  // Дыхательная практика
    Pulse,      // Пульсация
    Chase,      // Бегущие огни
    Spinner,    // Спиннер
    Custom      // Пользовательский
}
```

### Компоненты для практик

#### PracticeCard
Карточка практики с информацией и действиями.

```kotlin
@Composable
fun PracticeCard(
    practice: Practice,
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
)

data class Practice(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val type: PracticeType,
    val difficulty: PracticeDifficulty,
    val isFavorite: Boolean,
    val thumbnailUrl: String?
)

enum class PracticeType {
    Breathing,  // Дыхание
    Meditation, // Медитация
    Sound       // Звуковые ландшафты
}

enum class PracticeDifficulty {
    Beginner,   // Начинающий
    Intermediate, // Средний
    Advanced    // Продвинутый
}
```

#### SessionProgress
Индикатор прогресса сессии практики.

```kotlin
@Composable
fun SessionProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = MaterialTheme.colors.surface,
    showPercentage: Boolean = true
)
```

### Компоненты для социальных функций

#### HugCard
Карточка объятия с информацией об отправителе и получателе.

```kotlin
@Composable
fun HugCard(
    hug: Hug,
    modifier: Modifier = Modifier,
    onReplyClick: () -> Unit = {},
    onInfoClick: () -> Unit = {}
)

data class Hug(
    val id: String,
    val fromUser: User,
    val toUser: User,
    val emotion: Emotion,
    val pattern: Pattern?,
    val message: String?,
    val sentAt: Long,
    val deliveredAt: Long?
)

data class Emotion(
    val type: EmotionType,
    val color: Color,
    val intensity: Float
)

enum class EmotionType {
    Love,       // Любовь
    Calm,       // Спокойствие
    Joy,        // Радость
    Sadness,    // Грусть
    Energy,     // Энергия
    Support     // Поддержка
}
```

#### PairCard
Карточка пары пользователей.

```kotlin
@Composable
fun PairCard(
    pair: Pair,
    modifier: Modifier = Modifier,
    onSendHugClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
)

data class Pair(
    val id: String,
    val partner: User,
    val status: PairStatus,
    val createdAt: Long,
    val lastHugAt: Long?
)

enum class PairStatus {
    Active,     // Активная пара
    Pending,    // Ожидает подтверждения
    Blocked     // Заблокирована
}
```

---

## Анимации

### Принципы анимации

1. **Естественность**: Анимации должны имитировать физические законы
2. **Целесообразность**: Каждая анимация должна иметь четкую цель
3. **Производительность**: Анимации не должны влиять на производительность
4. **Доступность**: Уважение к настройкам пользователя по уменьшению анимаций

### Стандартные анимации

#### Переходы между экранами
```kotlin
val slideInFromRight = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
)

val slideOutToLeft = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
)
```

#### Анимации появления элементов
```kotlin
val fadeIn = fadeIn(
    animationSpec = tween(200, easing = LinearEasing)
)

val scaleIn = scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(200, easing = FastOutSlowInEasing)
)
```

#### Анимации состояний
```kotlin
val buttonPress = scaleIn(
    initialScale = 0.95f,
    animationSpec = tween(100, easing = LinearEasing)
)

val cardHover = scaleIn(
    initialScale = 1.02f,
    animationSpec = tween(200, easing = FastOutSlowInEasing)
)
```

### Специализированные анимации

#### Анимация амулета
```kotlin
@Composable
fun AmuletAnimation(
    pattern: Pattern,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    when (pattern.type) {
        PatternType.Breathing -> {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            // Анимация дыхания
        }
        PatternType.Pulse -> {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            // Анимация пульсации
        }
        // Другие типы анимаций
    }
}
```

#### Анимация прогресса
```kotlin
@Composable
fun AnimatedProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color
    )
}
```

---

## Правила нейминга

### Компоненты

#### Префиксы
- **Amulet** - для основных компонентов дизайн-системы
- **Base** - для базовых компонентов (BaseButton, BaseCard)
- **Custom** - для специализированных компонентов

#### Суффиксы
- **Card** - для карточек (PracticeCard, HugCard)
- **Button** - для кнопок (AmuletButton, IconButton)
- **Field** - для полей ввода (AmuletTextField, SearchField)
- **Indicator** - для индикаторов (ProgressIndicator, StatusIndicator)

### Цвета

#### Структура именования
```
[Категория][Оттенок][Интенсивность]
```

Примеры:
- `Primary` - основной цвет
- `PrimaryVariant` - вариант основного цвета
- `PrimaryLight` - светлый вариант
- `Success` - цвет успеха
- `Error` - цвет ошибки
- `Gray50` - серый оттенок 50

### Размеры

#### Компоненты
- `Small` - маленький размер
- `Medium` - средний размер (по умолчанию)
- `Large` - большой размер
- `ExtraLarge` - очень большой размер

#### Отступы и размеры
- Использование `dp` для размеров
- Использование `sp` для текста
- Консистентные значения: 4dp, 8dp, 12dp, 16dp, 24dp, 32dp

### Состояния

#### Общие состояния
- `Enabled` - активное состояние
- `Disabled` - неактивное состояние
- `Loading` - состояние загрузки
- `Error` - состояние ошибки
- `Success` - состояние успеха

#### Специфичные состояния
- `Connected` - подключено
- `Disconnected` - отключено
- `Charging` - заряжается
- `Online` - онлайн
- `Offline` - офлайн

---

## Архитектура темизации

### Структура темы

```kotlin
@Composable
fun AmuletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        AmuletDarkColors
    } else {
        AmuletLightColors
    }
    
    MaterialTheme(
        colors = colors,
        typography = AmuletTypography,
        shapes = AmuletShapes,
        content = content
    )
}
```

### Цветовые схемы

#### Светлая тема
```kotlin
val AmuletLightColors = lightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    background = Gray50,
    surface = White,
    error = Error,
    onPrimary = White,
    onSecondary = White,
    onBackground = Gray900,
    onSurface = Gray900,
    onError = White
)
```

#### Темная тема
```kotlin
val AmuletDarkColors = darkColors(
    primary = PrimaryLight,
    primaryVariant = Primary,
    secondary = SecondaryLight,
    secondaryVariant = Secondary,
    background = Gray900,
    surface = Gray800,
    error = ErrorLight,
    onPrimary = Gray900,
    onSecondary = Gray900,
    onBackground = Gray50,
    onSurface = Gray50,
    onError = Gray900
)
```

### Формы и скругления

```kotlin
val AmuletShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)
```

### Кастомные темы

#### Тема для практик
```kotlin
@Composable
fun PracticeTheme(
    practiceType: PracticeType,
    content: @Composable () -> Unit
) {
    val colors = when (practiceType) {
        PracticeType.Breathing -> BreathingColors
        PracticeType.Meditation -> MeditationColors
        PracticeType.Sound -> SoundColors
    }
    
    MaterialTheme(
        colors = colors,
        typography = AmuletTypography,
        shapes = AmuletShapes,
        content = content
    )
}
```

---

## Адаптивность

### Брейкпоинты

```kotlin
object Breakpoints {
    val Small = 600.dp      // Маленькие экраны
    val Medium = 840.dp     // Средние экраны
    val Large = 1200.dp     // Большие экраны
}
```

### Адаптивные компоненты

#### Адаптивная сетка
```kotlin
@Composable
fun AdaptiveGrid(
    items: List<Any>,
    modifier: Modifier = Modifier,
    content: @Composable (Any) -> Unit
) {
    val windowSize = LocalConfiguration.current.screenWidthDp.dp
    
    val columns = when {
        windowSize < Breakpoints.Small -> 1
        windowSize < Breakpoints.Medium -> 2
        else -> 3
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            content(item)
        }
    }
}
```

#### Адаптивные отступы
```kotlin
@Composable
fun AdaptivePadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val windowSize = LocalConfiguration.current.screenWidthDp.dp
    
    val padding = when {
        windowSize < Breakpoints.Small -> PaddingValues(16.dp)
        windowSize < Breakpoints.Medium -> PaddingValues(24.dp)
        else -> PaddingValues(32.dp)
    }
    
    Box(
        modifier = modifier.padding(padding)
    ) {
        content()
    }
}
```

### Ориентация

#### Поддержка поворота экрана
```kotlin
@Composable
fun OrientationAwareLayout(
    modifier: Modifier = Modifier,
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    Box(modifier = modifier) {
        content(isLandscape)
    }
}
```

---

## Рекомендации по использованию

### Лучшие практики

1. **Консистентность**: Всегда используйте компоненты из дизайн-системы
2. **Переиспользование**: Создавайте новые компоненты только при необходимости
3. **Документация**: Документируйте все кастомные компоненты
4. **Тестирование**: Тестируйте компоненты на различных размерах экранов
5. **Производительность**: Оптимизируйте анимации для плавной работы

### Избегайте

1. **Хардкод значений**: Используйте константы из дизайн-системы
2. **Дублирование**: Не создавайте похожие компоненты
3. **Нарушение иерархии**: Следуйте принципам Material Design
4. **Игнорирование доступности**: Всегда учитывайте потребности пользователей

### Миграция

При обновлении дизайн-системы:

1. **Обратная совместимость**: Старые компоненты должны продолжать работать
2. **Постепенная миграция**: Обновляйте компоненты поэтапно
3. **Документация изменений**: Ведите changelog всех изменений
4. **Тестирование**: Проверяйте все экраны после обновлений

---

## Заключение

Данная дизайн-система обеспечивает:

- **Консистентность** визуального стиля во всем приложении
- **Эффективность** разработки через переиспользование компонентов
- **Масштабируемость** для будущих обновлений и новых функций
- **Доступность** для всех пользователей
- **Производительность** через оптимизированные компоненты

Следование принципам и правилам данной системы гарантирует создание качественного, красивого и функционального пользовательского интерфейса, соответствующего концепции и ценностям приложения Amulet.
