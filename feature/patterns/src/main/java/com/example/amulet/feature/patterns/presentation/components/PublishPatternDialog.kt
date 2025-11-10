package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R

/**
 * Data class representing the publish pattern form state.
 */
data class PublishPatternData(
    val publicTitle: String = "",
    val publicDescription: String = "",
    val tags: String = "",
    val category: String = "",
    val makePublicImmediately: Boolean = true
)

/**
 * Fullscreen dialog for publishing a pattern to the public library.
 * 
 * Allows users to provide public-facing information including:
 * - Public title and description
 * - Tags for discoverability
 * - Category selection
 * - Option to make public immediately
 * 
 * @param initialData Initial form data (optional)
 * @param onConfirm Callback when user confirms publication with the form data
 * @param onDismiss Callback when user cancels or dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPatternDialog(
    initialData: PublishPatternData = PublishPatternData(),
    onConfirm: (PublishPatternData) -> Unit,
    onDismiss: () -> Unit
) {
    var publicTitle by remember { mutableStateOf(initialData.publicTitle) }
    var publicDescription by remember { mutableStateOf(initialData.publicDescription) }
    var tags by remember { mutableStateOf(initialData.tags) }
    var selectedCategory by remember { mutableStateOf(initialData.category) }
    var makePublicImmediately by remember { mutableStateOf(initialData.makePublicImmediately) }

    // Available categories
    val categories = listOf(
        "Meditation",
        "Breathing",
        "Notifications",
        "Celebrations",
        "Ambient",
        "Other"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.dialog_publish_pattern_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.dialog_publish_pattern_cancel)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Public Title
                AmuletTextField(
                    value = publicTitle,
                    onValueChange = { publicTitle = it },
                    label = stringResource(R.string.dialog_publish_pattern_public_title),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Public Description
                AmuletTextField(
                    value = publicDescription,
                    onValueChange = { publicDescription = it },
                    label = stringResource(R.string.dialog_publish_pattern_public_description),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                // Tags
                AmuletTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = stringResource(R.string.dialog_publish_pattern_tags),
                    helperText = "Separate tags with commas",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                // Category Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dialog_publish_pattern_category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Category chips in a flow layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(3).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(3).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                // Make Public Immediately Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dialog_publish_pattern_make_public),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = makePublicImmediately,
                        onCheckedChange = { makePublicImmediately = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.dialog_publish_pattern_cancel))
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                PublishPatternData(
                                    publicTitle = publicTitle,
                                    publicDescription = publicDescription,
                                    tags = tags,
                                    category = selectedCategory,
                                    makePublicImmediately = makePublicImmediately
                                )
                            )
                        },
                        enabled = publicTitle.isNotBlank() && selectedCategory.isNotBlank()
                    ) {
                        Text(text = stringResource(R.string.dialog_publish_pattern_confirm))
                    }
                }
            }
        }
    }
}
