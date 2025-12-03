package com.example.amulet.feature.hugs.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsHistoryRoute(
    onNavigateBack: () -> Unit = {},
    onOpenDetails: (String) -> Unit = {},
    viewModel: HugsHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.hugs_history_title)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HugsHistoryEffect.ShowError -> {
                    // отображаем ошибку через карточку ниже
                }
                is HugsHistoryEffect.OpenDetails -> {
                    onOpenDetails(effect.hugId)
                }
            }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing || state.isLoading,
        onRefresh = { viewModel.onIntent(HugsHistoryIntent.Refresh) },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        HugsHistoryScreen(
            state = state,
            visibleHugs = viewModel.visibleHugs(),
            availableEmotionKeys = viewModel.availableEmotionKeys(),
            onIntent = viewModel::onIntent,
        )
    }
}

private fun formatHugDateTime(hug: Hug): String {
    val dt = hug.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dt.date.dayOfMonth.toString().padStart(2, '0')
    val month = (dt.date.month.ordinal + 1).toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$day.$month $hour:$minute"
}

@Composable
private fun HugsHistoryScreen(
    state: HugsHistoryState,
    visibleHugs: List<Hug>,
    availableEmotionKeys: List<String>,
    onIntent: (HugsHistoryIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFiltersRow(state = state, availableEmotionKeys = availableEmotionKeys, onIntent = onIntent)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleHugs, key = { it.id.value }) { hug ->
                HistoryHugItem(
                    hug = hug,
                    isPinned = state.pinnedIds.contains(hug.id.value),
                    currentUserId = state.currentUser?.id?.value,
                    onTogglePin = { onIntent(HugsHistoryIntent.TogglePin(hug.id.value)) },
                    onOpenDetails = { onIntent(HugsHistoryIntent.OpenDetails(hug.id.value)) },
                )
            }
        }

        state.error?.let { error ->
            HistoryErrorCard(error = error)
        }
    }
}

@Composable
private fun HistoryFiltersRow(
    state: HugsHistoryState,
    availableEmotionKeys: List<String>,
    onIntent: (HugsHistoryIntent) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок фильтров
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.hugs_history_filters_title),
                style = MaterialTheme.typography.titleSmall
            )
        }

        // Фильтры направления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.directionFilter == HugsHistoryDirectionFilter.ALL,
                onClick = { onIntent(HugsHistoryIntent.ChangeDirection(HugsHistoryDirectionFilter.ALL)) },
                label = { Text(stringResource(R.string.hugs_history_filter_all)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            FilterChip(
                selected = state.directionFilter == HugsHistoryDirectionFilter.SENT,
                onClick = { onIntent(HugsHistoryIntent.ChangeDirection(HugsHistoryDirectionFilter.SENT)) },
                label = { Text(stringResource(R.string.hugs_history_filter_sent)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            FilterChip(
                selected = state.directionFilter == HugsHistoryDirectionFilter.RECEIVED,
                onClick = { onIntent(HugsHistoryIntent.ChangeDirection(HugsHistoryDirectionFilter.RECEIVED)) },
                label = { Text(stringResource(R.string.hugs_history_filter_received)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Фильтры периода
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.periodFilter == HugsHistoryPeriodFilter.ALL_TIME,
                onClick = { onIntent(HugsHistoryIntent.ChangePeriod(HugsHistoryPeriodFilter.ALL_TIME)) },
                label = { Text(stringResource(R.string.hugs_history_period_all_time)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            FilterChip(
                selected = state.periodFilter == HugsHistoryPeriodFilter.LAST_7_DAYS,
                onClick = { onIntent(HugsHistoryIntent.ChangePeriod(HugsHistoryPeriodFilter.LAST_7_DAYS)) },
                label = { Text(stringResource(R.string.hugs_history_period_last_7_days)) }
            )
            FilterChip(
                selected = state.periodFilter == HugsHistoryPeriodFilter.LAST_24_HOURS,
                onClick = { onIntent(HugsHistoryIntent.ChangePeriod(HugsHistoryPeriodFilter.LAST_24_HOURS)) },
                label = { Text(stringResource(R.string.hugs_history_period_last_24_hours)) }
            )
        }

        // Фильтры эмоций
        if (availableEmotionKeys.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onIntent(HugsHistoryIntent.SelectEmotion(null)) },
                    label = { Text(stringResource(R.string.hugs_history_emotions_all)) }
                )
                availableEmotionKeys.take(4).forEach { key ->
                    AssistChip(
                        onClick = { onIntent(HugsHistoryIntent.SelectEmotion(key)) },
                        label = { Text(stringResource(R.string.hugs_history_emotion_chip)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHugItem(
    hug: Hug,
    isPinned: Boolean,
    currentUserId: String?,
    onTogglePin: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val isSentByMe = currentUserId != null && hug.fromUserId.value == currentUserId
    
    val (statusIcon, statusText, statusColor) = when (hug.status) {
        HugStatus.SENT -> Triple(
            Icons.Filled.Send,
            stringResource(R.string.hugs_home_status_sent),
            Color(0xFF2196F3)
        )
        HugStatus.DELIVERED -> Triple(
            Icons.Filled.DoneAll,
            stringResource(R.string.hugs_home_status_delivered),
            Color(0xFF4CAF50)
        )
        HugStatus.READ -> Triple(
            Icons.Filled.Visibility,
            stringResource(R.string.hugs_home_status_read),
            Color(0xFF4CAF50)
        )
        HugStatus.EXPIRED -> Triple(
            Icons.Filled.Schedule,
            stringResource(R.string.hugs_home_status_expired),
            Color(0xFF9E9E9E)
        )
    }

    val emotionColor = runCatching {
        val hex = hug.emotion.colorHex
        if (hex.isNullOrBlank()) Color.Gray else Color(android.graphics.Color.parseColor(hex))
    }.getOrElse { Color.Gray }

    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Большой круг эмоции
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(emotionColor)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Направление с иконкой
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSentByMe) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = if (isSentByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isSentByMe) stringResource(R.string.hugs_history_direction_sent) else stringResource(R.string.hugs_history_direction_received),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        // Статус с иконкой
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Кнопка закрепления
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Текст сообщения
            hug.payload?.get("text")?.toString()?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Время
            Text(
                text = formatHugDateTime(hug),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun HistoryErrorCard(error: AppError) {
    AmuletCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.hugs_history_error_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
