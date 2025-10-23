package com.example.amulet.feature.devices.presentation.pairing.components

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.amulet.feature.devices.scanner.QrScanManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable для отображения QR сканера.
 * 
 * Использует QrScanManager для работы с камерой и распознавания QR кодов.
 * Автоматически запрашивает разрешения камеры.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerView(
    qrScanManager: QrScanManager,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            cameraPermissionState.status.isGranted -> {
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
                        cameraPermissionState.launchPermissionRequest()
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
                text = "Наведите камеру на QR-код",
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
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Необходим доступ к камере",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "Для сканирования QR-кода требуется разрешение на использование камеры",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onRequestPermission) {
            Text("Предоставить доступ")
        }
    }
}
