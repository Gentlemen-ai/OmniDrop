package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    @Update
    suspend fun updateTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteTransferById(id: String)

    @Query("DELETE FROM transfers")
    suspend fun clearAllTransfers()

    @Query("SELECT * FROM cloud_sync ORDER BY lastModified DESC")
    fun getAllCloudSyncItems(): Flow<List<CloudSyncEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudSyncItem(item: CloudSyncEntity)

    @Query("DELETE FROM cloud_sync WHERE id = :id")
    suspend fun deleteCloudSyncItem(id: String)
}
