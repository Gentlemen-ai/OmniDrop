package com.example.data.model

enum class TransferStatus {
    QUEUED,
    TRANSFERRING,
    COMPLETED,
    FAILED,
    PAUSED
}

enum class TransferDirection {
    SENDING,
    RECEIVING
}

data class TransferItem(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val bytesTransferred: Long = 0,
    val speedBytesPerSec: Long = 0,
    val status: TransferStatus = TransferStatus.QUEUED,
    val direction: TransferDirection = TransferDirection.SENDING,
    val targetPlatform: DeviceType = DeviceType.IOS,
    val targetDeviceName: String = "Unknown Peer",
    val isEncrypted: Boolean = true,
    val encryptionAlgorithm: String = "AES-256-GCM",
    val sha256Checksum: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val localUri: String? = null,
    val mimeType: String = "application/octet-stream",
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (fileSize > 0) (bytesTransferred.toFloat() / fileSize).coerceIn(0f, 1f) else 0f

    val formattedSize: String
        get() = formatByteSize(fileSize)

    val formattedTransferred: String
        get() = formatByteSize(bytesTransferred)

    val formattedSpeed: String
        get() = "${formatByteSize(speedBytesPerSec)}/s"

    companion object {
        fun formatByteSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val index = digitGroups.coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, index.toDouble())
            return String.format("%.1f %s", value, units[index])
        }
    }
}
