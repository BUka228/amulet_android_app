package com.example.amulet.core.design.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.amulet.core.design.foundation.theme.AmuletTheme

enum class AvatarSize(val dp: Int) {
    Small(32),
    Medium(40),
    Large(56),
    ExtraLarge(80)
}

enum class AvatarStatus {
    Online,
    Offline,
    Away,
    Busy
}

@Composable
fun AmuletAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    initials: String? = null,
    size: AvatarSize = AvatarSize.Medium,
    status: AvatarStatus? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val dimension = size.dp.dp
    val statusSize = (dimension.value * 0.35f).dp
    Box(
        modifier = modifier
            .size(dimension)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            !imageUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }

            !initials.isNullOrBlank() -> {
                androidx.compose.material3.Text(
                    text = initials.take(2).uppercase(),
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Box(modifier = Modifier.matchParentSize())
            }
        }

        status?.let { statusValue ->
            val statusColor = when (statusValue) {
                AvatarStatus.Online -> AmuletTheme.colors.success
                AvatarStatus.Offline -> MaterialTheme.colorScheme.outline
                AvatarStatus.Away -> AmuletTheme.colors.warning
                AvatarStatus.Busy -> AmuletTheme.colors.error
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(statusSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size((statusSize.value * 0.75f).dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
        }
    }
}
