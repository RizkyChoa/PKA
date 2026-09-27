package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PayableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayableDao {
    @Query("SELECT * FROM payables ORDER BY dueDate ASC, id DESC")
    fun getAllPayables(): Flow<List<PayableEntity>>

    @Query("SELECT * FROM payables")
    suspend fun getAllPayablesSnapshot(): List<PayableEntity>

    @Query("SELECT * FROM payables WHERE status != 'PAID' ORDER BY dueDate ASC")
    fun getUnpaidPayables(): Flow<List<PayableEntity>>

    @Query("SELECT * FROM payables WHERE id = :id")
    suspend fun getPayableById(id: Long): PayableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayable(payable: PayableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payables: List<PayableEntity>)

    @Update
    suspend fun updatePayable(payable: PayableEntity)
}
