package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
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
    val completedProjectCount: Int = 0
)

data class ProjectCostBreakdown(
    val material: Double = 0.0,
    val labor: Double = 0.0,
    val mobilization: Double = 0.0,
    val fuel: Double = 0.0,
    val equipment: Double = 0.0,
    val maintenance: Double = 0.0,
    val other: Double = 0.0
)

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
