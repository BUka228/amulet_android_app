package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.amulet.feature.patterns.R

/**
 * Dialog for handling unsaved changes when leaving the pattern editor.
 * 
 * Provides three options:
 * - Save: Save changes and exit
 * - Don't save: Discard changes and exit
 * - Cancel: Stay in the editor
 * 
 * @param onSave Callback when user chooses to save changes
 * @param onDiscard Callback when user chooses to discard changes
 * @param onCancel Callback when user cancels the action
 */
@Composable
fun DiscardChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = stringResource(R.string.dialog_discard_changes_title))
        },
        text = {
            Text(text = stringResource(R.string.dialog_discard_changes_message))
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(text = stringResource(R.string.dialog_discard_changes_save))
            }
        },
        dismissButton = {
            // Stack the two text buttons
            androidx.compose.foundation.layout.Row {
                TextButton(onClick = onDiscard) {
                    Text(text = stringResource(R.string.dialog_discard_changes_discard))
                }
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.dialog_discard_changes_cancel))
                }
            }
        }
    )
}
