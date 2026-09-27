package com.example

import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerInvoiceWithDetails
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkerInvoiceTest {

    @Test
    fun testWorkerInvoiceEarningsAndLoansCalculation() {
        val invoice = WorkerInvoiceEntity(
            id = 1L,
            invoiceNumber = "INV-WRK-202609-001",
            projectId = 10L,
            projectName = "Proyek Jembatan",
            workerLeaderName = "Pak Slamet",
            date = "2026-09-27"
        )

        // 2 Job Items:
        // Job 1: 50 titik * 24m = 1200m * 40.000 = 48.000.000
        val job1 = WorkerJobItemEntity(
            id = 1L,
            invoiceId = 1L,
            jobName = "Bore Pile D40",
            pointCount = 50,
            depthMeters = 24.0,
            volumeMeters = 1200.0,
            unitPricePerMeter = 40000.0,
            subtotal = 48000000.0
        )

        // Job 2: 20 titik * 20m = 400m * 45.000 = 18.000.000
        val job2 = WorkerJobItemEntity(
            id = 2L,
            invoiceId = 1L,
            jobName = "Bore Pile D50",
            pointCount = 20,
            depthMeters = 20.0,
            volumeMeters = 400.0,
            unitPricePerMeter = 45000.0,
            subtotal = 18000000.0
        )

        // Kasbon 1: 5.000.000 - 1.500.000 (ongkos) = 3.500.000
        val loan1 = WorkerLoanItemEntity(
            id = 1L,
            invoiceId = 1L,
            date = "2026-09-20",
            description = "DP naik anggota 5 orang",
            trxType = "TRANSFER",
            amount = 5000000.0,
            deductionDescription = "ongkos tiket",
            deductionAmount = 1500000.0
        )

        // Kasbon 2 bertambah di kemudian hari: 2.000.000 (tanpa potongan)
        val loan2 = WorkerLoanItemEntity(
            id = 2L,
            invoiceId = 1L,
            date = "2026-09-25",
            description = "Tambahan kasbon makan & solar",
            trxType = "CASH",
            amount = 2000000.0,
            deductionDescription = "",
            deductionAmount = 0.0
        )

        val invoiceWithDetails = WorkerInvoiceWithDetails(
            invoice = invoice,
            jobItems = listOf(job1, job2),
            loanItems = listOf(loan1, loan2)
        )

        // Total earnings: 48jt + 18jt = 66jt
        assertEquals(66000000.0, invoiceWithDetails.totalEarnings, 0.01)

        // Total loans: 3.5jt + 2jt = 5.5jt
        assertEquals(5500000.0, invoiceWithDetails.totalLoans, 0.01)

        // Net balance: 66jt - 5.5jt = 60.5jt
        assertEquals(60500000.0, invoiceWithDetails.remainingBalance, 0.01)
    }

    @Test
    fun testAddAndRemoveJobItemsRecalculation() {
        val invoice = WorkerInvoiceEntity(
            id = 2L,
            invoiceNumber = "INV-WRK-202609-002",
            projectId = 10L,
            projectName = "Bore Pile Gudang Logistik Marunda",
            workerLeaderName = "Pak Bambang",
            date = "2026-09-27"
        )

        val jobInitial = WorkerJobItemEntity(
            id = 10L,
            invoiceId = 2L,
            jobName = "Bore pile diameter 40cm",
            pointCount = 50,
            depthMeters = 24.0,
            volumeMeters = 1200.0,
            unitPricePerMeter = 40000.0,
            subtotal = 48000000.0
        )

        val initialDetails = WorkerInvoiceWithDetails(
            invoice = invoice,
            jobItems = listOf(jobInitial),
            loanItems = emptyList()
        )
        assertEquals(48000000.0, initialDetails.totalEarnings, 0.01)

        // User adds a new job item (e.g. bobok pile cap atau bore pile diameter lain)
        val jobAdded = WorkerJobItemEntity(
            id = 11L,
            invoiceId = 2L,
            jobName = "Bore pile diameter 60cm",
            pointCount = 10,
            depthMeters = 20.0,
            volumeMeters = 200.0,
            unitPricePerMeter = 60000.0,
            subtotal = 12000000.0
        )

        val updatedDetailsWithNewJob = initialDetails.copy(
            jobItems = initialDetails.jobItems + jobAdded
        )
        assertEquals(60000000.0, updatedDetailsWithNewJob.totalEarnings, 0.01)

        // User deletes the first job item
        val updatedDetailsAfterDelete = updatedDetailsWithNewJob.copy(
            jobItems = updatedDetailsWithNewJob.jobItems.filter { it.id != 10L }
        )
        assertEquals(1, updatedDetailsAfterDelete.jobItems.size)
        assertEquals(12000000.0, updatedDetailsAfterDelete.totalEarnings, 0.01)
    }
}
