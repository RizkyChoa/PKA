package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReceivableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivableDao {
    @Query("SELECT * FROM receivables ORDER BY dueDate ASC, id DESC")
    fun getAllReceivables(): Flow<List<ReceivableEntity>>

    @Query("SELECT * FROM receivables")
    suspend fun getAllReceivablesSnapshot(): List<ReceivableEntity>

    @Query("SELECT * FROM receivables WHERE status != 'PAID' ORDER BY dueDate ASC")
    fun getUnpaidReceivables(): Flow<List<ReceivableEntity>>

    @Query("SELECT * FROM receivables WHERE id = :id")
    suspend fun getReceivableById(id: Long): ReceivableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivable(receivable: ReceivableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receivables: List<ReceivableEntity>)

    @Update
    suspend fun updateReceivable(receivable: ReceivableEntity)

    @Query("DELETE FROM receivables WHERE id = :id")
    suspend fun deleteReceivableById(id: Long)
}
