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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseModule
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel

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
    LaunchedEffect(Unit) {
        // Future: Collect one-time events from ViewModel
    }

    CourseDetailsScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                is CourseDetailsEvent.OnNavigateBack -> onNavigateBack()
                is CourseDetailsEvent.OnPracticeClick -> onOpenPractice(event.practiceId)
                is CourseDetailsEvent.OnStartCourse -> {
                    viewModel.onEvent(event)
                    onNavigateToSchedule() // Temporary flow until enrollment wizard
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
                        onContinue = { onEvent(CourseDetailsEvent.OnContinueCourse) }
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

        // Modules List
        if (state.modules.isNotEmpty()) {
            items(state.modules) { module ->
                CourseModuleItem(
                    module = module,
                    items = state.items.filter { it.moduleId == module.id }.sortedBy { it.order },
                    isExpanded = state.expandedModuleIds.contains(module.id.value),
                    completedItemIds = state.progress?.completedItemIds ?: emptySet(),
                    unlockedItemIds = state.unlockedItemIds,
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
                    
                    CoursePracticeItem(
                        item = item,
                        isCompleted = isCompleted,
                        isLocked = isLocked,
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
                    if (!isCompleted && !isLocked) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape) else Modifier
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
                color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
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
    onContinue: () -> Unit
) {
    val isStarted = state.progress != null && state.progress.percent > 0
    
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Button(
                onClick = if (isStarted) onContinue else onStart,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isStarted) {
                        stringResource(id = R.string.practices_course_action_continue)
                    } else {
                        stringResource(id = R.string.practices_course_action_start)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

