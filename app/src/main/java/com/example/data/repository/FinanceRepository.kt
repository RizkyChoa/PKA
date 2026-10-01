package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProfitPartnerEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(private val db: AppDatabase) {

    val allAccounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()
    val allProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()
    val activeProjects: Flow<List<ProjectEntity>> = db.projectDao().getActiveProjects()
    val allCategories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val validTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getValidTransactions()
    val allReceivables: Flow<List<ReceivableEntity>> = db.receivableDao().getAllReceivables()
    val allPayables: Flow<List<PayableEntity>> = db.payableDao().getAllPayables()
    val allProfitPartners: Flow<List<ProfitPartnerEntity>> = db.profitPartnerDao().getAllPartners()
    val auditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()

    suspend fun insertProfitPartner(partner: ProfitPartnerEntity): Long = withContext(Dispatchers.IO) {
        db.profitPartnerDao().insertPartner(partner)
    }

    suspend fun updateProfitPartner(partner: ProfitPartnerEntity) = withContext(Dispatchers.IO) {
        db.profitPartnerDao().updatePartner(partner)
    }

    suspend fun deleteProfitPartner(partnerId: Long) = withContext(Dispatchers.IO) {
        db.profitPartnerDao().deletePartnerById(partnerId)
    }

    suspend fun getProjectTransactions(projectId: Long): Flow<List<TransactionEntity>> {
        return db.transactionDao().getValidTransactionsByProject(projectId)
    }

    suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        db.projectDao().insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        db.projectDao().updateProject(project)
    }

    suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
        db.projectDao().deleteProjectById(projectId)
    }

    suspend fun updateProjectStatus(projectId: Long, newStatus: String) = withContext(Dispatchers.IO) {
        db.projectDao().updateProjectStatus(projectId, newStatus)
    }

    suspend fun insertAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        db.accountDao().insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        db.accountDao().updateAccount(account)
    }

    suspend fun deleteAccount(accountId: Long) = withContext(Dispatchers.IO) {
        db.accountDao().deleteAccountById(accountId)
    }

    suspend fun updateTransaction(newTrx: TransactionEntity) = withContext(Dispatchers.IO) {
        val oldTrx = db.transactionDao().getTransactionById(newTrx.id) ?: return@withContext
        if (oldTrx.status == "VALID") {
            // Revert old effect
            when (oldTrx.type) {
                "MONEY_IN" -> db.accountDao().adjustBalance(oldTrx.sourceAccountId, -oldTrx.amount)
                "MONEY_OUT" -> db.accountDao().adjustBalance(oldTrx.sourceAccountId, oldTrx.amount)
                "TRANSFER" -> {
                    db.accountDao().adjustBalance(oldTrx.sourceAccountId, oldTrx.amount)
                    oldTrx.destinationAccountId?.let { db.accountDao().adjustBalance(it, -oldTrx.amount) }
                }
            }
        }
        if (newTrx.status == "VALID") {
            // Apply new effect
            when (newTrx.type) {
                "MONEY_IN" -> db.accountDao().adjustBalance(newTrx.sourceAccountId, newTrx.amount)
                "MONEY_OUT" -> db.accountDao().adjustBalance(newTrx.sourceAccountId, -newTrx.amount)
                "TRANSFER" -> {
                    db.accountDao().adjustBalance(newTrx.sourceAccountId, -newTrx.amount)
                    newTrx.destinationAccountId?.let { db.accountDao().adjustBalance(it, newTrx.amount) }
                }
            }
        }
        db.transactionDao().updateTransaction(newTrx)
    }

    suspend fun deleteTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        val trx = db.transactionDao().getTransactionById(transactionId) ?: return@withContext
        if (trx.status == "VALID") {
            // Revert effect on account
            when (trx.type) {
                "MONEY_IN" -> db.accountDao().adjustBalance(trx.sourceAccountId, -trx.amount)
                "MONEY_OUT" -> db.accountDao().adjustBalance(trx.sourceAccountId, trx.amount)
                "TRANSFER" -> {
                    db.accountDao().adjustBalance(trx.sourceAccountId, trx.amount)
                    trx.destinationAccountId?.let { db.accountDao().adjustBalance(it, -trx.amount) }
                }
            }
        }
        db.transactionDao().deleteTransactionById(transactionId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.projectDao().deleteAllProjects()
        db.transactionDao().deleteAllTransactions()
        db.accountDao().deleteAllAccounts()
        // Fresh default account
        db.accountDao().insertAccount(AccountEntity(1, "Kas Utama (Tunai)", "CASH", "-", 0.0, 0.0, true))
    }

    suspend fun createTransaction(
        date: String,
        type: String, // "MONEY_IN", "MONEY_OUT", "TRANSFER"
        amount: Double,
        description: String,
        paymentMethod: String,
        sourceAccountId: Long,
        destinationAccountId: Long? = null,
        categoryId: Long,
        classification: String,
        projectId: Long? = null,
        receivableId: Long? = null,
        payableId: Long? = null,
        profitPartnerId: Long? = null,
        profitPartnerName: String? = null,
        costGroup: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val count = db.transactionDao().getTransactionCount() + 1
        val dateCompact = date.replace("-", "")
        val trxNumber = "TRX-$dateCompact-${String.format(Locale.US, "%04d", count)}"

        val sourceAccount = db.accountDao().getAccountById(sourceAccountId)
        val sourceAccountName = sourceAccount?.name ?: "Kas/Bank"
        val destinationAccount = destinationAccountId?.let { db.accountDao().getAccountById(it) }
        val destinationAccountName = destinationAccount?.name

        val project = projectId?.let { db.projectDao().getProjectById(it) }
        val projectName = project?.name

        val finalCostGroup = if (classification == "PROJECT") {
            costGroup.ifBlank { "OTHER" }
        } else ""

        // Adjust Account Balances
        when (type) {
            "MONEY_IN" -> {
                db.accountDao().adjustBalance(sourceAccountId, amount)
            }
            "MONEY_OUT" -> {
                db.accountDao().adjustBalance(sourceAccountId, -amount)
            }
            "TRANSFER" -> {
                db.accountDao().adjustBalance(sourceAccountId, -amount)
                destinationAccountId?.let { db.accountDao().adjustBalance(it, amount) }
            }
        }

        // If paying a receivable
        if (receivableId != null && type == "MONEY_IN") {
            val rec = db.receivableDao().getReceivableById(receivableId)
            if (rec != null) {
                val newPaid = rec.paidAmount + amount
                val newStatus = if (newPaid >= rec.totalAmount) "PAID" else "PARTIAL"
                db.receivableDao().updateReceivable(rec.copy(paidAmount = newPaid, status = newStatus))
            }
        }

        // If paying a payable
        if (payableId != null && type == "MONEY_OUT") {
            val pay = db.payableDao().getPayableById(payableId)
            if (pay != null) {
                val newPaid = pay.paidAmount + amount
                val newStatus = if (newPaid >= pay.totalAmount) "PAID" else "PARTIAL"
                db.payableDao().updatePayable(pay.copy(paidAmount = newPaid, status = newStatus))
            }
        }

        val transaction = TransactionEntity(
            trxNumber = trxNumber,
            date = date,
            type = type,
            amount = amount,
            description = description,
            paymentMethod = paymentMethod,
            sourceAccountId = sourceAccountId,
            sourceAccountName = sourceAccountName,
            destinationAccountId = destinationAccountId,
            destinationAccountName = destinationAccountName,
            categoryId = categoryId,
            categoryName = description.takeIf { it.isNotBlank() } ?: "Transaksi",
            classification = classification,
            projectId = projectId,
            projectName = projectName,
            costGroup = finalCostGroup,
            receivableId = receivableId,
            payableId = payableId,
            profitPartnerId = profitPartnerId,
            profitPartnerName = profitPartnerName
        )

        val insertedId = db.transactionDao().insertTransaction(transaction)

        // Log audit
        db.auditLogDao().insertLog(
            AuditLogEntity(
                transactionId = insertedId,
                trxNumber = trxNumber,
                action = "CREATE",
                details = "Transaksi dibuat: $type Rp${amount.toLong()} - $description"
            )
        )

        insertedId
    }

    suspend fun payPayablesBatch(
        payableIds: List<Long>,
        sourceAccountId: Long,
        date: String,
        paymentMethod: String = "TRANSFER",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        for (pId in payableIds) {
            val pay = db.payableDao().getPayableById(pId) ?: continue
            val remaining = pay.remainingAmount
            if (remaining <= 0) continue
            val desc = if (notes.isNotBlank()) "$notes (${pay.creditorName})" else "Pelunasan Utang: ${pay.creditorName} (${pay.description})"
            createTransaction(
                date = date,
                type = "MONEY_OUT",
                amount = remaining,
                description = desc,
                paymentMethod = paymentMethod,
                sourceAccountId = sourceAccountId,
                categoryId = 20L,
                classification = "DEBT",
                payableId = pId
            )
        }
    }

    suspend fun payReceivablesBatch(
        receivableIds: List<Long>,
        destinationAccountId: Long,
        date: String,
        paymentMethod: String = "TRANSFER",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        for (rId in receivableIds) {
            val rec = db.receivableDao().getReceivableById(rId) ?: continue
            val remaining = rec.remainingAmount
            if (remaining <= 0) continue
            val desc = if (notes.isNotBlank()) "$notes (${rec.clientName})" else "Penerimaan Piutang: ${rec.clientName} (${rec.description})"
            createTransaction(
                date = date,
                type = "MONEY_IN",
                amount = remaining,
                description = desc,
                paymentMethod = paymentMethod,
                sourceAccountId = destinationAccountId,
                categoryId = 3L,
                classification = "RECEIVABLE",
                receivableId = rId,
                projectId = rec.projectId
            )
        }
    }

    suspend fun receiveProjectPayment(
        projectId: Long,
        amount: Double,
        destinationAccountId: Long,
        date: String,
        paymentMethod: String = "TRANSFER",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val proj = db.projectDao().getProjectById(projectId) ?: return@withContext
        val desc = if (notes.isNotBlank()) notes else "Penerimaan Termin Proyek: ${proj.name} (${proj.clientName})"
        createTransaction(
            date = date,
            type = "MONEY_IN",
            amount = amount,
            description = desc,
            paymentMethod = paymentMethod,
            sourceAccountId = destinationAccountId,
            categoryId = 2L, // Pembayaran Termin Proyek
            classification = "PROJECT",
            projectId = projectId
        )
    }

    suspend fun voidTransaction(id: Long, reason: String) = withContext(Dispatchers.IO) {
        val trx = db.transactionDao().getTransactionById(id) ?: return@withContext
        if (trx.status == "VOID") return@withContext

        // Revert Balances
        when (trx.type) {
            "MONEY_IN" -> {
                db.accountDao().adjustBalance(trx.sourceAccountId, -trx.amount)
            }
            "MONEY_OUT" -> {
                db.accountDao().adjustBalance(trx.sourceAccountId, trx.amount)
            }
            "TRANSFER" -> {
                db.accountDao().adjustBalance(trx.sourceAccountId, trx.amount)
                trx.destinationAccountId?.let { db.accountDao().adjustBalance(it, -trx.amount) }
            }
        }

        // Revert Receivable if linked
        if (trx.receivableId != null && trx.type == "MONEY_IN") {
            val rec = db.receivableDao().getReceivableById(trx.receivableId)
            if (rec != null) {
                val newPaid = (rec.paidAmount - trx.amount).coerceAtLeast(0.0)
                val newStatus = if (newPaid == 0.0) "UNPAID" else if (newPaid >= rec.totalAmount) "PAID" else "PARTIAL"
                db.receivableDao().updateReceivable(rec.copy(paidAmount = newPaid, status = newStatus))
            }
        }

        // Revert Payable if linked
        if (trx.payableId != null && trx.type == "MONEY_OUT") {
            val pay = db.payableDao().getPayableById(trx.payableId)
            if (pay != null) {
                val newPaid = (pay.paidAmount - trx.amount).coerceAtLeast(0.0)
                val newStatus = if (newPaid == 0.0) "UNPAID" else if (newPaid >= pay.totalAmount) "PAID" else "PARTIAL"
                db.payableDao().updatePayable(pay.copy(paidAmount = newPaid, status = newStatus))
            }
        }

        db.transactionDao().voidTransaction(id, reason)

        db.auditLogDao().insertLog(
            AuditLogEntity(
                transactionId = id,
                trxNumber = trx.trxNumber,
                action = "VOID",
                details = "Dibatalkan (VOID) karena: $reason"
            )
        )
    }

    suspend fun insertReceivable(receivable: ReceivableEntity): Long = withContext(Dispatchers.IO) {
        db.receivableDao().insertReceivable(receivable)
    }

    suspend fun deleteReceivable(id: Long) = withContext(Dispatchers.IO) {
        db.receivableDao().deleteReceivableById(id)
    }

    suspend fun insertPayable(payable: PayableEntity): Long = withContext(Dispatchers.IO) {
        db.payableDao().insertPayable(payable)
    }

    suspend fun insertPayableWithTransaction(
        creditorName: String,
        type: String,
        description: String,
        totalAmount: Double,
        dueDate: String,
        destinationAccountId: Long,
        transactionDate: String = dueDate,
        paymentMethod: String = "TRANSFER"
    ): Long = withContext(Dispatchers.IO) {
        val payable = PayableEntity(
            creditorName = creditorName.trim(),
            type = type,
            description = description.trim(),
            totalAmount = totalAmount,
            paidAmount = 0.0,
            status = "UNPAID",
            dueDate = dueDate
        )
        val payableId = db.payableDao().insertPayable(payable)
        val trxDesc = if (description.isNotBlank()) "Penerimaan Pinjaman/Utang $creditorName - $description" else "Penerimaan Pinjaman/Utang dari $creditorName"
        createTransaction(
            date = transactionDate,
            type = "MONEY_IN",
            amount = totalAmount,
            description = trxDesc,
            paymentMethod = paymentMethod,
            sourceAccountId = destinationAccountId,
            destinationAccountId = null,
            categoryId = 19L, // Penerimaan Pinjaman Modal / Bank
            classification = "DEBT",
            payableId = payableId
        )
        payableId
    }

    suspend fun deletePayable(id: Long) = withContext(Dispatchers.IO) {
        db.payableDao().deletePayableById(id)
    }

    // Worker Invoices
    val allWorkerInvoices: Flow<List<com.example.data.model.WorkerInvoiceEntity>> = db.workerInvoiceDao().getAllInvoices()
    val allJobItems: Flow<List<com.example.data.model.WorkerJobItemEntity>> = db.workerInvoiceDao().getAllJobItems()
    val allLoanItems: Flow<List<com.example.data.model.WorkerLoanItemEntity>> = db.workerInvoiceDao().getAllLoanItems()

    suspend fun createWorkerInvoice(
        invoice: com.example.data.model.WorkerInvoiceEntity,
        jobs: List<com.example.data.model.WorkerJobItemEntity>,
        loans: List<com.example.data.model.WorkerLoanItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        val invoiceId = db.workerInvoiceDao().insertInvoice(invoice)
        val preparedJobs = jobs.map { it.copy(invoiceId = invoiceId) }
        val preparedLoans = loans.map { it.copy(invoiceId = invoiceId) }
        db.workerInvoiceDao().insertJobItems(preparedJobs)
        db.workerInvoiceDao().insertLoanItems(preparedLoans)
        invoiceId
    }

    suspend fun deleteWorkerInvoice(id: Long) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().deleteJobItems(id)
        db.workerInvoiceDao().deleteLoanItems(id)
        db.workerInvoiceDao().deleteInvoice(id)
    }

    suspend fun addJobItem(jobItem: com.example.data.model.WorkerJobItemEntity): Long = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().insertJobItem(jobItem)
    }

    suspend fun updateJobItem(jobItem: com.example.data.model.WorkerJobItemEntity) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().updateJobItem(jobItem)
    }

    suspend fun deleteJobItem(jobItemId: Long) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().deleteJobItemById(jobItemId)
    }

    suspend fun addLoanItem(loanItem: com.example.data.model.WorkerLoanItemEntity): Long = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().insertLoanItem(loanItem)
    }

    suspend fun updateLoanItem(loanItem: com.example.data.model.WorkerLoanItemEntity) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().updateLoanItem(loanItem)
    }

    suspend fun deleteLoanItem(loanItemId: Long) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().deleteLoanItemById(loanItemId)
    }

    suspend fun updateWorkerInvoice(invoice: com.example.data.model.WorkerInvoiceEntity) = withContext(Dispatchers.IO) {
        db.workerInvoiceDao().updateInvoice(invoice)
    }

    suspend fun exportAllData(): com.example.util.BackupData = withContext(Dispatchers.IO) {
        val projects = db.projectDao().getAllProjectsSnapshot()
        val accounts = db.accountDao().getAllAccountsSnapshot()
        val categories = db.categoryDao().getAllCategoriesSnapshot()
        val transactions = db.transactionDao().getAllTransactionsSnapshot()
        val receivables = db.receivableDao().getAllReceivablesSnapshot()
        val payables = db.payableDao().getAllPayablesSnapshot()
        val workerInvoices = db.workerInvoiceDao().getAllInvoicesSnapshot()
        val workerJobItems = db.workerInvoiceDao().getAllJobItemsSnapshot()
        val workerLoanItems = db.workerInvoiceDao().getAllLoanItemsSnapshot()

        com.example.util.BackupData(
            exportedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            projects = projects,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            receivables = receivables,
            payables = payables,
            workerInvoices = workerInvoices,
            workerJobItems = workerJobItems,
            workerLoanItems = workerLoanItems
        )
    }

    suspend fun importBackupData(data: com.example.util.BackupData) = withContext(Dispatchers.IO) {
        if (data.projects.isNotEmpty()) {
            db.projectDao().insertAll(data.projects)
        }
        if (data.accounts.isNotEmpty()) {
            db.accountDao().insertAll(data.accounts)
        }
        if (data.categories.isNotEmpty()) {
            db.categoryDao().insertAll(data.categories)
        }
        if (data.transactions.isNotEmpty()) {
            db.transactionDao().insertAll(data.transactions)
        }
        if (data.receivables.isNotEmpty()) {
            db.receivableDao().insertAll(data.receivables)
        }
        if (data.payables.isNotEmpty()) {
            db.payableDao().insertAll(data.payables)
        }
        if (data.workerInvoices.isNotEmpty()) {
            db.workerInvoiceDao().insertAllInvoices(data.workerInvoices)
        }
        if (data.workerJobItems.isNotEmpty()) {
            db.workerInvoiceDao().insertJobItems(data.workerJobItems)
        }
        if (data.workerLoanItems.isNotEmpty()) {
            db.workerInvoiceDao().insertLoanItems(data.workerLoanItems)
        }
    }
}
