package com.example.network

import com.example.crypto.CryptoEngine
import com.example.data.local.TransferRepository
import com.example.data.model.CloudSyncItem
import com.example.data.model.CloudSyncStatus
import com.example.data.model.DeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CloudSyncEngine(
    private val repository: TransferRepository,
    private val scope: CoroutineScope
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis() - 1000 * 60 * 12)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _vaultPassphrase = MutableStateFlow("omni-secure-e2ee-vault-2026")
    val vaultPassphrase: StateFlow<String> = _vaultPassphrase.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    init {
        // Seed default cloud sync items if repository is empty
        scope.launch {
            seedDefaultItems()
        }
    }

    private suspend fun seedDefaultItems() {
        val sampleItems = listOf(
            CloudSyncItem(
                id = "cloud-1",
                fileName = "Project_Pitch_Deck_Q3.key",
                fileSize = 18_450_000,
                lastModified = System.currentTimeMillis() - 1000 * 60 * 45,
                status = CloudSyncStatus.SYNCED,
                isEncrypted = true,
                sha256Fingerprint = "7a8f3b0e2c1d99a4e5f6...",
                uploadedByPlatform = DeviceType.MACOS,
                uploadedByDevice = "MacBook Pro M3",
                remotePath = "/vault/Presentations"
            ),
            CloudSyncItem(
                id = "cloud-2",
                fileName = "Security_Audit_Report.pdf",
                fileSize = 4_230_000,
                lastModified = System.currentTimeMillis() - 1000 * 60 * 180,
                status = CloudSyncStatus.SYNCED,
                isEncrypted = true,
                sha256Fingerprint = "1d4b6a9c8f0e2213ab45...",
                uploadedByPlatform = DeviceType.WINDOWS,
                uploadedByDevice = "Windows Workstation",
                remotePath = "/vault/Audits"
            ),
            CloudSyncItem(
                id = "cloud-3",
                fileName = "IMG_4901_Raw_Photo.dng",
                fileSize = 34_800_000,
                lastModified = System.currentTimeMillis() - 1000 * 60 * 320,
                status = CloudSyncStatus.SYNCED,
                isEncrypted = true,
                sha256Fingerprint = "4e5f6a7b8c9d0e1f2a3b...",
                uploadedByPlatform = DeviceType.IOS,
                uploadedByDevice = "Elena's iPhone 15 Pro",
                remotePath = "/vault/Photos"
            )
        )

        for (item in sampleItems) {
            repository.saveCloudSyncItem(item)
        }
    }

    fun setAutoSync(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
    }

    fun setVaultPassphrase(passphrase: String) {
        _vaultPassphrase.value = passphrase
    }

    fun syncNow(onComplete: () -> Unit = {}) {
        if (_isSyncing.value) return
        _isSyncing.value = true
        scope.launch {
            // Emulate secure end-to-end handshake with cloud sync cluster
            delay(2000)
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false
            onComplete()
        }
    }

    fun addFileToCloud(fileName: String, fileSize: Long, sourcePlatform: DeviceType = DeviceType.ANDROID) {
        scope.launch {
            val newItem = CloudSyncItem(
                id = UUID.randomUUID().toString(),
                fileName = fileName,
                fileSize = fileSize,
                lastModified = System.currentTimeMillis(),
                status = CloudSyncStatus.SYNCED,
                isEncrypted = true,
                sha256Fingerprint = CryptoEngine.computeSha256(fileName.toByteArray()).take(20) + "...",
                uploadedByPlatform = sourcePlatform,
                uploadedByDevice = "My Android Phone",
                remotePath = "/vault/Uploads"
            )
            repository.saveCloudSyncItem(newItem)
            _lastSyncTime.value = System.currentTimeMillis()
        }
    }
}
