package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CashAccountEntity
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.ui.components.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialSummary(
    val totalCashBalance: Double = 0.0,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalProjectCost: Double = 0.0,
    val totalOperationalCost: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val activeProjectCount: Int = 0,
    val completedProjectCount: Int = 0,
    val accountBalances: Map<Int, Double> = emptyMap()
)

data class ProjectCostBreakdown(
    val materialCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val mobilizationCost: Double = 0.0,
    val fuelCost: Double = 0.0,
    val equipmentCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val revenueReceived: Double = 0.0,
    val contractValue: Double = 0.0,
    val grossProfit: Double = 0.0,
    val marginPercentage: Double = 0.0,
    val remainingReceivable: Double = 0.0
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    val allProjects: StateFlow<List<ProjectEntity>>
    val activeProjects: StateFlow<List<ProjectEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>
    val allAccounts: StateFlow<List<CashAccountEntity>>
    val allDebtReceivables: StateFlow<List<DebtReceivableEntity>>

    // Filter states for Cash Flow
    val searchQuery = MutableStateFlow("")
    val selectedFilterProjectId = MutableStateFlow<Int?>(null)
    val selectedFilterType = MutableStateFlow<String?>(null) // "MONEY_IN", "MONEY_OUT", null

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(db.appDao())

        allProjects = repository.allProjects.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        activeProjects = repository.activeProjects.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allTransactions = repository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allAccounts = repository.allAccounts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allDebtReceivables = repository.allDebtReceivables.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    // Single Calculation Engine adhering strictly to rules
    val financialSummary: StateFlow<FinancialSummary> = combine(
        allTransactions,
        allAccounts,
        allDebtReceivables,
        allProjects
    ) { transactions, accounts, debtReceivables, projects ->
        var totalCashIn = 0.0
        var totalCashOut = 0.0
        var totalRevenue = 0.0
        var totalProjectCost = 0.0
        var totalOperationalCost = 0.0

        val accountBalanceMap = accounts.associate { it.id to it.initialBalance }.toMutableMap()

        transactions.forEach { trx ->
            if (!trx.isVoid) {
                if (trx.type == "MONEY_IN") {
                    totalCashIn += trx.amount
                    val currentBal = accountBalanceMap[trx.accountId] ?: 0.0
                    accountBalanceMap[trx.accountId] = currentBal + trx.amount

                    // Sesuai Aturan Akuntansi:
                    // Modal Pemilik (CAPITAL) dan Pinjaman (DEBT) TIDAK DIANGGAP REVENUE!
                    if (trx.classification == "PROJECT" || trx.classification == "OTHER") {
                        totalRevenue += trx.amount
                    }
                } else if (trx.type == "MONEY_OUT") {
                    totalCashOut += trx.amount
                    val currentBal = accountBalanceMap[trx.accountId] ?: 0.0
                    accountBalanceMap[trx.accountId] = currentBal - trx.amount

                    // Beban Proyek
                    if (trx.classification == "PROJECT") {
                        totalProjectCost += trx.amount
                    }
                    // Beban Operasional Kantor
                    else if (trx.classification == "OPERATIONAL") {
                        totalOperationalCost += trx.amount
                    }
                    // CATATAN: Pembayaran Pokok Utang (DEBT) dan ASET tidak masuk ke Expense P&L
                }
            }
        }

        val totalCashBalance = accountBalanceMap.values.sum()
        val netCashFlow = totalCashIn - totalCashOut
        val netProfit = totalRevenue - (totalProjectCost + totalOperationalCost)

        val totalReceivable = debtReceivables
            .filter { it.type == "RECEIVABLE" && it.status != "PAID" }
            .sumOf { it.totalAmount - it.paidAmount }

        val totalPayable = debtReceivables
            .filter { it.type == "DEBT" && it.status != "PAID" }
            .sumOf { it.totalAmount - it.paidAmount }

        val activeCount = projects.count { it.status == "ACTIVE" }
        val completedCount = projects.count { it.status == "COMPLETED" }

        FinancialSummary(
            totalCashBalance = totalCashBalance,
            totalCashIn = totalCashIn,
            totalCashOut = totalCashOut,
            netCashFlow = netCashFlow,
            totalRevenue = totalRevenue,
            totalProjectCost = totalProjectCost,
            totalOperationalCost = totalOperationalCost,
            netProfit = netProfit,
            totalReceivable = totalReceivable,
            totalPayable = totalPayable,
            activeProjectCount = activeCount,
            completedProjectCount = completedCount,
            accountBalances = accountBalanceMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun calculateProjectBreakdown(
        project: ProjectEntity,
        transactions: List<TransactionEntity>
    ): ProjectCostBreakdown {
        var material = 0.0
        var labor = 0.0
        var mobilization = 0.0
        var fuel = 0.0
        var equipment = 0.0
        var other = 0.0
        var revenue = 0.0

        transactions.filter { it.projectId == project.id && !it.isVoid }.forEach { trx ->
            if (trx.type == "MONEY_IN") {
                revenue += trx.amount
            } else if (trx.type == "MONEY_OUT") {
                when {
                    trx.category.contains("Material", ignoreCase = true) || trx.category.contains("Beton", ignoreCase = true) -> material += trx.amount
                    trx.category.contains("Upah", ignoreCase = true) || trx.category.contains("Mandor", ignoreCase = true) || trx.category.contains("Operator", ignoreCase = true) -> labor += trx.amount
                    trx.category.contains("Mobilisasi", ignoreCase = true) -> mobilization += trx.amount
                    trx.category.contains("BBM", ignoreCase = true) || trx.category.contains("Solar", ignoreCase = true) -> fuel += trx.amount
                    trx.category.contains("Sewa", ignoreCase = true) || trx.category.contains("Alat", ignoreCase = true) -> equipment += trx.amount
                    else -> other += trx.amount
                }
            }
        }

        val totalCost = material + labor + mobilization + fuel + equipment + other
        val grossProfit = revenue - totalCost
        val margin = if (revenue > 0) (grossProfit / revenue) * 100.0 else 0.0
        val remainingReceivable = (project.contractValue - revenue).coerceAtLeast(0.0)

        return ProjectCostBreakdown(
            materialCost = material,
            laborCost = labor,
            mobilizationCost = mobilization,
            fuelCost = fuel,
            equipmentCost = equipment,
            otherCost = other,
            totalCost = totalCost,
            revenueReceived = revenue,
            contractValue = project.contractValue,
            grossProfit = grossProfit,
            marginPercentage = margin,
            remainingReceivable = remainingReceivable
        )
    }

    // === TRANSACTION ENGINE (SINGLE SOURCE OF TRUTH) ===
    fun addTransaction(
        date: String,
        type: String,
        amount: Double,
        accountId: Int,
        accountName: String,
        classification: String,
        category: String,
        description: String,
        projectId: Int?,
        projectName: String?,
        paymentMethod: String,
        referenceNumber: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val count = allTransactions.value.size + 1
            val code = Formatters.generateTrxCode(count)
            val newTrx = TransactionEntity(
                code = code,
                date = date,
                type = type,
                amount = amount,
                accountId = accountId,
                accountName = accountName,
                classification = classification,
                category = category,
                description = description,
                projectId = projectId,
                projectName = projectName,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                isVoid = false,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTransaction(newTrx)
            onSuccess()
        }
    }

    fun voidTransaction(trx: TransactionEntity, reason: String) {
        viewModelScope.launch {
            val updated = trx.copy(
                isVoid = true,
                voidReason = reason,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTransaction(updated)
        }
    }

    // === PROJECT MANAGEMENT ===
    fun addProject(
        name: String,
        client: String,
        location: String,
        startDate: String,
        targetEndDate: String,
        contractValue: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val count = allProjects.value.size + 1
            val code = "PRJ-2026-${String.format("%03d", count)}"
            val project = ProjectEntity(
                code = code,
                name = name,
                client = client,
                location = location,
                startDate = startDate,
                targetEndDate = targetEndDate,
                contractValue = contractValue,
                status = "ACTIVE",
                notes = notes
            )
            repository.insertProject(project)
            onSuccess()
        }
    }

    fun updateProjectStatus(project: ProjectEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = project.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateProject(updated)
        }
    }

    // === DEBT & RECEIVABLE SETTLEMENT ===
    fun payDebtOrReceivable(
        item: DebtReceivableEntity,
        paymentAmount: Double,
        accountId: Int,
        accountName: String,
        date: String,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            val newPaid = item.paidAmount + paymentAmount
            val newStatus = if (newPaid >= item.totalAmount) "PAID" else "PARTIAL"
            val updatedItem = item.copy(paidAmount = newPaid, status = newStatus)
            repository.updateDebtReceivable(updatedItem)

            // Catat otomatis ke Cash Flow (Single Source of Truth)
            val isReceivable = item.type == "RECEIVABLE"
            val trxType = if (isReceivable) "MONEY_IN" else "MONEY_OUT"
            val classification = if (isReceivable) "RECEIVABLE" else "DEBT"
            val category = if (isReceivable) "Penerimaan Piutang Klien" else "Pembayaran Utang Supplier"
            val description = "Pelunasan: ${item.title} (${item.partyName}) - $notes"

            val count = allTransactions.value.size + 1
            val code = Formatters.generateTrxCode(count)

            val trx = TransactionEntity(
                code = code,
                date = date,
                type = trxType,
                amount = paymentAmount,
                accountId = accountId,
                accountName = accountName,
                classification = classification,
                category = category,
                description = description,
                projectId = item.projectId,
                paymentMethod = paymentMethod,
                referenceNumber = "SETTLE-${item.id}"
            )
            repository.insertTransaction(trx)
        }
    }
}
