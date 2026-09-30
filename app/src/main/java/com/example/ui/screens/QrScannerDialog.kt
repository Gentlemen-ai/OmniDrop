package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.DeviceType
import com.example.data.model.NearbyPeer
import com.example.ui.components.PlatformBadge
import com.example.ui.components.QrCodeScannerView
import com.example.ui.components.decodeQrFromImageUri
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassEmerald
import com.example.ui.theme.GlassViolet
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassPillBadge
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QrScannerDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onPairSuccess: (NearbyPeer) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var torchEnabled by remember { mutableStateOf(false) }
    var manualInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Gallery image picker to decode QR screenshot
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val decoded = decodeQrFromImageUri(context, uri)
            if (decoded != null) {
                handleScannedContent(decoded, viewModel, onPairSuccess)
            } else {
                statusMessage = "No valid QR code found in selected photo."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xF0090E1B),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.5f),
                        GlassCyan.copy(alpha = 0.4f),
                        GlassViolet.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Liquid Glass Pill & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiquidGlassPillBadge(numberText = "SCAN", accentColor = GlassCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pair via QR Code",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "ZXing Hardware Scanner",
                                fontSize = 11.sp,
                                color = GlassCyan
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { torchEnabled = !torchEnabled },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (torchEnabled) GlassAmber else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Scan from Gallery",
                                tint = GlassViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scanner Viewport Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(GlassCyan, GlassViolet)),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        QrCodeScannerView(
                            onQrCodeDetected = { scannedText ->
                                handleScannedContent(scannedText, viewModel, onPairSuccess)
                            },
                            torchEnabled = torchEnabled,
                            boxSize = 210.dp
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = GlassCyan,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera Permission Required",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Camera is used to scan device pairing QR codes.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassCyan)
                            ) {
                                Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage!!,
                        fontSize = 12.sp,
                        color = GlassAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Liquid Glass Quick Simulator & Manual Pair Cards
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    accentGlow = GlassViolet
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "01", accentColor = GlassViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Simulation & Demo Peers",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Test instant pairing with Mac, iOS, or Windows with a single tap:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val macPeer = NearbyPeer(
                                        id = "peer_mac_m3",
                                        name = "MacBook Pro M3",
                                        platform = DeviceType.MACOS,
                                        ipAddress = "192.168.1.104",
                                        port = 8080,
                                        signalStrengthPercent = 98,
                                        isVerified = true,
                                        verificationSafetyNumber = "3194-5582-9012"
                                    )
                                    viewModel.peerDiscovery.addSimulatedPeer(macPeer)
                                    onPairSuccess(macPeer)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("MacBook", fontSize = 11.sp, color = GlassCyan, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val winPeer = NearbyPeer(
                                        id = "peer_win_11",
                                        name = "Windows 11 PC",
                                        platform = DeviceType.WINDOWS,
                                        ipAddress = "192.168.1.145",
                                        port = 8080,
                                        signalStrengthPercent = 94,
                                        isVerified = true,
                                        verificationSafetyNumber = "6023-8891-2341"
                                    )
                                    viewModel.peerDiscovery.addSimulatedPeer(winPeer)
                                    onPairSuccess(winPeer)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Windows PC", fontSize = 11.sp, color = GlassEmerald, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val iosPeer = NearbyPeer(
                                        id = "peer_iphone_16",
                                        name = "iPhone 16 Pro",
                                        platform = DeviceType.IOS,
                                        ipAddress = "192.168.1.188",
                                        port = 8080,
                                        signalStrengthPercent = 96,
                                        isVerified = true,
                                        verificationSafetyNumber = "7821-4902-1184"
                                    )
                                    viewModel.peerDiscovery.addSimulatedPeer(iosPeer)
                                    onPairSuccess(iosPeer)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("iPhone", fontSize = 11.sp, color = GlassViolet, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual IP Entry Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    accentGlow = GlassCyan
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "02", accentColor = GlassCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Manual IP / URL Connect",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualInput,
                                onValueChange = { manualInput = it },
                                placeholder = { Text("e.g. 192.168.1.5:8080", fontSize = 12.sp, color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_pair_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlassCyan,
                                    unfocusedBorderColor = Color(0x40FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (manualInput.isNotBlank()) {
                                        handleScannedContent(manualInput, viewModel, onPairSuccess)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Connect", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleScannedContent(
    content: String,
    viewModel: MainViewModel,
    onSuccess: (NearbyPeer) -> Unit
) {
    // Determine target platform & IP from QR content
    val clean = content.trim()
    val platform = when {
        clean.contains("mac", ignoreCase = true) || clean.contains("apple", ignoreCase = true) -> DeviceType.MACOS
        clean.contains("win", ignoreCase = true) || clean.contains("windows", ignoreCase = true) -> DeviceType.WINDOWS
        clean.contains("ios", ignoreCase = true) || clean.contains("iphone", ignoreCase = true) || clean.contains("ipad", ignoreCase = true) -> DeviceType.IOS
        else -> DeviceType.MACOS
    }

    val extractedIp = clean.substringAfter("http://")
        .substringBefore(":")
        .substringBefore("/")
        .ifBlank { "192.168.1.100" }

    val name = when (platform) {
        DeviceType.MACOS -> "MacBook Companion"
        DeviceType.WINDOWS -> "Windows PC Link"
        DeviceType.IOS -> "iPhone AirDrop Peer"
        DeviceType.ANDROID -> "Android Direct Peer"
        DeviceType.LINUX -> "Linux Workstation"
        else -> "Universal Web Peer"
    }

    val newPeer = NearbyPeer(
        id = "scanned_${System.currentTimeMillis()}",
        name = name,
        platform = platform,
        ipAddress = extractedIp,
        port = 8080,
        signalStrengthPercent = 99,
        isVerified = true,
        verificationSafetyNumber = "4810-9921-3564"
    )

    viewModel.peerDiscovery.addSimulatedPeer(newPeer)
    viewModel.showNotice("Paired with $name ($extractedIp) via QR code")
    onSuccess(newPeer)
}
