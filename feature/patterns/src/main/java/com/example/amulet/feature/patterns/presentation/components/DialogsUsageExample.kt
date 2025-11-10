package com.example.amulet.feature.patterns.presentation.components

/**
 * Usage examples for the confirmation dialogs.
 * 
 * This file demonstrates how to integrate the dialogs into your screens.
 * Delete this file after integration is complete.
 */

/*
// Example 1: DeleteConfirmationDialog
// Use in PatternsListScreen or PatternEditorScreen

var showDeleteDialog by remember { mutableStateOf(false) }

if (showDeleteDialog) {
    DeleteConfirmationDialog(
        patternName = pattern.title, // Optional
        onConfirm = {
            viewModel.onEvent(PatternsListEvent.DeletePattern(patternId))
            showDeleteDialog = false
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )
}

// Trigger the dialog
IconButton(onClick = { showDeleteDialog = true }) {
    Icon(Icons.Default.Delete, contentDescription = "Delete")
}


// Example 2: DiscardChangesDialog
// Use in PatternEditorScreen when user tries to navigate back with unsaved changes

var showDiscardDialog by remember { mutableStateOf(false) }

if (showDiscardDialog) {
    DiscardChangesDialog(
        onSave = {
            viewModel.onEvent(PatternEditorEvent.SavePattern)
            showDiscardDialog = false
            navController.navigateUp()
        },
        onDiscard = {
            showDiscardDialog = false
            navController.navigateUp()
        },
        onCancel = {
            showDiscardDialog = false
        }
    )
}

// Intercept back navigation
BackHandler(enabled = state.hasUnsavedChanges) {
    showDiscardDialog = true
}


// Example 3: PublishPatternDialog
// Use in PatternEditorScreen or PatternPreviewScreen

var showPublishDialog by remember { mutableStateOf(false) }

if (showPublishDialog) {
    PublishPatternDialog(
        initialData = PublishPatternData(
            publicTitle = state.pattern?.title ?: "",
            publicDescription = state.pattern?.description ?: ""
        ),
        onConfirm = { data ->
            viewModel.onEvent(PatternEditorEvent.PublishPattern(data))
            showPublishDialog = false
        },
        onDismiss = {
            showPublishDialog = false
        }
    )
}

// Trigger the dialog
Button(onClick = { showPublishDialog = true }) {
    Text("Publish")
}
*/
