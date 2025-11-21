package com.example.amulet.feature.practices.presentation.home

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.foundation.color.AmuletPalette.Divider
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.practices.model.PracticeGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticesHomeRoute(
    onOpenPractice: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: PracticesHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PracticesHomeEffect.NavigateToPractice -> onOpenPractice(effect.practiceId)
                is PracticesHomeEffect.NavigateToCourse -> onOpenCourse(effect.courseId)
                PracticesHomeEffect.NavigateToSchedule -> Unit
                PracticesHomeEffect.NavigateToStats -> Unit
                PracticesHomeEffect.NavigateToSearch -> onOpenSearch()
                is PracticesHomeEffect.ShowError -> Unit
            }
        }
    }

    PracticesHomeScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticesHomeScreen(
    state: PracticesHomeState,
    onIntent: (PracticesHomeIntent) -> Unit
) {
    val scaffoldState = LocalScaffoldState.current
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(state.greeting, style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            IconButton(onClick = { onIntent(PracticesHomeIntent.OpenSchedule) }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = stringResource(id = R.string.practices_home_topbar_schedule)
                                )
                            }
                            IconButton(onClick = { onIntent(PracticesHomeIntent.OpenStats) }) {
                                Icon(
                                    imageVector = Icons.Filled.BarChart,
                                    contentDescription = stringResource(id = R.string.practices_home_topbar_stats)
                                )
                            }
                            IconButton(onClick = { onIntent(PracticesHomeIntent.OpenSearch) }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(id = R.string.practices_home_topbar_search)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onIntent(PracticesHomeIntent.Refresh) },
        state = pullRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { MoodSection(state = state, onIntent = onIntent) }
            item { RecommendedSection(state = state, onIntent = onIntent) }
            item { TodayPlanSection(state = state, onIntent = onIntent) }
            item { QuickRitualsSection(state = state, onIntent = onIntent) }
            item { RecentSection(state = state, onIntent = onIntent) }
            item { MyCoursesSection(state = state, onIntent = onIntent) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    val selectedMood = state.selectedMood
    val selectedColor = moodColor(selectedMood)
    val selectedIcon = moodIcon(selectedMood)
    val selectedDescription = stringResource(id = moodDescriptionRes(selectedMood))

    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with selected mood info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = selectedColor.copy(alpha = 0.16f),
                        disabledContainerColor = selectedColor.copy(alpha = 0.16f),
                        contentColor = selectedColor,
                        disabledContentColor = selectedColor
                    )
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.practices_home_mood_title),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Horizontal scroll for mood chips
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                state.availableMoods.forEach { mood ->
                    val isSelected = state.selectedMood.id == mood.id
                    val icon = moodIcon(mood)
                    val color = moodColor(mood)

                    IconButton(
                        onClick = { onIntent(PracticesHomeIntent.SelectMood(mood)) },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.24f) else color.copy(alpha = 0.08f),
                            contentColor = color
                        )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun moodIcon(mood: MoodChip) = when (mood) {
    MoodChip.Nervous -> Icons.Filled.SentimentDissatisfied
    MoodChip.Sleep -> Icons.Filled.Bedtime
    MoodChip.Focus -> Icons.Filled.Bolt
    MoodChip.Relax -> Icons.Filled.Spa
    MoodChip.Neutral -> Icons.Filled.SentimentNeutral
}

@Composable
private fun moodColor(mood: MoodChip): Color {
    val colors = MaterialTheme.colorScheme
    return when (mood) {
        MoodChip.Nervous -> colors.error
        MoodChip.Sleep -> colors.primary
        MoodChip.Focus -> colors.tertiary
        MoodChip.Relax -> colors.secondary
        MoodChip.Neutral -> colors.outline
    }
}

private fun moodDescriptionRes(mood: MoodChip): Int = when (mood) {
    MoodChip.Nervous -> R.string.practices_home_mood_nervous_description
    MoodChip.Sleep -> R.string.practices_home_mood_sleep_description
    MoodChip.Focus -> R.string.practices_home_mood_focus_description
    MoodChip.Relax -> R.string.practices_home_mood_relax_description
    MoodChip.Neutral -> R.string.practices_home_mood_neutral_description
}

