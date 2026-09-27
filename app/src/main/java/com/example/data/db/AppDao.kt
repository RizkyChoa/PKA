package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashAccountEntity
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // === PROJECTS ===
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    // === TRANSACTIONS (Single Source of Truth) ===
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY date DESC, id DESC")
    fun getTransactionsByProject(projectId: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    // === CASH ACCOUNTS ===
    @Query("SELECT * FROM cash_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<CashAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: CashAccountEntity): Long

    // === DEBT & RECEIVABLES ===
    @Query("SELECT * FROM debt_receivables ORDER BY id DESC")
    fun getAllDebtReceivables(): Flow<List<DebtReceivableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtReceivable(item: DebtReceivableEntity): Long

    @Update
    suspend fun updateDebtReceivable(item: DebtReceivableEntity)
}
