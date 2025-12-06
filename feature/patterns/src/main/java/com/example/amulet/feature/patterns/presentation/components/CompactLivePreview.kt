package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.PatternSpec

/**
 * Компактный компонент живого превью паттерна для BottomSheet.
 * Отображает кольцо с кнопками управления сбоку.
 */
@Composable
fun CompactLivePreview(
    spec: PatternSpec?,
    isPlaying: Boolean,
    loop: Boolean,
    onPlayPause: () -> Unit,
    onToggleLoop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопки управления слева
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp)
        ) {
            
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) {
                        stringResource(R.string.cd_pause_pattern)
                    } else {
                        stringResource(R.string.cd_play_pattern)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Surface(
                onClick = onToggleLoop,
                shape = MaterialTheme.shapes.medium,
                color = if (loop) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = stringResource(R.string.pattern_editor_loop_label),
                        tint = if (loop) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Кольцо превью по центру
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            AmuletAvatar2D(
                spec = spec?.copy(loop = loop),
                isPlaying = isPlaying,
                size = 120.dp,
                ledRadius = 10.dp
            )
        }
        
        // Пустое пространство справа для баланса
        Box(modifier = Modifier.size(32.dp))
    }
}
