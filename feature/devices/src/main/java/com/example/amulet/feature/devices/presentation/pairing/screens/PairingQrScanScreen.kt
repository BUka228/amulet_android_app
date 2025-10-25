package com.example.amulet.feature.devices.presentation.pairing.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.presentation.pairing.PairingViewModel
import com.example.amulet.feature.devices.presentation.pairing.components.QrScannerView

@Composable
fun PairingQrScanScreen(
    viewModel: PairingViewModel = hiltViewModel(),
    onQrScanned: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var hasScanned by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // QR Scanner в полный экран
        QrScannerView(
            qrScanManager = viewModel.qrScanManager,
            onQrScanned = { qrContent ->
                if (!hasScanned) {
                    hasScanned = true
                    viewModel.onQrScanned(qrContent)
                    onQrScanned()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Темный оверлей с рамкой
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя часть - инструкция
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pairing_qr_scan_instruction_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.pairing_qr_scan_instruction_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Кнопка закрытия
            FilledTonalButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_cancel))
            }
        }
    }
}
