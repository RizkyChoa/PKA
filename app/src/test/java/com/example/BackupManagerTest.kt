package com.example

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import com.example.util.BackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupManagerTest {

    @Test
    fun testExportAndImportBackupRoundTrip() {
        val projects = listOf(
            ProjectEntity(
                id = 1L,
                projectCode = "PRJ-001",
                name = "Pembangunan Flyover Sudirman",
                clientName = "PT Jaya Konstruksi",
                location = "Jakarta Pusat",
                startDate = "2026-01-10",
                targetDate = "2026-10-15",
                contractAmount = 750000000.0,
                status = "ACTIVE"
            )
        )

        val accounts = listOf(
            AccountEntity(
                id = 1L,
                name = "Kas Operasional",
                type = "CASH",
                currentBalance = 15000000.0
            )
        )

        val categories = listOf(
            CategoryEntity(
                id = 1L,
                name = "Upah Borongan Bore Pile",
                type = "EXPENSE",
                classification = "PROJECT_COST"
            )
        )

        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                trxNumber = "TRX-20260901-001",
                date = "2026-09-01",
                type = "MONEY_OUT",
                amount = 5000000.0,
                description = "Kasbon tim mandor",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1L,
                sourceAccountName = "Kas Operasional",
                categoryId = 1L,
                categoryName = "Upah Borongan Bore Pile",
                classification = "PROJECT",
                projectId = 1L,
                projectName = "Pembangunan Flyover Sudirman"
            )
        )

        val invoices = listOf(
            WorkerInvoiceEntity(
                id = 1L,
                invoiceNumber = "INV-WRK-202609-001",
                projectId = 1L,
                projectName = "Pembangunan Flyover Sudirman",
                workerLeaderName = "Pak Mandor Joko",
                date = "2026-09-27",
                status = "LUNAS"
            )
        )

        val jobs = listOf(
            WorkerJobItemEntity(
                id = 1L,
                invoiceId = 1L,
                jobName = "Bore Pile D40cm",
                pointCount = 30,
                depthMeters = 20.0,
                volumeMeters = 600.0,
                unitPricePerMeter = 45000.0,
                subtotal = 27000000.0
            )
        )

        val loans = listOf(
            WorkerLoanItemEntity(
                id = 1L,
                invoiceId = 1L,
                date = "2026-09-10",
                description = "DP Pinjaman anggota",
                trxType = "TRANSFER",
                amount = 4000000.0,
                deductionDescription = "Tiket transportasi",
                deductionAmount = 1000000.0
            )
        )

        // Export to JSON
        val json = BackupManager.exportToJson(
            projects = projects,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            receivables = emptyList(),
            payables = emptyList(),
            workerInvoices = invoices,
            workerJobItems = jobs,
            workerLoanItems = loans
        )

        assertNotNull(json)

        // Parse back from JSON
        val parsed = BackupManager.parseFromJson(json)

        assertEquals(1, parsed.projects.size)
        assertEquals("Pembangunan Flyover Sudirman", parsed.projects[0].name)

        assertEquals(1, parsed.transactions.size)
        assertEquals(5000000.0, parsed.transactions[0].amount, 0.01)

        assertEquals(1, parsed.workerInvoices.size)
        assertEquals("Pak Mandor Joko", parsed.workerInvoices[0].workerLeaderName)

        assertEquals(1, parsed.workerJobItems.size)
        assertEquals(30, parsed.workerJobItems[0].pointCount)
        assertEquals(27000000.0, parsed.workerJobItems[0].subtotal, 0.01)

        assertEquals(1, parsed.workerLoanItems.size)
        assertEquals(4000000.0, parsed.workerLoanItems[0].amount, 0.01)
        assertEquals(1000000.0, parsed.workerLoanItems[0].deductionAmount, 0.01)
    }
}
