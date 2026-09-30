package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.GlassCyan
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

/**
 * CameraX Live Preview with ZXing ImageAnalysis Analyzer for QR Code detection.
 */
@Composable
fun QrCodeScannerView(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    boxSize: Dp = 250.dp
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("QrScanner", "Error unbinding camera", e)
            }
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(torchEnabled, camera) {
        try {
            camera?.cameraControl?.enableTorch(torchEnabled)
        } catch (e: Exception) {
            Log.e("QrScanner", "Failed to set torch", e)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser_line")
    val laserOffsetRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Camera Viewfinder (TextureView compatible mode to avoid SurfaceView BufferQueue abandoned errors)
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val reader = MultiFormatReader().apply {
                            val hints = mapOf(
                                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                                DecodeHintType.TRY_HARDER to true,
                                DecodeHintType.CHARACTER_SET to "UTF-8"
                            )
                            setHints(hints)
                        }

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (hasScanned) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            try {
                                val buffer = imageProxy.planes[0].buffer
                                val data = ByteArray(buffer.remaining())
                                buffer.get(data)
                                val width = imageProxy.width
                                val height = imageProxy.height

                                val source = PlanarYUVLuminanceSource(
                                    data, width, height, 0, 0, width, height, false
                                )
                                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                                val result = reader.decodeWithState(binaryBitmap)
                                val qrText = result?.text
                                if (!qrText.isNullOrBlank() && !hasScanned) {
                                    hasScanned = true
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        onQrCodeDetected(qrText)
                                    }
                                }
                            } catch (_: NotFoundException) {
                                // QR code not found in current frame
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Analysis error", e)
                            } finally {
                                reader.reset()
                                imageProxy.close()
                            }
                        }

                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QrScanner", "Failed to bind camera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Dark dim overlay with transparent cutout around targeting box
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxSizePx = boxSize.toPx()
            val left = (size.width - boxSizePx) / 2
            val top = (size.height - boxSizePx) / 2

            // Dim outer region
            drawRect(
                color = Color.Black.copy(alpha = 0.55f),
                size = size
            )

            // Draw glowing viewfinder targeting frame
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(GlassCyan, Color(0xFF8B5CF6), GlassCyan)
                ),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxSizePx, boxSizePx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
            )
        }

        // Animated Liquid Laser Scan Line
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = boxSize * laserOffsetRatio)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GlassCyan,
                                Color.White,
                                GlassCyan,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Utility to decode QR code from a gallery image Uri using ZXing.
 */
fun decodeQrFromImageUri(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        decodeQrFromBitmap(bitmap)
    } catch (e: Exception) {
        Log.e("QrDecoder", "Failed to decode QR from image Uri", e)
        null
    }
}

/**
 * Decodes a Bitmap using ZXing RGBLuminanceSource and HybridBinarizer.
 */
fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader().apply {
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            setHints(hints)
        }
        val result = reader.decodeWithState(binaryBitmap)
        reader.reset()
        result?.text
    } catch (e: Exception) {
        null
    }
}
