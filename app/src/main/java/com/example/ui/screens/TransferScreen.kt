package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DeviceType
import com.example.data.model.NearbyPeer
import com.example.data.model.TransferItem
import com.example.ui.components.PlatformBadge
import com.example.ui.components.SecurityShieldCard
import com.example.ui.components.TransferProgressCard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSecurity
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassEmerald
import com.example.ui.theme.GlassPink
import com.example.ui.theme.GlassViolet
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassPillBadge
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TransferScreen(
    viewModel: MainViewModel,
    onNavigateToWebPortal: () -> Unit,
    onVerifySecurity: () -> Unit,
    onOpenQrScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stagedFiles by viewModel.webPortal.stagedFilesList.collectAsState()
    val activeTransfers by viewModel.activeTransfers.collectAsState()
    val nearbyPeers by viewModel.peerDiscovery.discoveredPeers.collectAsState()
    val isScanning by viewModel.peerDiscovery.isScanning.collectAsState()
    val localIp by viewModel.localIp.collectAsState()

    // Media Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.stageFileFromUri(uri)
        }
    }

    // Generic Document Picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.stageFileFromUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Liquid Glass Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                accentGlow = GlassCyan
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_cross_transfer),
                        contentDescription = "Cross-Platform Wireless Transfer",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xF2070B16))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "OmniDrop Wireless",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            LiquidGlassPillBadge(numberText = "LIQUID GLASS", accentColor = GlassCyan)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GlassEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Local IP: $localIp:8080 • E2EE Active",
                                fontSize = 12.sp,
                                color = GlassEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // QR Code Scanner Action Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                accentGlow = GlassCyan,
                onClick = onOpenQrScanner
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(listOf(GlassCyan.copy(alpha = 0.3f), GlassViolet.copy(alpha = 0.2f)))
                                )
                                .border(1.dp, GlassCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = GlassCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Scan QR Code to Pair",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(GlassCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ZXing", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = GlassCyan)
                                }
                            }
                            Text(
                                text = "Point camera at Mac, Windows, or iOS to link instantly",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenQrScanner,
                        colors = ButtonDefaults.buttonColors(containerColor = GlassCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("scan_qr_button")
                    ) {
                        Text("Scan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Mac <-> Windows Transfer Quick Action Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                accentGlow = GlassViolet
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "MAC ↔ WIN", accentColor = GlassViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mac & Windows Bridge",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("105 MB/s", fontSize = 10.sp, color = GlassEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Zero-install peer-to-peer bridge connecting Apple macOS Finder and Microsoft Windows Explorer without USB drives or iTunes.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val mac = NearbyPeer(
                                    id = "mac_link_pair",
                                    name = "MacBook Pro M3",
                                    platform = DeviceType.MACOS,
                                    ipAddress = "192.168.1.104",
                                    port = 8080,
                                    signalStrengthPercent = 98,
                                    isVerified = true,
                                    verificationSafetyNumber = "3194-5582-9012"
                                )
                                viewModel.peerDiscovery.addSimulatedPeer(mac)
                                viewModel.showNotice("MacBook Pro M3 ready for direct transfer")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LaptopMac, contentDescription = null, tint = GlassCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pair Mac", fontSize = 11.sp, color = GlassCyan, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val win = NearbyPeer(
                                    id = "win_link_pair",
                                    name = "Windows 11 PC",
                                    platform = DeviceType.WINDOWS,
                                    ipAddress = "192.168.1.145",
                                    port = 8080,
                                    signalStrengthPercent = 95,
                                    isVerified = true,
                                    verificationSafetyNumber = "6023-8891-2341"
                                )
                                viewModel.peerDiscovery.addSimulatedPeer(win)
                                viewModel.showNotice("Windows 11 PC ready for direct transfer")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = GlassEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pair Windows", fontSize = 11.sp, color = GlassEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Security Shield Status Card
        item {
            SecurityShieldCard(
                safetyNumber = "4829-1094-8831",
                onVerifyClick = onVerifySecurity
            )
        }

        // Active Transfers in progress
        if (activeTransfers.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassPillBadge(numberText = "LIVE", accentColor = GlassCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Wireless Transfers (${activeTransfers.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassCyan
                    )
                }
            }
            items(activeTransfers, key = { it.id }) { transfer ->
                TransferProgressCard(item = transfer)
            }
        }

        // Staged Files Ready to Send (Liquid Glass Card)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                accentGlow = GlassCyan
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "01", accentColor = GlassCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Staged Files (${stagedFiles.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Ready to transmit to Mac, Windows, iOS, or Android",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GlassCyan.copy(alpha = 0.18f))
                                    .testTag("pick_photos_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pick Photos/Videos",
                                    tint = GlassCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GlassViolet.copy(alpha = 0.18f))
                                    .testTag("pick_docs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilePresent,
                                    contentDescription = "Pick Documents",
                                    tint = GlassViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (stagedFiles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant.copy(alpha = 0.45f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No files selected yet",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        viewModel.stageDemoFile("Design_Specs_2026.pdf", 4_500_000, "application/pdf")
                                        viewModel.stageDemoFile("4K_Drone_Footage.mp4", 42_800_000, "video/mp4")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("stage_demo_button")
                                ) {
                                    Text("+ Add Demo Files", fontSize = 12.sp, color = GlassCyan)
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stagedFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurfaceVariant.copy(alpha = 0.8f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (file.mimeType.contains("video")) Icons.Default.Movie else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = GlassCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = file.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${TransferItem.formatByteSize(file.size)} • 🔒 AES-256",
                                                fontSize = 11.sp,
                                                color = GlassEmerald
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeStagedFile(file.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove file",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nearby Cross-Platform Devices Radar (Liquid Glass Card)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                accentGlow = GlassEmerald
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "02", accentColor = GlassEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nearby Peer Radar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.peerDiscovery.startDiscovery() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = GlassCyan
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh scan",
                                    tint = GlassCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Discovered Mac, Windows, iOS, and Android peers on local LAN",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        nearbyPeers.forEach { peer ->
                            PeerItemRow(
                                peer = peer,
                                hasStagedFiles = stagedFiles.isNotEmpty(),
                                onSendClick = {
                                    if (stagedFiles.isNotEmpty()) {
                                        stagedFiles.forEach { file ->
                                            viewModel.sendToPeer(peer, file)
                                        }
                                    } else {
                                        viewModel.showNotice("Please stage at least one file to send to ${peer.name}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Web Portal Quick Action Banner
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                accentGlow = GlassViolet,
                onClick = onNavigateToWebPortal
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassPillBadge(numberText = "03", accentColor = GlassViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WebDrop Browser Portal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Open on Safari, Chrome, or Edge on Mac/Windows with zero installs.",
                            fontSize = 11.sp,
                            color = Color(0xFFDDD6FE),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    ElevatedButton(
                        onClick = onNavigateToWebPortal,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = GlassViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open QR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerItemRow(
    peer: NearbyPeer,
    hasStagedFiles: Boolean,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.75f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(peer.platform.brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = peer.platform.brandColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = peer.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PlatformBadge(platform = peer.platform)
                }
                Text(
                    text = "${peer.ipAddress} • Signal ${peer.signalStrengthPercent}% • Verified",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Button(
            onClick = onSendClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasStagedFiles) GlassCyan else DarkSurfaceBorder,
                contentColor = if (hasStagedFiles) Color.Black else TextSecondary
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.testTag("send_peer_${peer.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Send", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
