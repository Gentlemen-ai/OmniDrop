package com.example.data.model

enum class CloudSyncStatus {
    SYNCED,
    UPLOADING,
    DOWNLOADING,
    PENDING,
    CONFLICT
}

data class CloudSyncItem(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val lastModified: Long = System.currentTimeMillis(),
    val status: CloudSyncStatus = CloudSyncStatus.SYNCED,
    val isEncrypted: Boolean = true,
    val sha256Fingerprint: String = "",
    val uploadedByPlatform: DeviceType = DeviceType.ANDROID,
    val uploadedByDevice: String = "My Android",
    val remotePath: String = "/vault/files",
    val syncPassphraseHash: String = ""
) {
    val formattedSize: String
        get() = TransferItem.formatByteSize(fileSize)
}
