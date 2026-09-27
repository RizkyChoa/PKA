package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String, // e.g. PRJ-2026-001
    val name: String, // e.g. Proyek Bore Pile RSUD Graha Husada
    val client: String, // e.g. PT Waskita Karya
    val location: String, // e.g. Cikarang, Jawa Barat
    val startDate: String, // YYYY-MM-DD
    val targetEndDate: String, // YYYY-MM-DD
    val contractValue: Double, // Nilai Kontrak Rp
    val status: String = "ACTIVE", // PLANNING, ACTIVE, COMPLETED, CANCELLED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_accounts")
data class CashAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // e.g. "BCA Operasional", "Mandiri Proyek", "Kas Kecil Tunai"
    val accountNumber: String = "",
    val initialBalance: Double = 0.0,
    val type: String = "BANK" // BANK, CASH, EWALLET
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // e.g. TRX-20260927-0001
    val date: String, // YYYY-MM-DD
    val type: String, // MONEY_IN, MONEY_OUT
    val amount: Double,
    val accountId: Int, // ID Rekening Kas/Bank
    val accountName: String,
    val classification: String, // PROJECT, OPERATIONAL, DEBT, RECEIVABLE, CAPITAL, ASSET, OTHER
    val category: String, // e.g. "DP Proyek", "Termin Proyek", "Material Beton", "Upah Operator", "Mobilisasi Rig", "BBM Solar", "Operasional Kantor"
    val description: String,
    val projectId: Int? = null,
    val projectName: String? = null,
    val paymentMethod: String = "Transfer", // Transfer, Tunai, Cek/Giro
    val referenceNumber: String = "", // Nomor Kuitansi/Invoice
    val isVoid: Boolean = false, // Audit trail: jangan hapus fisik, gunakan flag VOID
    val voidReason: String? = null,
    val createdBy: String = "Admin Keuangan",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_receivables")
data class DebtReceivableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // RECEIVABLE (Piutang) or DEBT (Utang)
    val partyName: String, // Nama Klien atau Vendor/Kreditur
    val projectId: Int? = null,
    val title: String, // e.g. "Invoice Termin 1 RSUD", "Pinjaman Modal Kerja Bank BNI"
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: String = "",
    val status: String = "UNPAID", // UNPAID, PARTIAL, PAID
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
