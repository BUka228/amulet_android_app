package com.example.amulet.feature.practices.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.devices.model.BleConnectionState
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeDetailsRoute(
    practiceId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPattern: (String) -> Unit,
    onNavigateToPlan: (String) -> Unit,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToSession: (String) -> Unit,
    viewModel: PracticeDetailsViewModel = hiltViewModel()
) {
    viewModel.setIdIfEmpty(practiceId)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PracticeDetailsEffect.NavigateToPattern -> onNavigateToPattern(effect.patternId)
                is PracticeDetailsEffect.NavigateBack -> onNavigateBack()
                is PracticeDetailsEffect.NavigateToPlan -> onNavigateToPlan(effect.practiceId)
                is PracticeDetailsEffect.NavigateToCourse -> onNavigateToCourse(effect.courseId)
                is PracticeDetailsEffect.NavigateToSession -> onNavigateToSession(effect.practiceId)
                is PracticeDetailsEffect.NavigateToPairing -> onNavigateToPairing()
            }
        }
    }

    val scaffoldState = LocalScaffoldState.current
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(state.practice?.title.orEmpty()) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.handleIntent(PracticeDetailsIntent.NavigateBack) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            val isFavorite = state.isFavorite
                            IconButton(onClick = { viewModel.handleIntent(PracticeDetailsIntent.ToggleFavorite) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {},
                bottomBar = {
                    PracticeDetailsBottomBar(
                        isEnabled = state.practice != null && !state.isLoading,
                        isFavorite = state.isFavorite,
                        onStart = { viewModel.handleIntent(PracticeDetailsIntent.StartPractice) },
                        onAddToPlan = { viewModel.handleIntent(PracticeDetailsIntent.AddToPlan) },
                        onToggleFavorite = { viewModel.handleIntent(PracticeDetailsIntent.ToggleFavorite) }
                    )
                }
            )
        }
    }

    PracticeDetailsScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
