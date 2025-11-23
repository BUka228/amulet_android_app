package com.example.amulet.feature.practices.presentation.course

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseModule
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.courses.model.CourseRhythm
import com.example.amulet.shared.domain.courses.model.EnrollmentParams
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.ScheduledSessionStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun CourseDetailsRoute(
    courseId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onOpenPractice: (String) -> Unit,
    viewModel: CourseDetailsViewModel = hiltViewModel()
) {
    viewModel.setIdIfEmpty(courseId)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation events from ViewModel if needed, or just use callbacks
    LaunchedEffect(state.nextPracticeId) {
        val nextId = state.nextPracticeId
        if (nextId != null) {
            onOpenPractice(nextId)
            viewModel.onEvent(CourseDetailsEvent.OnNextPracticeConsumed)
        }
    }

    CourseDetailsScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                is CourseDetailsEvent.OnNavigateBack -> onNavigateBack()
                is CourseDetailsEvent.OnPracticeClick -> onOpenPractice(event.practiceId)
                is CourseDetailsEvent.OnStartCourse -> viewModel.onEvent(event)
                is CourseDetailsEvent.OnOpenScheduleEdit -> onNavigateToSchedule()
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun CourseDetailsScreen(
    state: CourseDetailsState,
    onEvent: (CourseDetailsEvent) -> Unit
) {
    val scaffoldState = LocalScaffoldState.current
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Hide default top bar as we use a custom Hero section or transparent bar
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(stringResource(id = R.string.practices_course_title))
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { onEvent(CourseDetailsEvent.OnNavigateBack) }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showResetDialog = true }) {
                                Icon(Icons.Filled.Restore, null)
                            }
                        }
                    )
                },
                floatingActionButton = {},
                bottomBar = {
                    CourseBottomBar(
                        state = state,
                        onStart = { onEvent(CourseDetailsEvent.OnStartCourse) },
                        onContinue = { onEvent(CourseDetailsEvent.OnContinueCourse) },
                        onEnroll = { onEvent(CourseDetailsEvent.OnOpenEnrollmentWizard(CourseEnrollmentMode.STANDARD)) },
                        onRestart = { onEvent(CourseDetailsEvent.OnRestartCourse) },
                        onEditSchedule = { onEvent(CourseDetailsEvent.OnOpenScheduleEdit) }
                    )
                }
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(id = R.string.practices_course_reset_title)) },
            text = { Text(stringResource(id = R.string.practices_course_reset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onEvent(CourseDetailsEvent.OnResetCourse)
                    }
                ) {
                    Text(stringResource(id = R.string.practices_course_reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(id = R.string.practices_course_reset_cancel))
                }
            }
        )
    }

    if (state.showEnrollmentWizard && state.courseId != null) {
        CourseEnrollmentDialog(
            courseId = state.courseId,
            rhythm = state.course?.rhythm ?: CourseRhythm.DAILY,
            enrollmentMode = state.enrollmentMode ?: CourseEnrollmentMode.STANDARD,
            isLoading = state.enrollmentInProgress,
            onConfirm = { params -> onEvent(CourseDetailsEvent.OnEnrollCourse(params)) },
            onDismiss = { onEvent(CourseDetailsEvent.OnDismissEnrollmentWizard) }
        )
    }

    val currentItemId = remember(state.items, state.unlockedItemIds, state.progress) {
        val completed = state.progress?.completedItemIds ?: emptySet()
        state.items
            .sortedBy { it.order }
            .firstOrNull { item ->
                state.unlockedItemIds.contains(item.id) && !completed.contains(item.id)
            }
            ?.id
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Section
        item {
            CourseHero(state.course, state.progress)
        }

        // Description
        item {
            state.course?.description?.let { desc ->
                AmuletCard(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Upcoming sessions block
        if (state.upcomingSessions.isNotEmpty()) {
            item {
                val session = state.upcomingSessions.first()
                val timeZone = TimeZone.currentSystemDefault()
                val localDateTime = Instant
                    .fromEpochMilliseconds(session.scheduledTime)
                    .toLocalDateTime(timeZone)

                AmuletCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(id = R.string.practices_course_upcoming_sessions_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${localDateTime.date} • %02d:%02d".format(
                                        localDateTime.time.hour,
                                        localDateTime.time.minute
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { onEvent(CourseDetailsEvent.OnOpenScheduleEdit) }) {
                                Text(text = stringResource(id = R.string.calendar_title))
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    modifier = Modifier.width(16.dp),
                                    contentDescription = null,
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = session.practiceTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = when (session.status) {
                                        ScheduledSessionStatus.PLANNED -> stringResource(R.string.schedule_status_planned)
                                        ScheduledSessionStatus.MISSED -> stringResource(R.string.schedule_status_missed)
                                        ScheduledSessionStatus.COMPLETED -> stringResource(R.string.schedule_status_completed)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modules List
        if (state.modules.isNotEmpty()) {
            items(state.modules) { module ->
                CourseModuleItem(
                    module = module,
                    items = state.items.filter { it.moduleId == module.id }.sortedBy { it.order },
                    isExpanded = state.expandedModuleIds.contains(module.id.value),
                    completedItemIds = state.progress?.completedItemIds ?: emptySet(),
                    unlockedItemIds = state.unlockedItemIds,
                    currentItemId = currentItemId,
                    onToggle = { onEvent(CourseDetailsEvent.OnModuleClick(module.id.value)) },
                    onItemClick = { item ->
                        item.practiceId?.let { practiceId ->
                            onEvent(CourseDetailsEvent.OnPracticeClick(practiceId))
                        }
                    }
                )
            }
        } else {
            // Fallback for flat list
            items(state.items.sortedBy { it.order }) { item ->
                CoursePracticeItem(
                    item = item,
                    isCompleted = state.progress?.completedItemIds?.contains(item.id) == true,
                    isLocked = !state.unlockedItemIds.contains(item.id),
                    isCurrent = currentItemId == item.id,
                    onClick = {
                        item.practiceId?.let { practiceId ->
                            onEvent(CourseDetailsEvent.OnPracticeClick(practiceId))
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun CourseEnrollmentDialog(
    courseId: String,
    rhythm: CourseRhythm,
    enrollmentMode: CourseEnrollmentMode,
    isLoading: Boolean,
    onConfirm: (EnrollmentParams) -> Unit,
    onDismiss: () -> Unit
) {
    val now = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var selectedRhythm by remember { mutableStateOf(rhythm) }
    var selectedDays by remember(selectedRhythm) {
        mutableStateOf(
            when (selectedRhythm) {
                CourseRhythm.DAILY -> DayOfWeek.entries.toSet()
                CourseRhythm.THREE_TIMES_WEEK -> setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                CourseRhythm.FLEXIBLE -> DayOfWeek.entries.toSet()
            }
        )
    }

    val titleRes = when (enrollmentMode) {
        CourseEnrollmentMode.STANDARD -> R.string.practices_course_enrollment_title
        CourseEnrollmentMode.PLAN_FOCUSED -> R.string.practices_course_enrollment_title_plan
        CourseEnrollmentMode.REPEAT -> R.string.practices_course_enrollment_title_repeat
    }

    val rhythmSubtitleRes = when (enrollmentMode) {
        CourseEnrollmentMode.STANDARD -> R.string.practices_course_enrollment_step_rhythm_subtitle
        CourseEnrollmentMode.PLAN_FOCUSED -> R.string.practices_course_enrollment_step_rhythm_subtitle_plan
        CourseEnrollmentMode.REPEAT -> R.string.practices_course_enrollment_step_rhythm_subtitle_repeat
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Time picker state: по умолчанию устанавливаем утреннее время,
    // чтобы соответствующий чип «утро» был выбран при открытии шита
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    var showTimePickerDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(id = rhythmSubtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rhythm section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.practices_course_enrollment_step_rhythm_title),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { selectedRhythm = CourseRhythm.DAILY },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_rhythm_daily)) },
                        leadingIcon = {
                            if (selectedRhythm == CourseRhythm.DAILY) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    AssistChip(
                        onClick = { selectedRhythm = CourseRhythm.THREE_TIMES_WEEK },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_rhythm_three_times)) },
                        leadingIcon = {
                            if (selectedRhythm == CourseRhythm.THREE_TIMES_WEEK) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    AssistChip(
                        onClick = { selectedRhythm = CourseRhythm.FLEXIBLE },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_rhythm_flexible)) },
                        leadingIcon = {
                            if (selectedRhythm == CourseRhythm.FLEXIBLE) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }

            // Time section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.practices_course_enrollment_step_time_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(id = R.string.practices_course_enrollment_step_time_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val selectedTime = LocalTime(
                    hour = timePickerState.hour,
                    minute = timePickerState.minute
                )
                val selectedTimeText = "%02d:%02d".format(selectedTime.hour, selectedTime.minute)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val morningTime = LocalTime(hour = 8, minute = 0)
                    val dayTime = LocalTime(hour = 13, minute = 0)
                    val eveningTime = LocalTime(hour = 19, minute = 0)
                    val nightTime = LocalTime(hour = 22, minute = 30)

                    AssistChip(
                        onClick = {
                            timePickerState.hour = morningTime.hour
                            timePickerState.minute = morningTime.minute
                        },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_time_morning)) },
                        leadingIcon = {
                            if (selectedTime == morningTime) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                    AssistChip(
                        onClick = {
                            timePickerState.hour = dayTime.hour
                            timePickerState.minute = dayTime.minute
                        },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_time_day)) },
                        leadingIcon = {
                            if (selectedTime == dayTime) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                    AssistChip(
                        onClick = {
                            timePickerState.hour = eveningTime.hour
                            timePickerState.minute = eveningTime.minute
                        },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_time_evening)) },
                        leadingIcon = {
                            if (selectedTime == eveningTime) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                    AssistChip(
                        onClick = {
                            timePickerState.hour = nightTime.hour
                            timePickerState.minute = nightTime.minute
                        },
                        label = { Text(stringResource(id = R.string.practices_course_enrollment_time_night)) },
                        leadingIcon = {
                            if (selectedTime == nightTime) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
                OutlinedButton(
                    onClick = { showTimePickerDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = selectedTimeText)
                }
            }

            // Days of week section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.practices_course_enrollment_step_days_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(id = R.string.practices_course_enrollment_step_days_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val allDays = DayOfWeek.entries.toTypedArray()
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allDays.forEach { day ->
                        val selected = selectedDays.contains(day)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedDays = if (selected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day.displayNameShort()) }
                        )
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.practices_course_enrollment_cancel))
                }

                Button(
                    onClick = {
                        val params = EnrollmentParams(
                            courseId = courseId,
                            rhythm = selectedRhythm,
                            preferredTime = LocalTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            ),
                            selectedDays = selectedDays,
                            startDate = now.date
                        )
                        onConfirm(params)
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.practices_course_enrollment_confirm))
                }
            }
        }
        if (showTimePickerDialog) {
            AlertDialog(
                onDismissRequest = { showTimePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = { showTimePickerDialog = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePickerDialog = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}

@Composable
private fun PracticeGoal.displayName(): String = when (this) {
    PracticeGoal.SLEEP -> stringResource(R.string.practice_goal_sleep)
    PracticeGoal.STRESS -> stringResource(R.string.practice_goal_stress)
    PracticeGoal.ENERGY -> stringResource(R.string.practice_goal_energy)
    PracticeGoal.FOCUS -> stringResource(R.string.practice_goal_focus)
    PracticeGoal.RELAXATION -> stringResource(R.string.practice_goal_relaxation)
    PracticeGoal.ANXIETY -> stringResource(R.string.practice_goal_anxiety)
    PracticeGoal.MOOD -> stringResource(R.string.practice_goal_mood)
}

@Composable
private fun PracticeLevel.displayName(): String = when (this) {
    PracticeLevel.BEGINNER -> stringResource(R.string.practice_level_beginner)
    PracticeLevel.INTERMEDIATE -> stringResource(R.string.practice_level_intermediate)
    PracticeLevel.ADVANCED -> stringResource(R.string.practice_level_advanced)
}

@Composable
private fun CourseRhythm.displayName(): String = when (this) {
    CourseRhythm.DAILY -> stringResource(R.string.practices_course_enrollment_rhythm_daily)
    CourseRhythm.THREE_TIMES_WEEK -> stringResource(R.string.practices_course_enrollment_rhythm_three_times)
    CourseRhythm.FLEXIBLE -> stringResource(R.string.practices_course_enrollment_rhythm_flexible)
}

@Composable
private fun DayOfWeek.displayNameShort(): String = when (this) {
    DayOfWeek.MONDAY -> stringResource(R.string.weekday_mon)
    DayOfWeek.TUESDAY -> stringResource(R.string.weekday_tue)
    DayOfWeek.WEDNESDAY -> stringResource(R.string.weekday_wed)
    DayOfWeek.THURSDAY -> stringResource(R.string.weekday_thu)
    DayOfWeek.FRIDAY -> stringResource(R.string.weekday_fri)
    DayOfWeek.SATURDAY -> stringResource(R.string.weekday_sat)
    DayOfWeek.SUNDAY -> stringResource(R.string.weekday_sun)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseHero(course: Course?, progress: CourseProgress?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover Image / Illustration on the left (outside the card)
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(3f / 4.5f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val coverName = course?.coverUrl
            val resId = coverName?.let {
                context.resources.getIdentifier(it, "drawable", context.packageName)
            } ?: 0

            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Info card on the right
        AmuletCard(
            modifier = Modifier.weight(2f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course?.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Meta info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    course?.modulesCount?.let { modulesCount ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.ViewModule, null, modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(id = R.string.practices_course_modules_count, modulesCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    course?.recommendedDays?.let { days ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Event, null, modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(id = R.string.practices_course_recommended_days, days),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                course?.totalDurationSec?.let { totalSec ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(id = R.string.practices_course_total_duration, totalSec / 60),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                course?.rhythm?.let { rhythm ->
                    Text(
                        text = rhythm.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    course?.goal?.let { goal ->
                        SuggestionChip(onClick = {}, label = { Text(goal.displayName()) })
                    }
                    course?.level?.let { level ->
                        SuggestionChip(onClick = {}, label = { Text(level.displayName()) })
                    }
                }

                // Progress
                if (progress != null && progress.percent > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(id = R.string.practices_course_progress_label, progress.percent),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { progress.percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseModuleItem(
    module: CourseModule,
    items: List<CourseItem>,
    isExpanded: Boolean,
    completedItemIds: Set<String>,
    unlockedItemIds: Set<String>,
    currentItemId: String?,
    onToggle: () -> Unit,
    onItemClick: (CourseItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title ?: stringResource(id = R.string.practices_course_module_fallback_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        id = R.string.practices_course_module_completed_items,
                        items.count { it.id in completedItemIds },
                        items.size
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    val isLocked = !unlockedItemIds.contains(item.id)
                    val isCompleted = item.id in completedItemIds
                    val isCurrent = currentItemId == item.id
                    
                    CoursePracticeItem(
                        item = item,
                        isCompleted = isCompleted,
                        isLocked = isLocked,
                        isCurrent = isCurrent,
                        onClick = {
                            if (item.practiceId != null) {
                                onItemClick(item)
                            }
                        }
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CoursePracticeItem(
    item: CourseItem,
    isCompleted: Boolean,
    isLocked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isLocked -> MaterialTheme.colorScheme.surfaceVariant
                        else -> Color.Transparent
                    }
                )
                .then(
                    if (isCurrent && !isCompleted && !isLocked) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else if (!isCompleted && !isLocked) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isLocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: stringResource(id = R.string.practices_course_practice_fallback_title),
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isLocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            item.minDurationSec?.let {
                Text(
                    text = stringResource(id = R.string.practices_course_practice_duration_minutes, it / 60),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.type == CourseItemType.THEORY) {
             Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        } else {
             Icon(Icons.Filled.Spa, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CourseBottomBar(
    state: CourseDetailsState,
    onStart: () -> Unit,
    onContinue: () -> Unit,
    onEnroll: () -> Unit,
    onRestart: () -> Unit,
    onEditSchedule: () -> Unit
) {
    val progress = state.progress
    val courseStatus = progress?.status ?: com.example.amulet.shared.domain.courses.model.CourseStatus.NOT_ENROLLED
    var showContinueDialog by remember { mutableStateOf(false) }
    
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            when (courseStatus) {
                com.example.amulet.shared.domain.courses.model.CourseStatus.NOT_ENROLLED -> {
                    // Состояние "Не начат": основная CTA
                    Button(
                        onClick = onEnroll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.practices_course_action_enroll),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                com.example.amulet.shared.domain.courses.model.CourseStatus.IN_PROGRESS -> {
                    // Состояние "В процессе"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (state.upcomingSessions.isNotEmpty()) {
                                    showContinueDialog = true
                                } else {
                                    onContinue()
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.practices_course_action_continue),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        OutlinedButton(
                            onClick = onEditSchedule,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.practices_course_action_edit_schedule),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                com.example.amulet.shared.domain.courses.model.CourseStatus.COMPLETED -> {
                    // Состояние "Завершён"
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Блок итогов
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.practices_course_completed_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            progress?.let {
                                Text(
                                    text = stringResource(
                                        id = R.string.practices_course_completed_time,
                                        it.totalTimeSec / 60
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        progress?.let {
                            Text(
                                text = stringResource(
                                    id = R.string.practices_course_progress_label,
                                    it.percent
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.practices_course_action_restart),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showContinueDialog && courseStatus == com.example.amulet.shared.domain.courses.model.CourseStatus.IN_PROGRESS) {
        AlertDialog(
            onDismissRequest = { showContinueDialog = false },
            title = {
                Text(text = stringResource(id = R.string.practices_course_continue_dialog_title))
            },
            text = {
                Text(text = stringResource(id = R.string.practices_course_continue_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showContinueDialog = false
                        onContinue()
                    }
                ) {
                    Text(text = stringResource(id = R.string.practices_course_action_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showContinueDialog = false
                        onEditSchedule()
                    }
                ) {
                    Text(text = stringResource(id = R.string.calendar_title))
                }
            }
        )
    }
}

