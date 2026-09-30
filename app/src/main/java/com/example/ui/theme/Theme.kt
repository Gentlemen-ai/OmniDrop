package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.ThemeMode

private val OmniDropDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = CyanPrimaryHover,

    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = Color(0xFFDDD6FE),

    tertiary = EmeraldSecurity,
    onTertiary = Color.Black,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = Color(0xFFA7F3D0),

    background = DarkBackgroundConst,
    onBackground = TextPrimaryConst,

    surface = DarkSurfaceConst,
    onSurface = TextPrimaryConst,
    surfaceVariant = DarkSurfaceVariantConst,
    onSurfaceVariant = TextSecondaryConst,

    error = RoseError,
    onError = Color.White,
    errorContainer = RoseContainer,
    onErrorContainer = Color(0xFFFECDD3),

    outline = DarkSurfaceBorderConst,
    outlineVariant = Color(0xFF1E293B)
)

private val OmniDropLightColorScheme = lightColorScheme(
    primary = LightCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = LightCyanContainer,
    onPrimaryContainer = Color(0xFF155E75),

    secondary = LightVioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightVioletContainer,
    onSecondaryContainer = Color(0xFF5B21B6),

    tertiary = LightEmeraldSecurity,
    onTertiary = Color.White,
    tertiaryContainer = LightEmeraldContainer,
    onTertiaryContainer = Color(0xFF065F46),

    background = LightBackgroundConst,
    onBackground = LightTextPrimaryConst,

    surface = LightSurfaceConst,
    onSurface = LightTextPrimaryConst,
    surfaceVariant = LightSurfaceVariantConst,
    onSurfaceVariant = LightTextSecondaryConst,

    error = RoseError,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF9F1239),

    outline = LightSurfaceBorderConst,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> OmniDropDarkColorScheme
        else -> OmniDropLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = dynamicColor,
        content = content
    )
}
