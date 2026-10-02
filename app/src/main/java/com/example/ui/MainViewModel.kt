package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BorePileJobItem
import com.example.data.model.CategoryEntity
import com.example.data.model.CreditorLedger
import com.example.data.model.DailyCashSummary
import com.example.data.model.GlobalFinancialSummary
import com.example.data.model.LoanItemDetail
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectCostBreakdown
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.ReceivableEntity
import com.example.data.model.RepaymentItemDetail
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.data.sample.InitialDataSeeder
import com.example.util.BackupManager
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FinanceRepository(db)

    val accounts = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Strict filter for new transaction creation: only projects that are NOT completed or cancelled
    val activeProjects = repository.activeProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Book Periods (Periode Buku & Arsip)
    val allBookPeriods = repository.allBookPeriods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBookPeriod = repository.activeBookPeriod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedPeriodId = MutableStateFlow<Long?>(null)
    val selectedPeriodId: StateFlow<Long?> = _selectedPeriodId.asStateFlow()

    // Currently selected Book Period (either user-selected archive or the active period)
    val currentPeriod: StateFlow<com.example.data.model.BookPeriodEntity?> = combine(
        allBookPeriods,
        activeBookPeriod,
        _selectedPeriodId
    ) { all, active, selId ->
        if (selId != null) {
            all.find { it.id == selId } ?: active
        } else {
            active ?: all.firstOrNull { it.status == "ACTIVE" } ?: all.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isCurrentPeriodReadOnly: StateFlow<Boolean> = currentPeriod.map { period ->
        period?.isArchived == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val rawAllTransactions = repository.allTransactions
    private val rawValidTransactions = repository.validTransactions

    // Filter transactions to the current selected period (Active or Archive)
    val allTransactions: StateFlow<List<TransactionEntity>> = combine(
        rawAllTransactions,
        currentPeriod
    ) { trxs, period ->
        if (period != null) {
            trxs.filter { it.date >= period.startDate && it.date <= period.endDate }
        } else {
            trxs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val validTransactions: StateFlow<List<TransactionEntity>> = combine(
        rawValidTransactions,
        currentPeriod
    ) { trxs, period ->
        if (period != null) {
            trxs.filter { it.date >= period.startDate && it.date <= period.endDate }
        } else {
            trxs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivables = repository.allReceivables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payables = repository.allPayables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profitPartners = repository.allProfitPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker Invoices With Details filtered by period
    val workerInvoicesWithDetails: StateFlow<List<com.example.data.model.WorkerInvoiceWithDetails>> = combine(
        repository.allWorkerInvoices,
        repository.allJobItems,
        repository.allLoanItems,
        currentPeriod
    ) { invoices, allJobs, allLoans, period ->
        val filtered = if (period != null) {
            invoices.filter { it.date >= period.startDate && it.date <= period.endDate }
        } else {
            invoices
        }
        filtered.map { inv ->
            val jobs = allJobs.filter { it.invoiceId == inv.id }
            val loans = allLoans.filter { it.invoiceId == inv.id }
            com.example.data.model.WorkerInvoiceWithDetails(
                invoice = inv,
                jobItems = jobs,
                loanItems = loans
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Project Selector state (User can choose which project P&L to highlight on Dashboard!)
    private val _selectedDashboardProjectId = MutableStateFlow<Long?>(null)
    val selectedDashboardProjectId: StateFlow<Long?> = _selectedDashboardProjectId.asStateFlow()

    init {
        viewModelScope.launch {
            InitialDataSeeder.seedIfEmpty(db)
        }
    }

    fun selectDashboardProject(projectId: Long?) {
        _selectedDashboardProjectId.value = projectId
    }

    // Global Financial Summary
    val globalSummary: StateFlow<GlobalFinancialSummary> = combine(
        accounts,
        validTransactions,
        receivables,
        payables,
        combine(allProjects, currentPeriod) { p, c -> Pair(p, c) }
    ) { accList, trxList, recList, payList, (projList, period) ->
        val totalCash = if (period?.isArchived == true && period.closingCashBalance > 0) {
            period.closingCashBalance
        } else {
            accList.sumOf { it.currentBalance }
        }

        var cashIn = 0.0
        var cashOut = 0.0
        var totalRev = 0.0
        var totalPrjCost = 0.0
        var totalOpsCost = 0.0

        var gMaterial = 0.0
        var gLaborMandor = 0.0
        var gLaborWorker = 0.0
        var gMobil = 0.0
        var gFuel = 0.0
        var gEquip = 0.0
        var gMaint = 0.0
        var gOther = 0.0

        for (trx in trxList) {
            when (trx.type) {
                "MONEY_IN" -> {
                    cashIn += trx.amount
                    // Distinguish Accounting Revenue vs Capital/Debt/Transfer
                    if (trx.classification == "PROJECT" || trx.classification == "REVENUE") {
                        totalRev += trx.amount
                    }
                }
                "MONEY_OUT" -> {
                    cashOut += trx.amount
                    // Distinguish Accounting Expenses vs Asset/Debt Repayment/Equity Draw
                    if (trx.classification == "PROJECT") {
                        totalPrjCost += trx.amount
                        val cg = trx.costGroup.uppercase()
                        if (cg == "MATERIAL") {
                            gMaterial += trx.amount
                        } else if (cg == "LABOR_MANDOR" || cg == "MANDOR" ||
                            (cg == "LABOR" && (trx.categoryName.contains("mandor", ignoreCase = true) || trx.description.contains("mandor", ignoreCase = true))) ||
                            trx.categoryName.contains("mandor", ignoreCase = true)
                        ) {
                            gLaborMandor += trx.amount
                        } else if (cg == "LABOR_WORKER" || cg == "PEKERJA" || cg == "LABOR" ||
                            trx.categoryName.contains("pekerja", ignoreCase = true) ||
                            trx.description.contains("pekerja", ignoreCase = true)
                        ) {
                            gLaborWorker += trx.amount
                        } else if (cg == "MOBILIZATION") {
                            gMobil += trx.amount
                        } else if (cg == "FUEL") {
                            gFuel += trx.amount
                        } else if (cg == "EQUIPMENT") {
                            gEquip += trx.amount
                        } else if (cg == "MAINTENANCE") {
                            gMaint += trx.amount
                        } else {
                            if (trx.description.contains("mandor", ignoreCase = true)) {
                                gLaborMandor += trx.amount
                            } else if (trx.description.contains("pekerja", ignoreCase = true)) {
                                gLaborWorker += trx.amount
                            } else {
                                gOther += trx.amount
                            }
                        }
                    } else if (trx.classification == "OPERATIONAL") {
                        totalOpsCost += trx.amount
                    }
                }
            }
        }

        val totalExp = totalPrjCost + totalOpsCost
        val netProfit = totalRev - totalExp
        val netCash = cashIn - cashOut
        val recOutstanding = if (period?.isArchived == true && period.closingReceivableBalance > 0) {
            period.closingReceivableBalance
        } else {
            recList.sumOf { it.remainingAmount }
        }
        val payOutstanding = if (period?.isArchived == true && period.closingPayableBalance > 0) {
            period.closingPayableBalance
        } else {
            payList.sumOf { it.remainingAmount }
        }
        val activeCount = if (period?.isArchived == true && period.activeProjectCount > 0) {
            period.activeProjectCount
        } else {
            projList.count { it.status == "ACTIVE" || it.status == "PLANNING" }
        }
        val compCount = projList.count { it.status == "COMPLETED" }

        GlobalFinancialSummary(
            totalCash = totalCash,
            totalCashIn = cashIn,
            totalCashOut = cashOut,
            netCashFlow = netCash,
            totalRevenue = totalRev,
            totalProjectCost = totalPrjCost,
            totalOperationalCost = totalOpsCost,
            totalExpense = totalExp,
            netProfit = netProfit,
            totalReceivableOutstanding = recOutstanding,
            totalPayableOutstanding = payOutstanding,
            activeProjectCount = activeCount,
            completedProjectCount = compCount,
            totalMaterial = gMaterial,
            totalLaborMandor = gLaborMandor,
            totalLaborWorker = gLaborWorker,
            totalMobilization = gMobil,
            totalFuel = gFuel,
            totalEquipment = gEquip,
            totalMaintenance = gMaint,
            totalOtherCost = gOther
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalFinancialSummary())

    // All Projects P&L Summaries
    val projectSummaries: StateFlow<List<ProjectFinancialSummary>> = combine(
        allProjects,
        validTransactions
    ) { projList, trxList ->
        projList.map { project ->
            val projectTrx = trxList.filter { it.projectId == project.id }
            val cashReceived = projectTrx.filter { it.type == "MONEY_IN" }.sumOf { it.amount }

            var material = 0.0
            var laborMandor = 0.0
            var laborWorker = 0.0
            var mobil = 0.0
            var fuel = 0.0
            var equip = 0.0
            var maint = 0.0
            var other = 0.0

            for (t in projectTrx.filter { it.type == "MONEY_OUT" }) {
                val cg = t.costGroup.uppercase()
                if (cg == "MATERIAL") {
                    material += t.amount
                } else if (cg == "LABOR_MANDOR" || cg == "MANDOR" ||
                    (cg == "LABOR" && (t.categoryName.contains("mandor", ignoreCase = true) || t.description.contains("mandor", ignoreCase = true))) ||
                    t.categoryName.contains("mandor", ignoreCase = true)
                ) {
                    laborMandor += t.amount
                } else if (cg == "LABOR_WORKER" || cg == "PEKERJA" || cg == "LABOR" ||
                    t.categoryName.contains("pekerja", ignoreCase = true) ||
                    t.description.contains("pekerja", ignoreCase = true)
                ) {
                    laborWorker += t.amount
                } else if (cg == "MOBILIZATION") {
                    mobil += t.amount
                } else if (cg == "FUEL") {
                    fuel += t.amount
                } else if (cg == "EQUIPMENT") {
                    equip += t.amount
                } else if (cg == "MAINTENANCE") {
                    maint += t.amount
                } else {
                    if (t.description.contains("mandor", ignoreCase = true)) {
                        laborMandor += t.amount
                    } else if (t.description.contains("pekerja", ignoreCase = true)) {
                        laborWorker += t.amount
                    } else {
                        other += t.amount
                    }
                }
            }

            val totalCost = material + laborMandor + laborWorker + mobil + fuel + equip + maint + other
            val grossProfit = cashReceived - totalCost
            val margin = if (cashReceived > 0) (grossProfit / cashReceived) * 100 else 0.0
            val remainingContract = (project.contractAmount - cashReceived).coerceAtLeast(0.0)

            ProjectFinancialSummary(
                project = project,
                contractAmount = project.contractAmount,
                cashReceived = cashReceived,
                totalCost = totalCost,
                costBreakdown = ProjectCostBreakdown(
                    material = material,
                    laborMandor = laborMandor,
                    laborWorker = laborWorker,
                    mobilization = mobil,
                    fuel = fuel,
                    equipment = equip,
                    maintenance = maint,
                    other = other
                ),
                grossProfit = grossProfit,
                marginPercent = margin,
                remainingContractBalance = remainingContract
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Cash Flow Summaries
    val dailySummaries: StateFlow<List<DailyCashSummary>> = validTransactions.combine(allTransactions) { validTrx, _ ->
        val grouped = validTrx.groupBy { it.date }
        grouped.map { (date, trxs) ->
            val inAmt = trxs.filter { it.type == "MONEY_IN" }.sumOf { it.amount }
            val outAmt = trxs.filter { it.type == "MONEY_OUT" }.sumOf { it.amount }
            DailyCashSummary(
                date = date,
                totalIn = inAmt,
                totalOut = outAmt,
                netFlow = inAmt - outAmt,
                transactions = trxs
            )
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Detail Ledger Utang & Pinjaman per Pemberi Pinjaman / Supplier
    val creditorLedgers: StateFlow<List<CreditorLedger>> = combine(
        payables,
        validTransactions
    ) { payList, trxList ->
        val groupedPayables = payList.groupBy { it.creditorName.trim() }

        groupedPayables.map { (creditorName, payablesForCreditor) ->
            val payableIds = payablesForCreditor.map { it.id }.toSet()

            // 1. Detail seluruh pinjaman / utang dari pihak ini
            val loanDetails = payablesForCreditor.map { pay ->
                LoanItemDetail(
                    payable = pay,
                    id = pay.id,
                    date = pay.date.ifBlank { pay.dueDate },
                    type = if (pay.type == "LOAN" || pay.type == "PINJAMAN_DANA") "Pinjaman Dana" else "Utang Usaha & Supplier",
                    description = pay.description.ifBlank { "Pinjaman/Utang" },
                    amount = pay.totalAmount,
                    paidAmount = pay.paidAmount,
                    remainingAmount = pay.remainingAmount,
                    dueDate = pay.dueDate,
                    status = pay.status
                )
            }.sortedWith(compareBy({ it.date }, { it.id }))

            // 2. Detail riwayat seluruh pelunasan / cicilan ke pihak ini
            val repaymentsFromTrx = trxList.filter { trx ->
                trx.type == "MONEY_OUT" && (
                    (trx.payableId != null && payableIds.contains(trx.payableId)) ||
                    (trx.payableId == null && trx.classification == "DEBT" && trx.description.contains(creditorName, ignoreCase = true))
                )
            }.map { trx ->
                RepaymentItemDetail(
                    transactionId = trx.id,
                    date = trx.date,
                    amount = trx.amount,
                    description = trx.description,
                    paymentMethod = trx.paymentMethod,
                    accountName = trx.sourceAccountName,
                    payableId = trx.payableId
                )
            }.sortedWith(compareBy({ it.date }, { it.transactionId }))

            val totalLoanAmount = loanDetails.sumOf { it.amount }
            val totalRepaymentTrx = repaymentsFromTrx.sumOf { it.amount }
            val totalPayablesPaid = payablesForCreditor.sumOf { it.paidAmount }
            val totalRepaymentAmount = maxOf(totalRepaymentTrx, totalPayablesPaid)
            val remainingBalance = (totalLoanAmount - totalRepaymentAmount).coerceAtLeast(0.0)
            val isSettled = remainingBalance <= 0.0 && totalLoanAmount > 0

            // 3. Tanggal Pelunasan Terakhir:
            // Diambil dari tanggal transaksi pembayaran yang benar-benar membuat outstanding menjadi Rp0
            var lastSettlementDate: String? = null
            if (isSettled) {
                var runningRepaid = 0.0
                for (rep in repaymentsFromTrx) {
                    runningRepaid += rep.amount
                    if (runningRepaid >= totalLoanAmount) {
                        lastSettlementDate = rep.date
                        break
                    }
                }
                if (lastSettlementDate == null) {
                    lastSettlementDate = repaymentsFromTrx.lastOrNull()?.date ?: payablesForCreditor.maxOfOrNull { it.dueDate }
                }
            }

            val classes = payablesForCreditor.map {
                if (it.type == "LOAN" || it.type == "PINJAMAN_DANA") "Pinjaman Dana" else "Utang Usaha & Supplier"
            }.toSet()

            CreditorLedger(
                creditorName = creditorName,
                loans = loanDetails,
                repayments = repaymentsFromTrx,
                totalLoanAmount = totalLoanAmount,
                totalRepaymentAmount = totalRepaymentAmount,
                remainingBalance = remainingBalance,
                isSettled = isSettled,
                lastSettlementDate = lastSettlementDate,
                classes = classes
            )
        }.sortedWith(
            compareBy<CreditorLedger> { it.isSettled }
                .thenBy { it.creditorName.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(
        name: String,
        client: String,
        location: String,
        startDate: String,
        targetDate: String,
        contractAmount: Double,
        notes: String,
        jobItems: List<BorePileJobItem> = emptyList(),
        mobiUnits: Int = 0,
        mobiPricePerUnit: Double = 0.0
    ) {
        viewModelScope.launch {
            val count = allProjects.value.size + 1
            val code = "PRJ-2026-${String.format(java.util.Locale.US, "%03d", count)}"
            val firstJob = jobItems.firstOrNull()
            val totalPoints = jobItems.sumOf { it.pointCount }
            val avgDepth = if (jobItems.isNotEmpty()) jobItems.map { it.depthMeters }.average() else 0.0
            val avgPrice = if (jobItems.isNotEmpty()) jobItems.map { it.pricePerMeter }.average() else 0.0

            repository.insertProject(
                ProjectEntity(
                    projectCode = code,
                    name = name,
                    clientName = client,
                    location = location,
                    startDate = startDate,
                    targetDate = targetDate,
                    contractAmount = contractAmount,
                    status = "ACTIVE",
                    notes = notes,
                    jobName = firstJob?.jobName ?: "",
                    pointCount = totalPoints,
                    depthMeters = avgDepth,
                    pricePerMeter = avgPrice,
                    mobiUnits = mobiUnits,
                    mobiPricePerUnit = mobiPricePerUnit,
                    jobItemsJson = ProjectEntity.serializeJobItems(jobItems)
                )
            )
        }
    }

    fun updateProject(
        id: Long,
        name: String,
        client: String,
        location: String,
        startDate: String,
        targetDate: String,
        contractAmount: Double,
        notes: String,
        jobItems: List<BorePileJobItem> = emptyList(),
        mobiUnits: Int = 0,
        mobiPricePerUnit: Double = 0.0
    ) {
        viewModelScope.launch {
            val existing = allProjects.value.find { it.id == id } ?: return@launch
            val firstJob = jobItems.firstOrNull()
            val totalPoints = jobItems.sumOf { it.pointCount }
            val avgDepth = if (jobItems.isNotEmpty()) jobItems.map { it.depthMeters }.average() else 0.0
            val avgPrice = if (jobItems.isNotEmpty()) jobItems.map { it.pricePerMeter }.average() else 0.0

            repository.updateProject(
                existing.copy(
                    name = name,
                    clientName = client,
                    location = location,
                    startDate = startDate,
                    targetDate = targetDate,
                    contractAmount = contractAmount,
                    notes = notes,
                    jobName = firstJob?.jobName ?: existing.jobName,
                    pointCount = if (jobItems.isNotEmpty()) totalPoints else existing.pointCount,
                    depthMeters = if (jobItems.isNotEmpty()) avgDepth else existing.depthMeters,
                    pricePerMeter = if (jobItems.isNotEmpty()) avgPrice else existing.pricePerMeter,
                    mobiUnits = mobiUnits,
                    mobiPricePerUnit = mobiPricePerUnit,
                    jobItemsJson = if (jobItems.isNotEmpty()) ProjectEntity.serializeJobItems(jobItems) else existing.jobItemsJson
                )
            )
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    fun createAccount(name: String, type: String, accountNumber: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.insertAccount(
                AccountEntity(
                    name = name,
                    type = type,
                    accountNumber = accountNumber,
                    initialBalance = initialBalance,
                    currentBalance = initialBalance,
                    isActive = true
                )
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun completeProject(projectId: Long) {
        viewModelScope.launch {
            repository.updateProjectStatus(projectId, "COMPLETED")
        }
    }

    fun reopenProject(projectId: Long) {
        viewModelScope.launch {
            repository.updateProjectStatus(projectId, "ACTIVE")
        }
    }

    fun createTransaction(
        date: String,
        type: String,
        amount: Double,
        description: String,
        paymentMethod: String,
        sourceAccountId: Long,
        destinationAccountId: Long? = null,
        categoryId: Long,
        classification: String,
        projectId: Long? = null,
        costGroup: String = "",
        receivableId: Long? = null,
        payableId: Long? = null,
        profitPartnerId: Long? = null,
        profitPartnerName: String? = null
    ) {
        viewModelScope.launch {
            repository.createTransaction(
                date = date,
                type = type,
                amount = amount,
                description = description,
                paymentMethod = paymentMethod,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destinationAccountId,
                categoryId = categoryId,
                classification = classification,
                projectId = projectId,
                receivableId = receivableId,
                payableId = payableId,
                profitPartnerId = profitPartnerId,
                profitPartnerName = profitPartnerName,
                costGroup = costGroup
            )
        }
    }

    fun voidTransaction(id: Long, reason: String) {
        viewModelScope.launch {
            repository.voidTransaction(id, reason)
        }
    }

    fun createWorkerInvoice(
        projectId: Long,
        projectName: String,
        workerLeaderName: String,
        workerRole: String = "PEKERJA",
        date: String,
        notes: String,
        jobItems: List<com.example.data.model.WorkerJobItemEntity>,
        loanItems: List<com.example.data.model.WorkerLoanItemEntity>
    ) {
        viewModelScope.launch {
            val count = workerInvoicesWithDetails.value.size + 1
            val prefix = if (workerRole == "MANDOR") "INV-MND" else "INV-WRK"
            val invNumber = "$prefix-${date.replace("-", "").take(6)}-${String.format(java.util.Locale.US, "%03d", count)}"
            val invoice = com.example.data.model.WorkerInvoiceEntity(
                invoiceNumber = invNumber,
                projectId = projectId,
                projectName = projectName,
                workerLeaderName = workerLeaderName,
                workerRole = workerRole,
                date = date,
                notes = notes,
                status = "LUNAS"
            )
            repository.createWorkerInvoice(invoice, jobItems, loanItems)
        }
    }

    fun deleteWorkerInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkerInvoice(id)
        }
    }

    fun addWorkerJobItem(
        invoiceId: Long,
        jobName: String,
        pointCount: Int,
        depthMeters: Double,
        unitPricePerMeter: Double
    ) {
        val volumeMeters = pointCount * depthMeters
        val subtotal = volumeMeters * unitPricePerMeter
        viewModelScope.launch {
            repository.addJobItem(
                com.example.data.model.WorkerJobItemEntity(
                    invoiceId = invoiceId,
                    jobName = jobName,
                    pointCount = pointCount,
                    depthMeters = depthMeters,
                    volumeMeters = volumeMeters,
                    unitPricePerMeter = unitPricePerMeter,
                    subtotal = subtotal
                )
            )
        }
    }

    fun updateWorkerJobItem(
        id: Long,
        invoiceId: Long,
        jobName: String,
        pointCount: Int,
        depthMeters: Double,
        unitPricePerMeter: Double
    ) {
        val volumeMeters = pointCount * depthMeters
        val subtotal = volumeMeters * unitPricePerMeter
        viewModelScope.launch {
            repository.updateJobItem(
                com.example.data.model.WorkerJobItemEntity(
                    id = id,
                    invoiceId = invoiceId,
                    jobName = jobName,
                    pointCount = pointCount,
                    depthMeters = depthMeters,
                    volumeMeters = volumeMeters,
                    unitPricePerMeter = unitPricePerMeter,
                    subtotal = subtotal
                )
            )
        }
    }

    fun deleteWorkerJobItem(jobItemId: Long) {
        viewModelScope.launch {
            repository.deleteJobItem(jobItemId)
        }
    }

    fun addWorkerLoanItem(
        invoiceId: Long,
        date: String,
        description: String,
        trxType: String,
        amount: Double,
        deductionDescription: String,
        deductionAmount: Double
    ) {
        viewModelScope.launch {
            repository.addLoanItem(
                com.example.data.model.WorkerLoanItemEntity(
                    invoiceId = invoiceId,
                    date = date,
                    description = description,
                    trxType = trxType,
                    amount = amount,
                    deductionDescription = deductionDescription,
                    deductionAmount = deductionAmount
                )
            )
        }
    }

    fun updateWorkerLoanItem(
        id: Long,
        invoiceId: Long,
        date: String,
        description: String,
        trxType: String,
        amount: Double,
        deductionDescription: String,
        deductionAmount: Double
    ) {
        viewModelScope.launch {
            repository.updateLoanItem(
                com.example.data.model.WorkerLoanItemEntity(
                    id = id,
                    invoiceId = invoiceId,
                    date = date,
                    description = description,
                    trxType = trxType,
                    amount = amount,
                    deductionDescription = deductionDescription,
                    deductionAmount = deductionAmount
                )
            )
        }
    }

    fun deleteWorkerLoanItem(loanItemId: Long) {
        viewModelScope.launch {
            repository.deleteLoanItem(loanItemId)
        }
    }

    fun updateWorkerInvoice(invoice: com.example.data.model.WorkerInvoiceEntity) {
        viewModelScope.launch {
            repository.updateWorkerInvoice(invoice)
        }
    }

    // Profit Partners (Bagi Hasil) Management
    fun createProfitPartner(name: String, percentage: Double, phone: String = "", notes: String = "") {
        viewModelScope.launch {
            repository.insertProfitPartner(
                com.example.data.model.ProfitPartnerEntity(
                    name = name.trim(),
                    sharePercentage = percentage,
                    phone = phone.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun updateProfitPartner(id: Long, name: String, percentage: Double, phone: String = "", notes: String = "") {
        viewModelScope.launch {
            repository.updateProfitPartner(
                com.example.data.model.ProfitPartnerEntity(
                    id = id,
                    name = name.trim(),
                    sharePercentage = percentage,
                    phone = phone.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteProfitPartner(id: Long) {
        viewModelScope.launch {
            repository.deleteProfitPartner(id)
        }
    }

    fun payCreditor(
        creditorName: String,
        amount: Double,
        sourceAccountId: Long,
        date: String,
        method: String = "TRANSFER",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.payCreditorRepayment(creditorName, amount, sourceAccountId, date, method, notes)
        }
    }

    fun payPayablesBatch(payableIds: List<Long>, sourceAccountId: Long, date: String, method: String = "TRANSFER", notes: String = "") {
        viewModelScope.launch {
            repository.payPayablesBatch(payableIds, sourceAccountId, date, method, notes)
        }
    }

    fun payReceivablesBatch(receivableIds: List<Long>, destinationAccountId: Long, date: String, method: String = "TRANSFER", notes: String = "") {
        viewModelScope.launch {
            repository.payReceivablesBatch(receivableIds, destinationAccountId, date, method, notes)
        }
    }

    fun createPayable(
        creditorName: String,
        type: String,
        description: String,
        totalAmount: Double,
        dueDate: String,
        destinationAccountId: Long? = null,
        transactionDate: String = dueDate
    ) {
        viewModelScope.launch {
            val accId = destinationAccountId ?: accounts.value.firstOrNull()?.id ?: 1L
            repository.insertPayableWithTransaction(
                creditorName = creditorName,
                type = type,
                description = description,
                totalAmount = totalAmount,
                dueDate = dueDate,
                destinationAccountId = accId,
                transactionDate = transactionDate
            )
        }
    }

    fun deletePayable(id: Long) {
        viewModelScope.launch {
            repository.deletePayable(id)
        }
    }

    fun createReceivable(clientName: String, projectName: String, invoiceNumber: String, description: String, totalAmount: Double, dueDate: String, projectId: Long? = null) {
        viewModelScope.launch {
            repository.insertReceivable(
                com.example.data.model.ReceivableEntity(
                    projectId = projectId,
                    projectName = projectName.trim(),
                    clientName = clientName.trim(),
                    invoiceNumber = invoiceNumber.trim(),
                    description = description.trim(),
                    totalAmount = totalAmount,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteReceivable(id: Long) {
        viewModelScope.launch {
            repository.deleteReceivable(id)
        }
    }

    fun receiveProjectPayment(projectId: Long, amount: Double, destinationAccountId: Long, date: String, method: String = "TRANSFER", notes: String = "") {
        viewModelScope.launch {
            repository.receiveProjectPayment(projectId, amount, destinationAccountId, date, method, notes)
        }
    }

    // Sync / Backup State & Actions
    private val _syncStateMessage = MutableStateFlow<String?>(null)
    val syncStateMessage: StateFlow<String?> = _syncStateMessage.asStateFlow()

    fun clearSyncMessage() {
        _syncStateMessage.value = null
    }

    fun exportBackup(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.exportAllData()
                val json = BackupManager.exportToJson(
                    projects = data.projects,
                    accounts = data.accounts,
                    categories = data.categories,
                    transactions = data.transactions,
                    receivables = data.receivables,
                    payables = data.payables,
                    workerInvoices = data.workerInvoices,
                    workerJobItems = data.workerJobItems,
                    workerLoanItems = data.workerLoanItems
                )
                val file = BackupManager.saveBackupToCache(context, json)
                onReady(file)
            } catch (e: Exception) {
                _syncStateMessage.value = "Gagal membuat cadangan: ${e.message}"
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = BackupManager.readJsonFromUri(context, uri)
                val parsed = BackupManager.parseFromJson(json)
                repository.importBackupData(parsed)
                val summary = "Sinkronisasi berhasil! Diperbarui: ${parsed.projects.size} Proyek, ${parsed.transactions.size} Transaksi, ${parsed.workerInvoices.size} Invoice Pekerja (${parsed.workerJobItems.size} Pekerjaan, ${parsed.workerLoanItems.size} Kasbon)."
                _syncStateMessage.value = summary
                onSuccess(summary)
            } catch (e: Exception) {
                _syncStateMessage.value = "Gagal mengimpor file: ${e.message}"
            }
        }
    }

    // Book Period & Archive Actions
    fun selectPeriod(periodId: Long?) {
        _selectedPeriodId.value = periodId
    }

    fun returnToActivePeriod() {
        _selectedPeriodId.value = null
    }

    fun closeAndArchiveBookPeriod(
        archiveName: String,
        newPeriodName: String,
        newStartDate: String,
        newEndDate: String,
        notes: String = "",
        onComplete: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currId = currentPeriod.value?.id ?: 1L
            val newId = repository.closeAndArchiveBookPeriod(
                currentPeriodId = currId,
                archiveName = archiveName,
                newPeriodName = newPeriodName,
                newStartDate = newStartDate,
                newEndDate = newEndDate,
                notes = notes
            )
            _selectedPeriodId.value = null
            onComplete("Tutup buku berhasil! Periode '$archiveName' telah diarsipkan dan periode baru '$newPeriodName' aktif.")
        }
    }

    fun renameBookPeriod(id: Long, newName: String) {
        viewModelScope.launch {
            repository.updatePeriodName(id, newName)
        }
    }

    fun unlockBookPeriod(id: Long) {
        viewModelScope.launch {
            repository.setPeriodStatus(id, "ACTIVE")
        }
    }

    fun lockBookPeriod(id: Long) {
        viewModelScope.launch {
            repository.setPeriodStatus(id, "ARCHIVED")
        }
    }
}
