package com.example.amulet.feature.practices.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.practices.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticesSearchRoute(
    onOpenPractice: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PracticesSearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PracticesSearchScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenPractice = onOpenPractice,
        onOpenCourse = onOpenCourse,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticesSearchScreen(
    state: PracticesSearchState,
    onEvent: (PracticesSearchEvent) -> Unit,
    onOpenPractice: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current
    
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            // Use key to preserve cursor position
                            TextField(
                                value = state.query,
                                onValueChange = { newValue ->
                                    onEvent(PracticesSearchEvent.OnQueryChange(newValue))
                                },
                                placeholder = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(R.string.practices_search_hint),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onEvent(PracticesSearchEvent.OnQueryChange("")) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Filters - always visible
        item {
            QuickFiltersSection(
                state = state,
                onEvent = onEvent
            )
        }

        // Advanced Filters Button
        item {
            OutlinedButton(
                onClick = { onEvent(PracticesSearchEvent.OnToggleAdvancedFilters) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.practices_search_advanced_filters))
            }
        }

        // Loading Indicator
        if (state.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        // Error State
        state.error?.let { error ->
            item {
                ErrorMessage(error = error)
            }
        }

        // Results or Recommendations or Empty
        if (state.results.isNotEmpty()) {
            items(state.results) { item ->
                when (item) {
                    is SearchResultItem.HeaderItem -> {
                        Text(
                            text = when (item.type) {
                                SearchResultHeaderType.COURSES -> stringResource(R.string.practices_search_header_courses)
                                SearchResultHeaderType.PRACTICES -> stringResource(R.string.practices_search_header_practices)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    is SearchResultItem.CourseItem -> {
                        CourseCard(
                            course = item.course,
                            onClick = { onOpenCourse(item.course.id) }
                        )
                    }
                    is SearchResultItem.PracticeItem -> {
                        PracticeCard(
                            practice = item.practice,
                            onClick = { onOpenPractice(item.practice.id) }
                        )
                    }
                }
            }
        } else if (!state.isLoading && (state.query.isNotEmpty() || state.filter != PracticeFilter() || state.isCoursesFilterSelected)) {
            // Empty search result
            item {
                EmptySearchResult(
                    onClearFilters = { onEvent(PracticesSearchEvent.OnClearFilters) }
                )
            }
        } else if (state.query.isEmpty() && state.filter == PracticeFilter() && !state.isCoursesFilterSelected) {
            // Recommendations when no query
            item {
                Text(
                    text = stringResource(R.string.practices_search_recommendations_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (state.isLoadingRecommendations) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(state.recommendations, key = { it.id }) { practice ->
                    PracticeCard(
                        practice = practice,
                        onClick = { onOpenPractice(practice.id) },
                        showBadge = true
                    )
                }
            }
        }
    }

    // Advanced Filters Bottom Sheet
    if (state.isAdvancedFiltersVisible) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(PracticesSearchEvent.OnToggleAdvancedFilters) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AdvancedFiltersContent(
                state = state,
                onEvent = onEvent,
                onApply = { onEvent(PracticesSearchEvent.OnToggleAdvancedFilters) },
                onClear = { onEvent(PracticesSearchEvent.OnClearFilters) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickFiltersSection(
    state: PracticesSearchState,
    onEvent: (PracticesSearchEvent) -> Unit
) {
    AmuletCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AnimatedVisibility(
                visible = state.isQuickFiltersExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Type Filters (including Courses)
                    Text(
                        text = stringResource(R.string.practices_search_filter_type),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Courses Filter Chip
                        FilterChip(
                            selected = state.isCoursesFilterSelected,
                            onClick = {
                                onEvent(PracticesSearchEvent.OnCoursesFilterChange(!state.isCoursesFilterSelected))
                            },
                            label = { Text(stringResource(R.string.practices_search_type_courses_chip)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        PracticeType.entries.forEach { type ->
                            val isSelected = state.filter.type == type
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onEvent(
                                        PracticesSearchEvent.OnTypeFilterChange(
                                            if (isSelected) null else type
                                        )
                                    )
                                },
                                label = { Text(type.displayName()) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = type.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) type.color() else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = type.color().copy(alpha = 0.16f),
                                    selectedLabelColor = type.color(),
                                    selectedLeadingIconColor = type.color()
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Practice Goal Filters
                    Text(
                        text = stringResource(R.string.practices_search_filter_goal),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PracticeGoal.entries.forEach { goal ->
                            val isSelected = state.filter.goal == goal
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onEvent(
                                        PracticesSearchEvent.OnGoalFilterChange(
                                            if (isSelected) null else goal
                                        )
                                    )
                                },
                                label = { Text(goal.displayName()) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = goal.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) goal.color() else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = goal.color().copy(alpha = 0.16f),
                                    selectedLabelColor = goal.color(),
                                    selectedLeadingIconColor = goal.color()
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Collapse/Expand arrow at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEvent(PracticesSearchEvent.OnToggleQuickFilters) },
                contentAlignment = Alignment.Center
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (state.isQuickFiltersExpanded) 180f else 0f,
                    label = "collapse_arrow_rotation"
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFiltersContent(
    state: PracticesSearchState,
    onEvent: (PracticesSearchEvent) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.practices_search_advanced_filters),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Level Filter
        Text(
            text = stringResource(R.string.practices_search_filter_level),
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PracticeLevel.entries.forEach { level ->
                FilterChip(
                    selected = state.filter.level == level,
                    onClick = {
                        onEvent(
                            PracticesSearchEvent.OnLevelFilterChange(
                                if (state.filter.level == level) null else level
                            )
                        )
                    },
                    label = { Text(level.displayName()) }
                )
            }
        }

        // Duration Range
        Text(
            text = stringResource(R.string.practices_search_filter_duration),
            style = MaterialTheme.typography.titleSmall
        )
        
        val durationRange = remember(state.filter.durationFromSec, state.filter.durationToSec) {
            (state.filter.durationFromSec?.div(60)?.toFloat() ?: 1f)..(state.filter.durationToSec?.div(60)?.toFloat() ?: 60f)
        }
        
        RangeSlider(
            value = durationRange,
            onValueChange = { range ->
                onEvent(
                    PracticesSearchEvent.OnDurationRangeChange(
                        fromSec = (range.start * 60).toInt(),
                        toSec = (range.endInclusive * 60).toInt()
                    )
                )
            },
            valueRange = 1f..60f,
            steps = 58
        )
        
        Text(
            text = stringResource(
                R.string.practices_search_duration_format,
                durationRange.start.toInt(),
                durationRange.endInclusive.toInt()
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Additional Options
        Text(
            text = stringResource(R.string.practices_search_filter_additional),
            style = MaterialTheme.typography.titleSmall
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.practices_search_filter_audio))
            Switch(
                checked = state.filter.hasAudio == true,
                onCheckedChange = { 
                    onEvent(PracticesSearchEvent.OnHasAudioChange(if (it) true else null)) 
                }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.practices_search_filter_amulet))
            Switch(
                checked = state.filter.amuletRequired == true,
                onCheckedChange = { 
                    onEvent(PracticesSearchEvent.OnAmuletRequiredChange(if (it) true else null)) 
                }
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.practices_search_reset))
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.practices_search_apply))
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Курс", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                    course.goal?.let { goal ->
                        AssistChip(
                            onClick = { },
                            label = { Text(goal.displayName(), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    course.totalDurationSec?.let { duration ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.practices_home_duration_minutes, duration / 60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    course.level?.let { level ->
                        Text(
                            text = level.displayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeCard(
    practice: Practice,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (showBadge && practice.usageCount > 100) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.practice_badge_popular),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(practice.type.displayName(), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                    practice.goal?.let { goal ->
                        AssistChip(
                            onClick = { },
                            label = { Text(goal.displayName(), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    practice.durationSec?.let { duration ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.practices_home_duration_minutes, duration / 60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    practice.level?.let { level ->
                        Text(
                            text = level.displayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (practice.audioUrl != null) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResult(
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.practices_search_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.practices_search_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onClearFilters) {
            Text(stringResource(R.string.practices_search_clear_filters))
        }
    }
}

@Composable
private fun ErrorMessage(error: com.example.amulet.shared.core.AppError) {
    val errorMessage = when (error) {
        is com.example.amulet.shared.core.AppError.Server ->
            error.message ?: stringResource(R.string.practices_error_server)
        is com.example.amulet.shared.core.AppError.Network ->
            stringResource(R.string.practices_error_network)
        is com.example.amulet.shared.core.AppError.Timeout ->
            stringResource(R.string.practices_error_timeout)
        else -> stringResource(R.string.practices_error_generic)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// Extension functions for display names
@Composable
private fun PracticeType.displayName(): String = when (this) {
    PracticeType.BREATH -> stringResource(R.string.practice_type_breath)
    PracticeType.MEDITATION -> stringResource(R.string.practice_type_meditation)
    PracticeType.SOUND -> stringResource(R.string.practice_type_sound)
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

// Icon helpers
@Composable
private fun PracticeType.icon(): ImageVector = when (this) {
    PracticeType.BREATH -> Icons.Filled.Air
    PracticeType.MEDITATION -> Icons.Filled.Spa
    PracticeType.SOUND -> Icons.Filled.MusicNote
}

@Composable
private fun PracticeGoal.icon(): ImageVector = when (this) {
    PracticeGoal.SLEEP -> Icons.Filled.Bedtime
    PracticeGoal.STRESS -> Icons.Filled.SelfImprovement
    PracticeGoal.ENERGY -> Icons.Filled.Bolt
    PracticeGoal.FOCUS -> Icons.Filled.Psychology
    PracticeGoal.RELAXATION -> Icons.Filled.Spa
    PracticeGoal.ANXIETY -> Icons.Filled.Favorite
    PracticeGoal.MOOD -> Icons.Filled.Mood
}

// Color helpers
@Composable
private fun PracticeType.color(): Color = when (this) {
    PracticeType.BREATH -> MaterialTheme.colorScheme.tertiary
    PracticeType.MEDITATION -> MaterialTheme.colorScheme.primary
    PracticeType.SOUND -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun PracticeGoal.color(): Color {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        PracticeGoal.SLEEP -> colors.primary
        PracticeGoal.STRESS -> colors.error.copy(alpha = 0.8f)
        PracticeGoal.ENERGY -> colors.tertiary
        PracticeGoal.FOCUS -> Color(0xFF6366F1) // Indigo
        PracticeGoal.RELAXATION -> colors.secondary
        PracticeGoal.ANXIETY -> Color(0xFFEC4899) // Pink
        PracticeGoal.MOOD -> Color(0xFFF59E0B) // Amber
    }
}
