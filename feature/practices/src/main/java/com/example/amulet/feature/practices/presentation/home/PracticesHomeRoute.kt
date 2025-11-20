package com.example.amulet.feature.practices.presentation.home

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
                PracticesHomeEffect.NavigateToSearch -> Unit
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
    var localSearchQuery by remember(state.isSearchMode) { mutableStateOf(state.searchQuery) }

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    if (state.isSearchMode) {
                        TopAppBar(
                            title = {
                                TextField(
                                    value = localSearchQuery,
                                    onValueChange = {
                                        localSearchQuery = it
                                        onIntent(PracticesHomeIntent.ChangeSearchQuery(it))
                                    },
                                    placeholder = {
                                        Text(
                                            text = stringResource(id = R.string.practices_home_topbar_search),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        errorContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,
                                    )
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { onIntent(PracticesHomeIntent.ExitSearch) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            },
                            actions = {}
                        )
                    } else {
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
                                IconButton(onClick = { onIntent(PracticesHomeIntent.EnterSearch) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(id = R.string.practices_home_topbar_search)
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {}
            )
        }
    }

    if (state.isSearchMode) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.searchResults) { index, practice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{ onIntent(PracticesHomeIntent.OpenPractice(practice.id))},
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onIntent(PracticesHomeIntent.OpenPractice(practice.id)) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                }
                if (index != state.searchResults.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    } else {
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.availableMoods.forEach { mood ->
                    val isSelected = state.selectedMood.id == mood.id
                    val icon = moodIcon(mood)
                    val color = moodColor(mood)

                    IconButton(
                        onClick = { onIntent(PracticesHomeIntent.SelectMood(mood)) },
                        modifier = Modifier.size(40.dp),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
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
        IconButton(onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.practices_home_my_courses_title),
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                state.myCourses.forEachIndexed { index, course ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(course.title, style = MaterialTheme.typography.bodyLarge)
                            course.goal?.let { goal ->
                                practiceGoalTitleRes(goal)?.let { resId ->
                                    Text(stringResource(id = resId), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        IconButton(onClick = { onIntent(PracticesHomeIntent.OpenCourse(course.id)) }) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                    if (index != state.myCourses.lastIndex) {
                        Divider()
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
                            text = stringResource(id = R.string.practices_home_today_plan_exists),
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

            if (!state.hasPlan) {
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
                            stringResource(id = R.string.practices_home_session_title, session.practiceId),
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
