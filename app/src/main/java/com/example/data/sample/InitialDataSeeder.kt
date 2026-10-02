package com.example.data.sample

import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProfitPartnerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {
    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val accountCount = database.accountDao().getAccountById(1)
        if (accountCount == null) {
            // 1. Rekening Kas Default Bersih (Saldo 0)
            val defaultAccounts = listOf(
                AccountEntity(1, "Kas Utama (Tunai)", "CASH", "-", 0.0, 0.0, true),
                AccountEntity(2, "Rekening Bank Operasional", "BANK", "-", 0.0, 0.0, true)
            )
            database.accountDao().insertAll(defaultAccounts)
        }

        val existingCategories = database.categoryDao().getAllCategoriesSnapshot()
        if (existingCategories.isEmpty()) {
            // 2. Kategori Transaksi Standar Bore Pile
            val categories = listOf(
                // Project Revenue
                CategoryEntity(1, "Uang Muka / DP Proyek", "INCOME", "PROJECT_REVENUE"),
                CategoryEntity(2, "Pembayaran Termin Proyek", "INCOME", "PROJECT_REVENUE"),
                CategoryEntity(3, "Pelunasan Kontrak Proyek", "INCOME", "PROJECT_REVENUE"),
                CategoryEntity(4, "Pembayaran Progress Proyek", "INCOME", "PROJECT_REVENUE"),

                // Project Costs (HPP)
                CategoryEntity(5, "Material - Beton Ready Mix", "EXPENSE", "PROJECT_COST", "MATERIAL"),
                CategoryEntity(6, "Material - Besi Tulangan & Kawat", "EXPENSE", "PROJECT_COST", "MATERIAL"),
                CategoryEntity(7, "Material - Bentonite / Polimer Bor", "EXPENSE", "PROJECT_COST", "MATERIAL"),
                CategoryEntity(8, "Upah Mandor & Supervisi Lapangan", "EXPENSE", "PROJECT_COST", "LABOR_MANDOR"),
                CategoryEntity(9, "Upah Pekerja / Operator Rig & Tenaga Bor", "EXPENSE", "PROJECT_COST", "LABOR_WORKER"),
                CategoryEntity(10, "Konsumsi Anggota Bore pile", "EXPENSE", "PROJECT_COST", "CONSUMPTION"),
                CategoryEntity(11, "Mobilisasi & Demobilisasi Rig Bore Pile", "EXPENSE", "PROJECT_COST", "MOBILIZATION"),
                CategoryEntity(12, "BBM Solar & Pelumas Alat Berat", "EXPENSE", "PROJECT_COST", "FUEL"),
                CategoryEntity(13, "Sewa Genset, Casing Bor & Kompresor", "EXPENSE", "PROJECT_COST", "EQUIPMENT"),
                CategoryEntity(14, "Perawatan & Mata Bor Lapangan", "EXPENSE", "PROJECT_COST", "MAINTENANCE"),
                CategoryEntity(15, "Koordinasi Lapangan & Perizinan", "EXPENSE", "PROJECT_COST", "OTHER"),

                // Operational Costs
                CategoryEntity(16, "Gaji Staff Kantor & Manajemen", "EXPENSE", "OPERATIONAL_EXPENSE"),
                CategoryEntity(17, "Listrik, Air & Internet Kantor", "EXPENSE", "OPERATIONAL_EXPENSE"),
                CategoryEntity(18, "ATK & Kebutuhan Workshop", "EXPENSE", "OPERATIONAL_EXPENSE"),

                // Debt & Capital
                CategoryEntity(19, "Penerimaan Pinjaman Modal / Bank", "INCOME", "DEBT"),
                CategoryEntity(20, "Pembayaran Pokok Pinjaman / Utang", "EXPENSE", "DEBT"),
                CategoryEntity(21, "Penyetoran Modal Pemilik", "INCOME", "EQUITY"),
                CategoryEntity(22, "Prive / Penarikan Pemilik", "EXPENSE", "EQUITY"),

                // Pengambilan Profit / Bagi Hasil Partner
                CategoryEntity(23, "Pengambilan Profit / Bagi Hasil Partner", "EXPENSE", "PENGAMBILAN_PROFIT"),

                // Internal Transfer
                CategoryEntity(24, "Mutasi Antar Kas / Bank", "TRANSFER", "INTERNAL_TRANSFER")
            )
            database.categoryDao().insertAll(categories)
        }

        // 3. Seed Default Profit Partners jika belum ada (Sesuai contoh user: Ramlan 60%, Gunawan 20%, Rizky 20%)
        val existingPartners = database.profitPartnerDao().getAllPartnersSnapshot()
        if (existingPartners.isEmpty()) {
            val defaultPartners = listOf(
                ProfitPartnerEntity(name = "Ramlan", sharePercentage = 60.0, notes = "Partner Utama (60%)"),
                ProfitPartnerEntity(name = "Gunawan", sharePercentage = 20.0, notes = "Partner (20%)"),
                ProfitPartnerEntity(name = "Rizky", sharePercentage = 20.0, notes = "Partner (20%)")
            )
            database.profitPartnerDao().insertAll(defaultPartners)
        }

        // 4. Seed Default Active Book Period jika belum ada
        val existingPeriods = database.bookPeriodDao().getAllPeriodsSnapshot()
        if (existingPeriods.isEmpty()) {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val defaultPeriod = com.example.data.model.BookPeriodEntity(
                id = 1L,
                periodCode = "PERIOD-$currentYear-01",
                name = "Data $currentYear Pembukuan Aktif",
                year = currentYear,
                startDate = "$currentYear-01-01",
                endDate = "$currentYear-12-31",
                status = "ACTIVE",
                notes = "Periode pembukuan aktif berjalan"
            )
            database.bookPeriodDao().insertPeriod(defaultPeriod)
        }

        // 0 Sample dummy projects, transactions, invoices, utang atau piutang.
        // Database 100% bersih siap diuji langsung!
    }
}