private fun PracticeDetailsScreen(
    state: PracticeDetailsState,
    onIntent: (PracticeDetailsIntent) -> Unit
) {
    val practice = state.practice
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (practice != null) {
            HeroSection(practice = practice)

            practice.description?.takeIf { it.isNotBlank() }?.let {
                AboutSection(description = it)
            }

            if (practice.steps.isNotEmpty()) {
                HowItGoesSection(steps = practice.steps)
            }

            if (practice.audioUrl != null) {
                AudioInfoSection()
            }

            AmuletSection(
                pattern = state.pattern,
                connectionStatus = state.connectionStatus,
                onOpenPattern = { onIntent(PracticeDetailsIntent.OpenPattern) },
                onConnectAmulet = { onIntent(PracticeDetailsIntent.OpenPairing) }
            )

            if (state.courses.isNotEmpty()) {
                CoursesEmbeddingSection(
                    courses = state.courses,
                    onOpenCourse = { courseId: String -> onIntent(PracticeDetailsIntent.OpenCourse(courseId)) }
                )
            }

            if (practice.contraindications.isNotEmpty() || practice.safetyNotes.isNotEmpty()) {
                SafetySection(
                    contraindications = practice.contraindications,
                    safetyNotes = practice.safetyNotes
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroSection(practice: Practice) {
    val (containerColor, onContainerColor) = when (practice.goal) {
        PracticeGoal.SLEEP -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimary
        PracticeGoal.STRESS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiary
        PracticeGoal.ENERGY -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondary
        PracticeGoal.FOCUS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        PracticeGoal.RELAXATION -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondary
        PracticeGoal.ANXIETY -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onError
        PracticeGoal.MOOD -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondary
        null -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    AmuletCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = containerColor,
        contentColor = onContainerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SelfImprovement,
                    contentDescription = null
                )
                Text(
                    text = practice.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                practice.goal?.let { goal ->
                    val resId = goalTitleRes(goal)
                    AssistChip(
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor,
                            labelColor = onContainerColor
                        ),
                        onClick = {},
                        label = { Text(text = stringResource(id = resId)) }
                    )
                }
                    AssistChip(
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor,
                            labelColor = onContainerColor
                        ),
                        onClick = {},
                        label = { Text(text =   when (practice.type) {
                            PracticeType.BREATH -> stringResource(id = R.string.practice_type_breath)
                            PracticeType.MEDITATION -> stringResource(id = R.string.practice_type_meditation)
                            PracticeType.SOUND -> stringResource(id = R.string.practice_type_sound)
                        }) })

                practice.durationSec?.let { durationSec ->
                    val minutes = durationSec / 60
                    val durationText = stringResource(id = R.string.practices_home_duration_minutes, minutes)
                    AssistChip(
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor,
                            labelColor = onContainerColor
                        ),
                        onClick = {},
                        label = { Text(text = durationText) }
                    )
                }

                practice.level?.let { level ->
                    val levelText = when (level) {
                        PracticeLevel.BEGINNER -> stringResource(id = R.string.practice_level_beginner)
                        PracticeLevel.INTERMEDIATE -> stringResource(id = R.string.practice_level_intermediate)
                        PracticeLevel.ADVANCED -> stringResource(id = R.string.practice_level_advanced)
                    }
                    AssistChip(
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor,
                            labelColor = onContainerColor
                        ),
                        onClick = {},
                        label = { Text(text = levelText) }
                    )
                }

                if (practice.usageCount > 100) {
                    AssistChip(
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = containerColor,
                            labelColor = onContainerColor
                        ),
                        onClick = {},
                        label = { Text(text = stringResource(id = R.string.practice_badge_popular)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoursesEmbeddingSection(
    courses: List<Course>,
    onOpenCourse: (String) -> Unit
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.practice_details_courses_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                courses.forEach { course ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenCourse(course.id) }
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        course.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItGoesSection(steps: List<String>) {
    if (steps.isEmpty()) return

    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.practice_details_how_it_goes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            steps.forEachIndexed { index, step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSection(description: String) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.practice_details_about),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AudioInfoSection() {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.practice_details_audio_warning),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AmuletSection(
    pattern: com.example.amulet.shared.domain.patterns.model.Pattern?,
    connectionStatus: BleConnectionState,
    onOpenPattern: () -> Unit,
    onConnectAmulet: () -> Unit
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bluetooth,
                        contentDescription = null,
                        tint = when (connectionStatus) {
                            BleConnectionState.Connected -> MaterialTheme.colorScheme.primary
                            BleConnectionState.Connecting,
                            is BleConnectionState.Reconnecting -> MaterialTheme.colorScheme.tertiary
                            is BleConnectionState.Failed,
                            BleConnectionState.Disconnected -> MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = stringResource(id = R.string.practice_details_amulet),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onOpenPattern, enabled = pattern != null) {
                    Text(text = stringResource(id = R.string.practice_details_open_pattern))
                }
            }

            if (pattern != null) {
                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                pattern.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.practice_details_pattern_auto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (connectionStatus !is BleConnectionState.Connected) {
                Button(onClick = onConnectAmulet, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Filled.Bluetooth, contentDescription = null)
                    Text(text = stringResource(id = R.string.practice_details_connect_amulet))
                }
            }
        }
    }
}

@Composable
private fun SafetySection(
    contraindications: List<String>,
    safetyNotes: List<String>
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(id = R.string.practice_details_safety),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            contraindications.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (safetyNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                safetyNotes.forEach { note ->
                    Text(
                        text = "• $note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeDetailsBottomBar(
    isEnabled: Boolean,
    onStart: () -> Unit,
    onAddToPlan: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onAddToPlan,
                enabled = isEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.practice_details_add_to_plan))
            }

            Button(
                onClick = onStart,
                enabled = isEnabled,
                modifier = Modifier.weight(1.4f)
            ) {
                Text(text = stringResource(id = R.string.practice_details_start))
            }
        }
    }
}

@Composable
private fun goalTitleRes(goal: PracticeGoal): Int = when (goal) {
    PracticeGoal.SLEEP -> R.string.practice_goal_sleep
    PracticeGoal.STRESS -> R.string.practice_goal_stress
    PracticeGoal.ENERGY -> R.string.practice_goal_energy
    PracticeGoal.FOCUS -> R.string.practice_goal_focus
    PracticeGoal.RELAXATION -> R.string.practice_goal_relaxation
    PracticeGoal.ANXIETY -> R.string.practice_goal_anxiety
    PracticeGoal.MOOD -> R.string.practice_goal_mood
}

