package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ThemeMode
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.CrossPlatformMatrixScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.QrScannerDialog
import com.example.ui.screens.SecurityDialog
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TransferScreen
import com.example.ui.screens.WebPortalScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecurity
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassEmerald
import com.example.ui.theme.GlassViolet
import com.example.ui.theme.LiquidGlassDock
import com.example.ui.theme.LiquidGlassMeshBackground
import com.example.ui.theme.LiquidGlassNavItem
import com.example.ui.theme.LiquidGlassPillBadge
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsState()
            val isDarkTheme = when (userPreferences.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(themeMode = userPreferences.themeMode) {
                var selectedTab by remember { mutableIntStateOf(0) }
                var showSecurityDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }
                var showQrScannerDialog by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }

                val userNotice by viewModel.userNotice.collectAsState()
                val activeTransfers by viewModel.activeTransfers.collectAsState()

                LaunchedEffect(userNotice) {
                    userNotice?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearNotice()
                    }
                }

                if (showSecurityDialog) {
                    SecurityDialog(
                        safetyNumber = "4829-1094-8831",
                        onDismiss = { showSecurityDialog = false }
                    )
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { showSettingsDialog = false }
                    )
                }

                if (showQrScannerDialog) {
                    QrScannerDialog(
                        viewModel = viewModel,
                        onDismiss = { showQrScannerDialog = false },
                        onPairSuccess = { peer ->
                            showQrScannerDialog = false
                            selectedTab = 0
                        }
                    )
                }

                LiquidGlassMeshBackground(isDark = isDarkTheme) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(GlassCyan.copy(alpha = 0.2f))
                                                .border(1.dp, GlassCyan.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = GlassCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "OmniDrop",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        LiquidGlassPillBadge(numberText = "E2EE", accentColor = GlassEmerald)
                                    }
                                },
                                actions = {
                                    // QR Scanner Button in Top Bar
                                    IconButton(
                                        onClick = { showQrScannerDialog = true },
                                        modifier = Modifier.testTag("open_qr_scanner_dialog")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Scan QR Code to Pair",
                                            tint = GlassCyan
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleThemeMode() },
                                        modifier = Modifier.testTag("quick_theme_toggle_button")
                                    ) {
                                        val (icon, tint, desc) = when (userPreferences.themeMode) {
                                            ThemeMode.LIGHT -> Triple(Icons.Default.LightMode, Color(0xFFD97706), "Light Mode")
                                            ThemeMode.DARK -> Triple(Icons.Default.DarkMode, GlassCyan, "Dark Mode")
                                            ThemeMode.SYSTEM -> Triple(Icons.Default.BrightnessAuto, GlassEmerald, "System Mode")
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "Current Theme: $desc. Tap to toggle.",
                                            tint = tint
                                        )
                                    }

                                    IconButton(
                                        onClick = { showSecurityDialog = true },
                                        modifier = Modifier.testTag("open_security_dialog")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Security Details",
                                            tint = GlassEmerald
                                        )
                                    }

                                    IconButton(
                                        onClick = { showSettingsDialog = true },
                                        modifier = Modifier.testTag("open_settings_dialog")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Preferences and Settings",
                                            tint = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = if (isDarkTheme) Color(0x660B1226) else Color(0x99FFFFFF)
                                )
                            )
                        },
                        bottomBar = {
                            val navItems = listOf(
                                LiquidGlassNavItem(0, Icons.Default.Send, "Transfer", activeTransfers.size),
                                LiquidGlassNavItem(1, Icons.Default.QrCode, "WebDrop"),
                                LiquidGlassNavItem(2, Icons.Default.SyncAlt, "6 OS Pairs"),
                                LiquidGlassNavItem(3, Icons.Default.Cloud, "Cloud"),
                                LiquidGlassNavItem(4, Icons.Default.History, "History")
                            )

                            LiquidGlassDock(
                                items = navItems,
                                selectedIndex = selectedTab,
                                onItemSelected = { selectedTab = it },
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("main_navigation_bar"),
                                isDark = isDarkTheme
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> TransferScreen(
                                    viewModel = viewModel,
                                    onNavigateToWebPortal = { selectedTab = 1 },
                                    onVerifySecurity = { showSecurityDialog = true },
                                    onOpenQrScanner = { showQrScannerDialog = true }
                                )
                                1 -> WebPortalScreen(viewModel = viewModel)
                                2 -> CrossPlatformMatrixScreen(
                                    viewModel = viewModel,
                                    onNavigateToWebPortal = { selectedTab = 1 }
                                )
                                3 -> CloudSyncScreen(viewModel = viewModel)
                                4 -> HistoryScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
