package com.example.amulet.feature.devices.presentation.pairing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.devices.R

/**
 * BottomSheet для ручного ввода серийного номера и claim token.
 * 
 * Включает валидацию формата и подсказки для пользователя.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (serialNumber: String, claimToken: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var serialNumber by remember { mutableStateOf("") }
    var claimToken by remember { mutableStateOf("") }
    var serialNumberError by remember { mutableStateOf<String?>(null) }
    var claimTokenError by remember { mutableStateOf<String?>(null) }
    
    val serialFocusRequester = remember { FocusRequester() }
    val tokenFocusRequester = remember { FocusRequester() }
    
    // Строки для валидации
    val errorSerialRequired = stringResource(R.string.pairing_manual_error_serial_required)
    val errorSerialShort = stringResource(R.string.pairing_manual_error_serial_short)
    val errorTokenRequired = stringResource(R.string.pairing_manual_error_token_required)
    val errorTokenShort = stringResource(R.string.pairing_manual_error_token_short)
    
    // Автофокус на первое поле
    LaunchedEffect(Unit) {
        serialFocusRequester.requestFocus()
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pairing_manual_entry_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close)
                    )
                }
            }
            
            // Информационная карточка
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.pairing_manual_entry_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Поле серийного номера
            OutlinedTextField(
                value = serialNumber,
                onValueChange = { 
                    serialNumber = it.uppercase().filter { char -> 
                        char.isLetterOrDigit() || char == '-'
                    }
                    serialNumberError = null
                },
                label = { Text(stringResource(R.string.pairing_manual_serial_label)) },
                placeholder = { Text(stringResource(R.string.pairing_manual_serial_placeholder)) },
                supportingText = {
                    if (serialNumberError != null) {
                        Text(serialNumberError!!)
                    } else {
                        Text(stringResource(R.string.pairing_manual_serial_format))
                    }
                },
                isError = serialNumberError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { tokenFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .focusRequester(serialFocusRequester)
            )
            
            // Поле claim token
            OutlinedTextField(
                value = claimToken,
                onValueChange = { 
                    claimToken = it.filter { char -> char.isLetterOrDigit() }
                    claimTokenError = null
                },
                label = { Text(stringResource(R.string.pairing_manual_token_label)) },
                placeholder = { Text(stringResource(R.string.pairing_manual_token_placeholder)) },
                supportingText = {
                    if (claimTokenError != null) {
                        Text(claimTokenError!!)
                    } else {
                        Text(stringResource(R.string.pairing_manual_token_format))
                    }
                },
                isError = claimTokenError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (validateAndSubmit(
                                serialNumber, 
                                claimToken,
                                errorSerialRequired,
                                errorSerialShort,
                                errorTokenRequired,
                                errorTokenShort,
                                onSerialError = { serialNumberError = it },
                                onTokenError = { claimTokenError = it },
                                onSubmit = onSubmit
                            )) {
                            onDismiss()
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(tokenFocusRequester)
            )
            
            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
                
                Button(
                    onClick = {
                        if (validateAndSubmit(
                                serialNumber, 
                                claimToken,
                                errorSerialRequired,
                                errorSerialShort,
                                errorTokenRequired,
                                errorTokenShort,
                                onSerialError = { serialNumberError = it },
                                onTokenError = { claimTokenError = it },
                                onSubmit = onSubmit
                            )) {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1f),
                    enabled = serialNumber.isNotBlank() && claimToken.isNotBlank()
                ) {
                    Text(text = stringResource(R.string.pairing_manual_submit_button))
                }
            }
        }
    }
}

/**
 * Валидация и отправка данных.
 * Возвращает true если валидация прошла успешно.
 */
private fun validateAndSubmit(
    serialNumber: String,
    claimToken: String,
    errorSerialRequired: String,
    errorSerialShort: String,
    errorTokenRequired: String,
    errorTokenShort: String,
    onSerialError: (String) -> Unit,
    onTokenError: (String) -> Unit,
    onSubmit: (String, String) -> Unit
): Boolean {
    var isValid = true
    
    // Валидация серийного номера (минимум 8 символов)
    if (serialNumber.isBlank()) {
        onSerialError(errorSerialRequired)
        isValid = false
    } else if (serialNumber.length < 8) {
        onSerialError(errorSerialShort)
        isValid = false
    }
    
    // Валидация claim token (минимум 16 символов)
    if (claimToken.isBlank()) {
        onTokenError(errorTokenRequired)
        isValid = false
    } else if (claimToken.length < 16) {
        onTokenError(errorTokenShort)
        isValid = false
    }
    
    if (isValid) {
        onSubmit(serialNumber, claimToken)
    }
    
    return isValid
}
