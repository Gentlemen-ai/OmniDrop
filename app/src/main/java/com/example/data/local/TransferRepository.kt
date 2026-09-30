package com.example.data.local

import com.example.data.model.CloudSyncItem
import com.example.data.model.CloudSyncStatus
import com.example.data.model.DeviceType
import com.example.data.model.TransferDirection
import com.example.data.model.TransferItem
import com.example.data.model.TransferStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransferRepository(private val dao: TransferDao) {

    val transferHistory: Flow<List<TransferItem>> = dao.getAllTransfers().map { list ->
        list.map { entity ->
            TransferItem(
                id = entity.id,
                fileName = entity.fileName,
                fileSize = entity.fileSize,
                bytesTransferred = entity.bytesTransferred,
                status = try { TransferStatus.valueOf(entity.status) } catch (e: Exception) { TransferStatus.COMPLETED },
                direction = try { TransferDirection.valueOf(entity.direction) } catch (e: Exception) { TransferDirection.SENDING },
                targetPlatform = try { DeviceType.valueOf(entity.targetPlatform) } catch (e: Exception) { DeviceType.IOS },
                targetDeviceName = entity.targetDeviceName,
                isEncrypted = entity.isEncrypted,
                sha256Checksum = entity.sha256Checksum,
                timestamp = entity.timestamp,
                localUri = entity.localUri
            )
        }
    }

    val cloudSyncItems: Flow<List<CloudSyncItem>> = dao.getAllCloudSyncItems().map { list ->
        list.map { entity ->
            CloudSyncItem(
                id = entity.id,
                fileName = entity.fileName,
                fileSize = entity.fileSize,
                lastModified = entity.lastModified,
                status = try { CloudSyncStatus.valueOf(entity.status) } catch (e: Exception) { CloudSyncStatus.SYNCED },
                isEncrypted = entity.isEncrypted,
                sha256Fingerprint = entity.sha256Fingerprint,
                uploadedByPlatform = try { DeviceType.valueOf(entity.uploadedByPlatform) } catch (e: Exception) { DeviceType.ANDROID },
                uploadedByDevice = entity.uploadedByDevice,
                remotePath = entity.remotePath
            )
        }
    }

    suspend fun recordTransfer(item: TransferItem) {
        val entity = TransferEntity(
            id = item.id,
            fileName = item.fileName,
            fileSize = item.fileSize,
            bytesTransferred = item.bytesTransferred,
            status = item.status.name,
            direction = item.direction.name,
            targetPlatform = item.targetPlatform.name,
            targetDeviceName = item.targetDeviceName,
            isEncrypted = item.isEncrypted,
            sha256Checksum = item.sha256Checksum,
            timestamp = item.timestamp,
            localUri = item.localUri
        )
        dao.insertTransfer(entity)
    }

    suspend fun saveCloudSyncItem(item: CloudSyncItem) {
        val entity = CloudSyncEntity(
            id = item.id,
            fileName = item.fileName,
            fileSize = item.fileSize,
            lastModified = item.lastModified,
            status = item.status.name,
            isEncrypted = item.isEncrypted,
            sha256Fingerprint = item.sha256Fingerprint,
            uploadedByPlatform = item.uploadedByPlatform.name,
            uploadedByDevice = item.uploadedByDevice,
            remotePath = item.remotePath
        )
        dao.insertCloudSyncItem(entity)
    }

    suspend fun deleteTransfer(id: String) = dao.deleteTransferById(id)

    suspend fun clearHistory() = dao.clearAllTransfers()

    suspend fun deleteCloudItem(id: String) = dao.deleteCloudSyncItem(id)
}
