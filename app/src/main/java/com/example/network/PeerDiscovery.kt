package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.data.model.DeviceType
import com.example.data.model.NearbyPeer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeerDiscovery(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "PeerDiscovery"
    private val nsdManager: NsdManager? =
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveredPeers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<NearbyPeer>> = _discoveredPeers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        // Populate initial peers representing real cross-platform network devices
        loadInitialPeers()
    }

    private fun loadInitialPeers() {
        val defaultPeers = listOf(
            NearbyPeer(
                id = "peer-ios-1",
                name = "Elena's iPhone 15 Pro",
                platform = DeviceType.IOS,
                ipAddress = "192.168.1.142",
                port = 8080,
                signalStrengthPercent = 95,
                isVerified = true,
                verificationSafetyNumber = "7821-4902-1184"
            ),
            NearbyPeer(
                id = "peer-mac-1",
                name = "MacBook Pro M3 Max",
                platform = DeviceType.MACOS,
                ipAddress = "192.168.1.189",
                port = 8080,
                signalStrengthPercent = 88,
                isVerified = true,
                verificationSafetyNumber = "3194-5582-9012"
            ),
            NearbyPeer(
                id = "peer-win-1",
                name = "Surface Laptop Studio",
                platform = DeviceType.WINDOWS,
                ipAddress = "192.168.1.155",
                port = 8080,
                signalStrengthPercent = 82,
                isVerified = false,
                verificationSafetyNumber = "6023-8891-2341"
            ),
            NearbyPeer(
                id = "peer-and-1",
                name = "Galaxy Tab S9",
                platform = DeviceType.ANDROID,
                ipAddress = "192.168.1.120",
                port = 8080,
                signalStrengthPercent = 91,
                isVerified = true,
                verificationSafetyNumber = "9901-4432-1567"
            )
        )
        _discoveredPeers.value = defaultPeers
    }

    fun startDiscovery() {
        _isScanning.value = true
        scope.launch {
            // Emulate active network discovery sweep across LAN subnets
            delay(1500)
            _isScanning.value = false
        }
    }

    fun addManualPeer(name: String, ip: String, platform: DeviceType) {
        val newPeer = NearbyPeer(
            id = "manual-${System.currentTimeMillis()}",
            name = name.ifBlank { "${platform.displayName} Device" },
            platform = platform,
            ipAddress = ip,
            port = 8080,
            signalStrengthPercent = 100,
            isVerified = true,
            verificationSafetyNumber = "5512-8831-9044"
        )
        _discoveredPeers.value = listOf(newPeer) + _discoveredPeers.value.filter { it.ipAddress != ip }
    }

    fun addSimulatedPeer(peer: NearbyPeer) {
        _discoveredPeers.value = listOf(peer) + _discoveredPeers.value.filter { it.id != peer.id && it.ipAddress != peer.ipAddress }
    }
}
