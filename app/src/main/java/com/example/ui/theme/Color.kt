package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Static Dark Palette
val DarkBackgroundConst = Color(0xFF090D16)
val DarkSurfaceConst = Color(0xFF101726)
val DarkSurfaceVariantConst = Color(0xFF172238)
val DarkSurfaceBorderConst = Color(0xFF243250)
val TextPrimaryConst = Color(0xFFF8FAFC)
val TextSecondaryConst = Color(0xFF94A3B8)
val TextTertiaryConst = Color(0xFF64748B)

// Static Light Palette (Crisp modern Slate aesthetic)
val LightBackgroundConst = Color(0xFFF8FAFC)
val LightSurfaceConst = Color(0xFFFFFFFF)
val LightSurfaceVariantConst = Color(0xFFF1F5F9)
val LightSurfaceBorderConst = Color(0xFFE2E8F0)
val LightTextPrimaryConst = Color(0xFF0F172A)
val LightTextSecondaryConst = Color(0xFF475569)
val LightTextTertiaryConst = Color(0xFF94A3B8)

// Theme-Aware Composable Color Accessors
// These dynamically resolve based on whether the app is in Light or Dark Mode!
val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val DarkSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val DarkSurfaceBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

// Clean Semantic Aliases
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val AppSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val AppSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val AppBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val AppTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val AppTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

// Brand Primary (Cyan)
val CyanPrimary = Color(0xFF06B6D4)
val CyanPrimaryHover = Color(0xFF22D3EE)
val CyanPrimaryContainer = Color(0xFF083344)
val LightCyanPrimary = Color(0xFF0891B2)
val LightCyanContainer = Color(0xFFE0F2FE)

// Brand Secondary (Violet)
val VioletSecondary = Color(0xFF8B5CF6)
val VioletContainer = Color(0xFF2E1065)
val LightVioletSecondary = Color(0xFF7C3AED)
val LightVioletContainer = Color(0xFFEDE9FE)

// Security & Status
val EmeraldSecurity = Color(0xFF10B981)
val EmeraldContainer = Color(0xFF064E3B)
val LightEmeraldSecurity = Color(0xFF059669)
val LightEmeraldContainer = Color(0xFFD1FAE5)

val AmberWarning = Color(0xFFF59E0B)
val AmberContainer = Color(0xFF451A03)

val RoseError = Color(0xFFF43F5E)
val RoseContainer = Color(0xFF4C0519)

// Platform brand colors
val AppleColor = Color(0xFFA2AAAD)
val WindowsColor = Color(0xFF0078D4)
val AndroidColor = Color(0xFF3DDC84)
val MacColor = Color(0xFFC084FC)
val CloudColor = Color(0xFF38BDF8)

