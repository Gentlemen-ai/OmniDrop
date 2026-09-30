package com.example.data.model

data class NearbyPeer(
    val id: String,
    val name: String,
    val platform: DeviceType,
    val ipAddress: String,
    val port: Int = 8080,
    val signalStrengthPercent: Int = 90,
    val isVerified: Boolean = false,
    val verificationSafetyNumber: String = "4829-1094-8831",
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
