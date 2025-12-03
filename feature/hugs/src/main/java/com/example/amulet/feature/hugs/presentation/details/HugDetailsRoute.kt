package com.example.amulet.feature.hugs.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugDetailsRoute(
    hugId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToReply: () -> Unit = {},
    viewModel: HugDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(hugId) {
        viewModel.setHugId(hugId)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HugDetailsEffect.NavigateBack -> onNavigateBack()
                HugDetailsEffect.NavigateToReply -> onNavigateToReply()
                is HugDetailsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.error.toString(),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.hug_details_title)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {},
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            )
        }
    }

    HugDetailsScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
private fun HugDetailsScreen(
    state: HugDetailsState,
    onIntent: (HugDetailsIntent) -> Unit
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        state.hug != null -> {
            HugDetailsContent(
                hug = state.hug,
                onIntent = onIntent
            )
        }
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.hug_details_error_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HugDetailsContent(
    hug: Hug,
    onIntent: (HugDetailsIntent) -> Unit
) {
    val emotionColor = remember(hug.emotion.colorHex) {
        parseEmotionColor(hug.emotion.colorHex)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hero секция с эмоцией
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Большой круг с цветом эмоции
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(emotionColor)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Пустое тело - просто круг с цветом
                }

                Text(
                    text = stringResource(R.string.hug_details_hero_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Статус карточка
        item {
            val (statusIcon, statusText, statusColor) = remember(hug.status) {
                when (hug.status) {
                    HugStatus.SENT -> Triple(
                        Icons.Filled.Send,
                        "Отправлено",
                        Color(0xFF2196F3) // Blue
                    )
                    HugStatus.DELIVERED -> Triple(
                        Icons.Filled.DoneAll,
                        "Доставлено",
                        Color(0xFF4CAF50) // Green
                    )
                    HugStatus.READ -> Triple(
                        Icons.Filled.Visibility,
                        "Прочитано",
                        Color(0xFF4CAF50) // Green
                    )
                    HugStatus.EXPIRED -> Triple(
                        Icons.Filled.Schedule,
                        "Просрочено",
                        Color(0xFF9E9E9E) // Gray
                    )
                }
            }

            AmuletCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hug_details_status_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Карточка времени
        item {
            val formattedTime = remember(hug.createdAt) {
                val dt = hug.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                val day = dt.date.dayOfMonth.toString().padStart(2, '0')
                val month = (dt.date.month.ordinal + 1).toString().padStart(2, '0')
                val year = dt.date.year
                val hour = dt.hour.toString().padStart(2, '0')
                val minute = dt.minute.toString().padStart(2, '0')
                "$day.$month.$year $hour:$minute"
            }

            AmuletCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hug_details_time_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Действия
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hug.status == HugStatus.DELIVERED || hug.status == HugStatus.READ) {
                    Button(
                        onClick = { onIntent(HugDetailsIntent.Reply) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.hug_details_reply_button))
                    }
                }

                TextButton(
                    onClick = { onIntent(HugDetailsIntent.Delete) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.hug_details_delete_button))
                }
            }
        }

        // Отступ снизу
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun parseEmotionColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color.Gray
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (_: Exception) {
        Color.Gray
    }
}
