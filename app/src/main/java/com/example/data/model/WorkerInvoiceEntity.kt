package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worker_invoices")
data class WorkerInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val projectId: Long,
    val projectName: String,
    val workerLeaderName: String, // Mandor / Nama Pekerja
    val date: String, // YYYY-MM-DD
    val status: String = "LUNAS", // "PROSES", "LUNAS"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "worker_job_items")
data class WorkerJobItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val jobName: String, // Contoh: "Bore pile diameter 40cm"
    val pointCount: Int, // Jumlah titik (contoh: 50)
    val depthMeters: Double, // Kedalaman meter (contoh: 24m)
    val volumeMeters: Double, // pointCount * depthMeters (contoh: 1200m)
    val unitPricePerMeter: Double, // Harga per meter (contoh: 40000)
    val subtotal: Double // volumeMeters * unitPricePerMeter
)

@Entity(tableName = "worker_loan_items")
data class WorkerLoanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val date: String, // Tanggal kasbon
    val description: String, // Contoh: "Pembayaran DP naik anggota 5 orang"
    val trxType: String, // "TRANSFER" / "CASH"
    val amount: Double, // Nominal kasbon (contoh: 5000000)
    val deductionDescription: String = "", // Keterangan potongan opsional (contoh: "ongkos keberangkatan")
    val deductionAmount: Double = 0.0 // Nominal potongan opsional (contoh: 1500000)
)

data class WorkerInvoiceWithDetails(
    val invoice: WorkerInvoiceEntity,
    val jobItems: List<WorkerJobItemEntity>,
    val loanItems: List<WorkerLoanItemEntity>
) {
    val totalEarnings: Double get() = jobItems.sumOf { it.subtotal }
    val totalLoans: Double get() = loanItems.sumOf { (it.amount - it.deductionAmount).coerceAtLeast(0.0) }
    val remainingBalance: Double get() = totalEarnings - totalLoans
}
