package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProfitPartnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfitPartnerDao {
    @Query("SELECT * FROM profit_partners ORDER BY sharePercentage DESC, name ASC")
    fun getAllPartners(): Flow<List<ProfitPartnerEntity>>

    @Query("SELECT * FROM profit_partners ORDER BY sharePercentage DESC, name ASC")
    suspend fun getAllPartnersSnapshot(): List<ProfitPartnerEntity>

    @Query("SELECT * FROM profit_partners WHERE id = :id LIMIT 1")
    suspend fun getPartnerById(id: Long): ProfitPartnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: ProfitPartnerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(partners: List<ProfitPartnerEntity>)

    @Update
    suspend fun updatePartner(partner: ProfitPartnerEntity)

    @Query("DELETE FROM profit_partners WHERE id = :id")
    suspend fun deletePartnerById(id: Long)
}
