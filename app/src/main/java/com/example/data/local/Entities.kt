package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val fileSize: Long,
    val bytesTransferred: Long,
    val status: String,
    val direction: String,
    val targetPlatform: String,
    val targetDeviceName: String,
    val isEncrypted: Boolean,
    val sha256Checksum: String,
    val timestamp: Long,
    val localUri: String?,
    val durationMs: Long = 0
)

@Entity(tableName = "cloud_sync")
data class CloudSyncEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val fileSize: Long,
    val lastModified: Long,
    val status: String,
    val isEncrypted: Boolean,
    val sha256Fingerprint: String,
    val uploadedByPlatform: String,
    val uploadedByDevice: String,
    val remotePath: String
)
