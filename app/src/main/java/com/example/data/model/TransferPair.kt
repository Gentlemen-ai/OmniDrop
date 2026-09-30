package com.example.data.model

data class TransferPair(
    val id: String,
    val sourcePlatform: DeviceType,
    val targetPlatform: DeviceType,
    val title: String,
    val protocol: String,
    val maxSpeedDescription: String,
    val securityMethod: String,
    val zeroInstallSupported: Boolean,
    val shortSummary: String,
    val stepGuide: List<String>
) {
    companion object {
        fun getAllSupportedPairs(): List<TransferPair> = listOf(
            TransferPair(
                id = "ios_android",
                sourcePlatform = DeviceType.IOS,
                targetPlatform = DeviceType.ANDROID,
                title = "iOS ↔ Android",
                protocol = "WebDrop P2P & Local HTTP Stream",
                maxSpeedDescription = "Up to 60 MB/s (Local 5GHz Wi-Fi)",
                securityMethod = "AES-256-GCM + 6-digit Session PIN",
                zeroInstallSupported = true,
                shortSummary = "Zero-install wireless link between iPhone/iPad and Android without AirDrop limits.",
                stepGuide = listOf(
                    "Connect both iPhone and Android to the same Wi-Fi network or Android Personal Hotspot.",
                    "Open the Camera app on your iPhone and scan the OmniDrop QR code on Android.",
                    "Tap the popup link to open the high-speed WebDrop portal in Safari.",
                    "Select photos or files on iOS to stream directly to Android, or tap files on screen to download instantly."
                )
            ),
            TransferPair(
                id = "mac_android",
                sourcePlatform = DeviceType.MACOS,
                targetPlatform = DeviceType.ANDROID,
                title = "Mac ↔ Android",
                protocol = "LAN High-Speed Stream & Web Portal",
                maxSpeedDescription = "Up to 95 MB/s (Local Wi-Fi 6 / Hotspot)",
                securityMethod = "AES-256-GCM + Peer Key Verification",
                zeroInstallSupported = true,
                shortSummary = "Seamless Mac Finder and browser drag-and-drop directly into Android storage.",
                stepGuide = listOf(
                    "Ensure Mac and Android share the same network (Wi-Fi or Android Portable Hotspot).",
                    "On your Mac, open Safari, Chrome, or Brave and navigate to the OmniDrop LAN URL (e.g., http://192.168.1.X:8080).",
                    "Drag & drop whole folders, 4K videos, or zip archives from Mac Finder into the browser window.",
                    "Files are transmitted with end-to-end checksum verification directly into your Android downloads folder."
                )
            ),
            TransferPair(
                id = "mac_ios",
                sourcePlatform = DeviceType.MACOS,
                targetPlatform = DeviceType.IOS,
                title = "Mac ↔ iOS",
                protocol = "E2EE Cloud Sync Relay & Web Tunnel",
                maxSpeedDescription = "Up to 80 MB/s (Direct Wi-Fi / Relay)",
                securityMethod = "Client-Side AES-256 + End-to-End Vault Key",
                zeroInstallSupported = true,
                shortSummary = "Encrypted bridge to sync files between Apple devices with zero iCloud storage limits.",
                stepGuide = listOf(
                    "Open the OmniDrop Cloud Sync Locker or local Web Bridge.",
                    "Both devices connect using the same private end-to-end encryption key or pairing code.",
                    "Files uploaded from Mac are automatically mirrored and decrypted on iOS in real time.",
                    "No AirDrop Bluetooth handshake failure or iCloud quota constraints."
                )
            ),
            TransferPair(
                id = "ios_windows",
                sourcePlatform = DeviceType.IOS,
                targetPlatform = DeviceType.WINDOWS,
                title = "iOS ↔ Windows",
                protocol = "Direct Browser Drop & Cloud Vault",
                maxSpeedDescription = "Up to 70 MB/s (Gigabit LAN / Wi-Fi)",
                securityMethod = "AES-256-GCM + Ephemeral Session Tokens",
                zeroInstallSupported = true,
                shortSummary = "No iTunes or cables required. Direct high-speed file transfer between iPhone and Windows PC.",
                stepGuide = listOf(
                    "Open Microsoft Edge or Chrome on your Windows 10/11 PC.",
                    "Navigate to the OmniDrop local address or shared Cloud Sync room.",
                    "On iPhone, open Safari or scan the transfer QR code displayed on Windows.",
                    "Instantly send photos, Live Photos, raw camera files, and docs directly to your PC downloads."
                )
            ),
            TransferPair(
                id = "windows_android",
                sourcePlatform = DeviceType.WINDOWS,
                targetPlatform = DeviceType.ANDROID,
                title = "Windows ↔ Android",
                protocol = "Ultra-Fast LAN Socket & WebDrop",
                maxSpeedDescription = "Up to 110 MB/s (Gigabit Wi-Fi 6 / Hotspot)",
                securityMethod = "AES-256-GCM Hardware Accelerated",
                zeroInstallSupported = true,
                shortSummary = "High-throughput wireless link replacing USB cables between Windows PC and Android.",
                stepGuide = listOf(
                    "Open the OmniDrop Web Portal on Android and verify your local IP.",
                    "On Windows, open browser or link via OmniDrop companion URL.",
                    "Select or drop any files: PC games, ROMs, movies, APKs, or documents.",
                    "Enjoy wire-speed transfers with live throughput meter and SHA-256 verification."
                )
            ),
            TransferPair(
                id = "mac_windows",
                sourcePlatform = DeviceType.MACOS,
                targetPlatform = DeviceType.WINDOWS,
                title = "Mac ↔ Windows",
                protocol = "Direct Wi-Fi P2P & WebDrop Relay",
                maxSpeedDescription = "Up to 105 MB/s (Local LAN / Wi-Fi 6)",
                securityMethod = "Hardware AES-256-GCM + Ephemeral Pairing Key",
                zeroInstallSupported = true,
                shortSummary = "Instant wireless bridge between Apple macOS and Microsoft Windows without thumb drives or cloud size caps.",
                stepGuide = listOf(
                    "Ensure both your Mac and Windows PC are connected to the same local Wi-Fi or mobile hotspot.",
                    "On Windows PC, open browser (Edge/Chrome) or scan the QR code to open the OmniDrop peer portal.",
                    "On your Mac, open the web drop dashboard or companion bridge in Safari or Chrome.",
                    "Drag & drop massive folders, ProRes video files, or zip archives from Mac Finder directly to your Windows desktop with end-to-end encryption."
                )
            )
        )
    }
}
