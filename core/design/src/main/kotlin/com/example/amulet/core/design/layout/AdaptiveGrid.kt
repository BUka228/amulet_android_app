package com.example.amulet.core.design.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.foundation.responsive.Breakpoints
import com.example.amulet.core.design.foundation.responsive.rememberScreenWidth
import com.example.amulet.core.design.foundation.theme.AmuletTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AdaptiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    val width = rememberScreenWidth()
    val columns = remember(width) {
        when {
            width < Breakpoints.Small -> 1
            width < Breakpoints.Medium -> 2
            else -> 3
        }
    }
    val state = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columns),
        state = state,
        horizontalArrangement = Arrangement.spacedBy(AmuletTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AmuletTheme.spacing.lg),
        content = {
            items(items) { item ->
                itemContent(item)
            }
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AmuletTheme.spacing.lg,
            vertical = AmuletTheme.spacing.lg
        )
    )
}
