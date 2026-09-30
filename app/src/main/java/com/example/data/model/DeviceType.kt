package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AndroidColor
import com.example.ui.theme.AppleColor
import com.example.ui.theme.MacColor
import com.example.ui.theme.WindowsColor

enum class DeviceType(val displayName: String, val brandColor: Color) {
    ANDROID("Android", AndroidColor),
    IOS("iOS (iPhone/iPad)", AppleColor),
    MACOS("Mac (macOS)", MacColor),
    WINDOWS("Windows PC", WindowsColor),
    LINUX("Linux", Color(0xFFF59E0B));

    fun getIconDescription(): String = when (this) {
        ANDROID -> "Android Device"
        IOS -> "Apple iPhone or iPad"
        MACOS -> "Apple Mac or MacBook"
        WINDOWS -> "Windows PC or Surface"
        LINUX -> "Linux Workstation"
    }
}
