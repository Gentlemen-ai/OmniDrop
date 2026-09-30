package com.example.data.preferences

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val deviceName: String = "OmniDrop Device",
    val autoAcceptTrusted: Boolean = true,
    val e2eeStrictVerification: Boolean = true,
    val highSpeedDirect: Boolean = true,
    val webPortalPinEnabled: Boolean = false,
    val webPortalPin: String = "1234",
    val cloudAutoSync: Boolean = true,
    val hapticFeedback: Boolean = true
)
