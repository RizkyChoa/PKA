package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookPeriodDao {
    @Query("SELECT * FROM book_periods ORDER BY startDate DESC, id DESC")
    fun getAllPeriods(): Flow<List<BookPeriodEntity>>

    @Query("SELECT * FROM book_periods ORDER BY startDate DESC, id DESC")
    suspend fun getAllPeriodsSnapshot(): List<BookPeriodEntity>

    @Query("SELECT * FROM book_periods WHERE status = 'ACTIVE' LIMIT 1")
    fun getActivePeriod(): Flow<BookPeriodEntity?>

    @Query("SELECT * FROM book_periods WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActivePeriodSnapshot(): BookPeriodEntity?

    @Query("SELECT * FROM book_periods WHERE id = :id")
    suspend fun getPeriodById(id: Long): BookPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: BookPeriodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(periods: List<BookPeriodEntity>)

    @Update
    suspend fun updatePeriod(period: BookPeriodEntity)

    @Query("UPDATE book_periods SET status = 'ARCHIVED', closedAt = :closedAt WHERE status = 'ACTIVE'")
    suspend fun archiveAllActivePeriods(closedAt: Long = System.currentTimeMillis())

    @Query("UPDATE book_periods SET name = :newName WHERE id = :id")
    suspend fun updatePeriodName(id: Long, newName: String)

    @Query("UPDATE book_periods SET status = :status WHERE id = :id")
    suspend fun updatePeriodStatus(id: Long, status: String)

    @Query("DELETE FROM book_periods WHERE id = :id")
    suspend fun deletePeriodById(id: Long)
}
