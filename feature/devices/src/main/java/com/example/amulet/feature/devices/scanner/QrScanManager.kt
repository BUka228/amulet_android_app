package com.example.amulet.feature.devices.scanner

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Менеджер для сканирования QR-кодов через камеру.
 * 
 * Использует CameraX + ML Kit Barcode Scanning для надежного распознавания.
 * Lifecycle-aware, автоматически управляет ресурсами камеры.
 */
class QrScanManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val barcodeScanner = BarcodeScanning.getClient()
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    
    /**
     * Запускает сканирование QR кодов.
     * 
     * @param previewView View для отображения превью камеры
     * @param lifecycleOwner Lifecycle владелец (обычно Fragment/Activity)
     * @return Flow с отсканированными QR данными
     */
    fun startScanning(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ): Flow<String> = callbackFlow {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Image analysis use case для ML Kit
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy) { qrData ->
                                trySend(qrData)
                            }
                        }
                    }
                
                // Camera selector - задняя камера
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Unbind all use cases перед rebinding
                cameraProvider?.unbindAll()
                
                // Bind use cases к lifecycle
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                close(e)
            }
        }, ContextCompat.getMainExecutor(context))
        
        awaitClose {
            cameraProvider?.unbindAll()
        }
    }
    
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy,
        onQrDetected: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.format == Barcode.FORMAT_QR_CODE) {
                            barcode.rawValue?.let { qrData ->
                                onQrDetected(qrData)
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    /**
     * Освобождение ресурсов.
     * Вызывать при завершении работы с менеджером.
     */
    fun release() {
        barcodeScanner.close()
        cameraExecutor.shutdown()
    }
}
