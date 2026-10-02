package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

fun formatNumberWithDots(number: Long): String {
    val str = number.toString()
    val sb = StringBuilder()
    for (i in str.indices) {
        if (i > 0 && (str.length - i) % 3 == 0) {
            sb.append('.')
        }
        sb.append(str[i])
    }
    return sb.toString()
}

fun formatInputNumber(input: String): String {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val parsed = digits.toLongOrNull() ?: 0L
    return formatNumberWithDots(parsed)
}

fun parseInputNumber(input: String): Double {
    val digits = input.filter { it.isDigit() }
    return digits.toDoubleOrNull() ?: 0.0
}

data class GlobalFinancialSummary(
    val totalCash: Double = 0.0,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalProjectCost: Double = 0.0,
    val totalOperationalCost: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalReceivableOutstanding: Double = 0.0,
    val totalPayableOutstanding: Double = 0.0,
    val activeProjectCount: Int = 0,
    val completedProjectCount: Int = 0,
    // Rincian HPP Perusahaan
    val totalMaterial: Double = 0.0,
    val totalLaborMandor: Double = 0.0,
    val totalLaborWorker: Double = 0.0,
    val totalConsumption: Double = 0.0, // Konsumsi Anggota Bore Pile
    val totalMobilization: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalEquipment: Double = 0.0,
    val totalMaintenance: Double = 0.0,
    val totalOtherCost: Double = 0.0
)

data class ProjectCostBreakdown(
    val material: Double = 0.0,
    val laborMandor: Double = 0.0,
    val laborWorker: Double = 0.0,
    val consumption: Double = 0.0, // Konsumsi Anggota Bore Pile
    val mobilization: Double = 0.0,
    val fuel: Double = 0.0,
    val equipment: Double = 0.0,
    val maintenance: Double = 0.0,
    val other: Double = 0.0
) {
    val laborTotal: Double get() = laborMandor + laborWorker
    val labor: Double get() = laborTotal
}

data class ProjectFinancialSummary(
    val project: ProjectEntity,
    val contractAmount: Double,
    val cashReceived: Double,
    val totalCost: Double,
    val costBreakdown: ProjectCostBreakdown,
    val grossProfit: Double,
    val marginPercent: Double,
    val remainingContractBalance: Double
)

data class DailyCashSummary(
    val date: String,
    val totalIn: Double,
    val totalOut: Double,
    val netFlow: Double,
    val transactions: List<TransactionEntity>
)

data class PartnerProfitShare(
    val partner: ProfitPartnerEntity,
    val sharePercentage: Double,
    val grossShareAmount: Double, // Hak profit kotor berdasarkan % bagi hasil
    val totalWithdrawn: Double, // Total pengambilan profit / prive yang sudah ditarik
    val netRemainingProfit: Double // Sisa profit bersih yang diterima
)

data class ProfitDistributionSummary(
    val projectContextName: String, // "Semua Proyek (Seluruh Perusahaan)" atau nama proyek tertentu
    val totalDistributableProfit: Double, // Total laba yang dibagikan
    val partnerShares: List<PartnerProfitShare>
)

data class LoanItemDetail(
    val payable: PayableEntity,
    val id: Long,
    val date: String,
    val type: String, // "PINJAMAN_DANA", "SUPPLIER", "LOAN", "OTHER"
    val description: String,
    val amount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val dueDate: String,
    val status: String
)

data class RepaymentItemDetail(
    val transactionId: Long,
    val date: String,
    val amount: Double,
    val description: String,
    val paymentMethod: String,
    val accountName: String,
    val payableId: Long?
)

data class CreditorLedger(
    val creditorName: String,
    val loans: List<LoanItemDetail>,
    val repayments: List<RepaymentItemDetail>,
    val totalLoanAmount: Double,
    val totalRepaymentAmount: Double,
    val remainingBalance: Double,
    val isSettled: Boolean,
    val lastSettlementDate: String?,
    val classes: Set<String>
)
