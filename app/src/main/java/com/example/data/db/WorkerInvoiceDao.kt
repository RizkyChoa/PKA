package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerInvoiceDao {
    @Query("SELECT * FROM worker_invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<WorkerInvoiceEntity>>

    @Query("SELECT * FROM worker_invoices WHERE projectId = :projectId ORDER BY id DESC")
    fun getInvoicesByProject(projectId: Long): Flow<List<WorkerInvoiceEntity>>

    @Query("SELECT * FROM worker_invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): WorkerInvoiceEntity?

    @Query("SELECT COUNT(*) FROM worker_invoices")
    suspend fun getInvoiceCount(): Int

    @Query("SELECT * FROM worker_job_items WHERE invoiceId = :invoiceId")
    suspend fun getJobItemsForInvoice(invoiceId: Long): List<WorkerJobItemEntity>

    @Query("SELECT * FROM worker_loan_items WHERE invoiceId = :invoiceId")
    suspend fun getLoanItemsForInvoice(invoiceId: Long): List<WorkerLoanItemEntity>

    @Query("SELECT * FROM worker_job_items")
    fun getAllJobItems(): Flow<List<WorkerJobItemEntity>>

    @Query("SELECT * FROM worker_loan_items")
    fun getAllLoanItems(): Flow<List<WorkerLoanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: WorkerInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvoices(invoices: List<WorkerInvoiceEntity>)

    @Query("SELECT * FROM worker_invoices")
    suspend fun getAllInvoicesSnapshot(): List<WorkerInvoiceEntity>

    @Query("SELECT * FROM worker_job_items")
    suspend fun getAllJobItemsSnapshot(): List<WorkerJobItemEntity>

    @Query("SELECT * FROM worker_loan_items")
    suspend fun getAllLoanItemsSnapshot(): List<WorkerLoanItemEntity>

    @Update
    suspend fun updateInvoice(invoice: WorkerInvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItems(items: List<WorkerJobItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItem(item: WorkerJobItemEntity): Long

    @Update
    suspend fun updateJobItem(item: WorkerJobItemEntity)

    @Query("DELETE FROM worker_job_items WHERE id = :id")
    suspend fun deleteJobItemById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanItems(items: List<WorkerLoanItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanItem(item: WorkerLoanItemEntity): Long

    @Update
    suspend fun updateLoanItem(item: WorkerLoanItemEntity)

    @Query("DELETE FROM worker_loan_items WHERE id = :id")
    suspend fun deleteLoanItemById(id: Long)

    @Query("DELETE FROM worker_invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("DELETE FROM worker_job_items WHERE invoiceId = :invoiceId")
    suspend fun deleteJobItems(invoiceId: Long)

    @Query("DELETE FROM worker_loan_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLoanItems(invoiceId: Long)
}