@Composable
private fun RecommendedSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.practices_home_recommended_title),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                state.recommendedPractices.firstOrNull()?.let { practice ->
                    RecommendedItemCard(
                        title = practice.title,
                        goal = practice.goal?.let(::practiceGoalTitleRes),
                        durationMinutes = practice.durationSec?.div(60),
                        badge = if (practice.usageCount > 100) "Популярная" else null,
                        onClick = { onIntent(PracticesHomeIntent.OpenPractice(practice.id)) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                state.recommendedCourse?.let { course ->
                    RecommendedItemCard(
                        title = course.title,
                        goal = course.goal?.let(::practiceGoalTitleRes),
                        durationMinutes = course.totalDurationSec?.div(60),
                        badge = if (course.tags.contains("popular")) "Популярная" else null,
                        onClick = { onIntent(PracticesHomeIntent.OpenCourse(course.id)) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedItemCard(
    title: String,
    goal: Int?,
    durationMinutes: Int?,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    goal?.let { resId ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(id = resId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    durationMinutes?.let { minutes ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.practices_home_duration_minutes, minutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun practiceGoalTitleRes(goal: PracticeGoal): Int? = when (goal) {
    PracticeGoal.SLEEP -> R.string.practice_goal_sleep
    PracticeGoal.STRESS -> R.string.practice_goal_stress
    PracticeGoal.ENERGY -> R.string.practice_goal_energy
    PracticeGoal.FOCUS -> R.string.practice_goal_focus
    PracticeGoal.RELAXATION -> R.string.practice_goal_relaxation
    PracticeGoal.ANXIETY -> R.string.practice_goal_anxiety
    PracticeGoal.MOOD -> R.string.practice_goal_mood
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyCoursesSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    if (state.myCourses.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(id = R.string.practices_home_my_courses_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            items(state.myCourses) { course ->
                AmuletCard(
                    modifier = Modifier
                        .width(280.dp)
                        .clickable { onIntent(PracticesHomeIntent.OpenCourse(course.id)) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        course.goal?.let { goal ->
                            practiceGoalTitleRes(goal)?.let { resId ->
                                Text(
                                    text = stringResource(id = resId),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Прогресс бар
                        val progress = state.coursesProgress[course.id]?.percent?.toFloat()?.div(100) ?: 0f
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${state.coursesProgress[course.id]?.completedItemIds?.size ?: 0} из ${course.modulesCount} занятий",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayPlanSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.practices_home_today_plan_heading),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (state.hasPlan) {
                        Text(
                            text = "Сегодня запланировано ${state.scheduledSessions.size} сессии",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.practices_home_today_plan_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.hasPlan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.scheduledSessions.take(3).forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Отображение времени
                            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(session.scheduledTime))
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = session.practiceTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { onIntent(PracticesHomeIntent.CreateDayRitual) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.practices_home_today_plan_cta))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickRitualsSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    if (state.quickRituals.isEmpty()) return
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.practices_home_quick_rituals_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.quickRituals.forEach { practice ->
                    AssistChip(
                        onClick = { onIntent(PracticesHomeIntent.OpenPractice(practice.id)) },
                        label = { Text(practice.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSection(state: PracticesHomeState, onIntent: (PracticesHomeIntent) -> Unit) {
    if (state.recentSessions.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(id = R.string.practices_home_recent_title), style = MaterialTheme.typography.titleMedium)
        state.recentSessions.forEach { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = session.practiceTitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        session.durationSec?.let {
                            Text(
                                stringResource(id = R.string.practices_home_duration_minutes, it / 60),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Button(onClick = { onIntent(PracticesHomeIntent.OpenPractice(session.practiceId)) }) {
                        Text(stringResource(id = R.string.practices_home_action_open))
                    }
                }
            }
        }
    }
}
