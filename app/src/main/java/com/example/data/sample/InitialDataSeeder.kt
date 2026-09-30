package com.example.data.sample

import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {
    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val accountCount = database.accountDao().getAccountById(1)
        if (accountCount != null) return@withContext

        // 1. Rekening Kas Default Bersih (Saldo 0)
        val defaultAccounts = listOf(
            AccountEntity(1, "Kas Utama (Tunai)", "CASH", "-", 0.0, 0.0, true),
            AccountEntity(2, "Rekening Bank Operasional", "BANK", "-", 0.0, 0.0, true)
        )
        database.accountDao().insertAll(defaultAccounts)

        // 2. Kategori Transaksi Standar Bore Pile (Agar pengguna tidak perlu input manual)
        val categories = listOf(
            // Project Revenue
            CategoryEntity(1, "Uang Muka / DP Proyek", "INCOME", "PROJECT_REVENUE"),
            CategoryEntity(2, "Pembayaran Termin Proyek", "INCOME", "PROJECT_REVENUE"),
            CategoryEntity(3, "Pelunasan Kontrak Proyek", "INCOME", "PROJECT_REVENUE"),

            // Project Costs (HPP)
            CategoryEntity(4, "Material - Beton Ready Mix", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(5, "Material - Besi Tulangan & Kawat", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(6, "Material - Bentonite / Polimer Bor", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(7, "Upah Mandor & Supervisi Lapangan", "EXPENSE", "PROJECT_COST", "LABOR_MANDOR"),
            CategoryEntity(8, "Upah Pekerja / Operator Rig & Tenaga Bor", "EXPENSE", "PROJECT_COST", "LABOR_WORKER"),
            CategoryEntity(9, "Mobilisasi & Demobilisasi Rig Bore Pile", "EXPENSE", "PROJECT_COST", "MOBILIZATION"),
            CategoryEntity(10, "BBM Solar & Pelumas Alat Berat", "EXPENSE", "PROJECT_COST", "FUEL"),
            CategoryEntity(11, "Sewa Genset, Casing Bor & Kompresor", "EXPENSE", "PROJECT_COST", "EQUIPMENT"),
            CategoryEntity(12, "Perawatan & Mata Bor Lapangan", "EXPENSE", "PROJECT_COST", "MAINTENANCE"),
            CategoryEntity(13, "Koordinasi Lapangan & Perizinan", "EXPENSE", "PROJECT_COST", "OTHER"),

            // Operational Costs
            CategoryEntity(14, "Gaji Staff Kantor & Manajemen", "EXPENSE", "OPERATIONAL_EXPENSE"),
            CategoryEntity(15, "Listrik, Air & Internet Kantor", "EXPENSE", "OPERATIONAL_EXPENSE"),
            CategoryEntity(16, "ATK & Kebutuhan Workshop", "EXPENSE", "OPERATIONAL_EXPENSE"),

            // Debt & Capital
            CategoryEntity(17, "Penerimaan Pinjaman Modal / Bank", "INCOME", "DEBT"),
            CategoryEntity(18, "Pembayaran Pokok Pinjaman / Utang", "EXPENSE", "DEBT"),
            CategoryEntity(19, "Penyetoran Modal Pemilik", "INCOME", "EQUITY"),
            CategoryEntity(20, "Prive / Penarikan Pemilik", "EXPENSE", "EQUITY"),

            // Internal Transfer
            CategoryEntity(21, "Mutasi Antar Kas / Bank", "TRANSFER", "INTERNAL_TRANSFER")
        )
        database.categoryDao().insertAll(categories)

        // Tidak ada sample proyek, transaksi, kasbon, piutang atau utang!
        // Database 100% bersih seperti baru instal APK baru.
    }
}
