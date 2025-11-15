package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.runtime.Composable
import com.example.amulet.feature.patterns.presentation.components.TimelineEditorContent
import com.example.amulet.shared.domain.patterns.model.PatternElement
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline

@Composable
fun TimelineEditor(
    element: PatternElementTimeline,
    onUpdate: (PatternElement) -> Unit
) {
    TimelineEditorContent(element = element, onUpdate = onUpdate)
}
