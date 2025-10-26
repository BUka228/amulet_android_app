package com.example.amulet.feature.devices.presentation.pairing.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.scanner.QrScanManager
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable для отображения QR сканера.
 * 
 * Использует QrScanManager для работы с камерой и распознавания QR кодов.
 * Автоматически запрашивает разрешения камеры при первом отображении.
 */
@Composable
fun QrScannerView(
    qrScanManager: QrScanManager,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Состояние разрешения камеры
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Launcher для запроса разрешения
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Автоматический запрос разрешения при первом открытии
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            hasCameraPermission -> {
                // Камера доступна - показываем preview
                var previewView: PreviewView? by remember { mutableStateOf(null) }
                
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { preview ->
                            previewView = preview
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Запуск сканирования
                LaunchedEffect(previewView) {
                    previewView?.let { preview ->
                        qrScanManager.startScanning(preview, lifecycleOwner)
                            .collectLatest { qrData ->
                                onQrScanned(qrData)
                            }
                    }
                }
                
                // Overlay с подсказкой
                QrScannerOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Разрешение не предоставлено
                CameraPermissionDenied(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun QrScannerOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Рамка сканирования
            Surface(
                modifier = Modifier
                    .size(250.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {}
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.pairing_qr_scan_instruction_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CameraPermissionDenied(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Иконка камеры
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.pairing_camera_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stringResource(R.string.pairing_camera_permission_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.pairing_camera_permission_button),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
