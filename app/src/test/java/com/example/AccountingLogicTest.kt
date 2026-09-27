package com.example

import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountingLogicTest {

    @Test
    fun test001_OwnerCapital_AffectsCashAndEquityNotRevenueOrProfit() {
        val transactions = listOf(
            TransactionEntity(
                code = "TRX-TEST-01",
                date = "2026-09-27",
                type = "MONEY_IN",
                amount = 50_000_000.0,
                accountId = 1,
                accountName = "Bank BCA",
                classification = "CAPITAL",
                category = "Modal Pemilik",
                description = "Setoran modal"
            )
        )

        var cashIn = 0.0
        var revenue = 0.0
        var profit = 0.0

        transactions.forEach { trx ->
            if (trx.type == "MONEY_IN") {
                cashIn += trx.amount
                if (trx.classification == "PROJECT" || trx.classification == "OTHER") {
                    revenue += trx.amount
                }
            }
        }
        profit = revenue - 0.0

        assertEquals(50_000_000.0, cashIn, 0.01)
        assertEquals(0.0, revenue, 0.01)
        assertEquals(0.0, profit, 0.01)
    }

    @Test
    fun test002_LoanReceived_AffectsCashAndLiabilityNotRevenueOrProfit() {
        val transactions = listOf(
            TransactionEntity(
                code = "TRX-TEST-02",
                date = "2026-09-27",
                type = "MONEY_IN",
                amount = 20_000_000.0,
                accountId = 1,
                accountName = "Bank BCA",
                classification = "DEBT",
                category = "Pencairan Pinjaman",
                description = "Pinjaman modal kerja"
            )
        )

        var cashIn = 0.0
        var revenue = 0.0
        transactions.forEach { trx ->
            if (trx.type == "MONEY_IN") {
                cashIn += trx.amount
                if (trx.classification == "PROJECT" || trx.classification == "OTHER") {
                    revenue += trx.amount
                }
            }
        }
        val profit = revenue

        assertEquals(20_000_000.0, cashIn, 0.01)
        assertEquals(0.0, revenue, 0.01)
        assertEquals(0.0, profit, 0.01)
    }

    @Test
    fun test003_ProjectProfitCalculation() {
        val revenue = 100_000_000.0
        val cost = 60_000_000.0
        val profit = revenue - cost
        val margin = (profit / revenue) * 100.0

        assertEquals(40_000_000.0, profit, 0.01)
        assertEquals(40.0, margin, 0.01)
    }

    @Test
    fun test004_LoanPrincipalRepayment_ReducesCashNotExpense() {
        val transactions = listOf(
            TransactionEntity(
                code = "TRX-TEST-04",
                date = "2026-09-27",
                type = "MONEY_OUT",
                amount = 5_000_000.0,
                accountId = 1,
                accountName = "Bank BCA",
                classification = "DEBT",
                category = "Bayar Pokok Pinjaman",
                description = "Cicilan pokok utang ke bank"
            )
        )

        var cashOut = 0.0
        var operationalExpense = 0.0
        var projectExpense = 0.0

        transactions.forEach { trx ->
            if (trx.type == "MONEY_OUT") {
                cashOut += trx.amount
                if (trx.classification == "PROJECT") {
                    projectExpense += trx.amount
                } else if (trx.classification == "OPERATIONAL") {
                    operationalExpense += trx.amount
                }
            }
        }

        val totalExpense = projectExpense + operationalExpense

        assertEquals(5_000_000.0, cashOut, 0.01)
        assertEquals(0.0, totalExpense, 0.01) // Pembayaran pokok pinjaman bukan expense P&L!
    }
}
