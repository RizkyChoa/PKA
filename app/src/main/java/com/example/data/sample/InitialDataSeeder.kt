package com.example.data.sample

import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {
    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val accountCount = database.accountDao().getAccountById(1)
        if (accountCount != null) return@withContext

        // 1. Rekening Bank & Kas
        val accounts = listOf(
            AccountEntity(1, "Bank Mandiri Utama", "BANK", "123-00-987654-1", 150_000_000.0, 150_000_000.0),
            AccountEntity(2, "Bank BCA Operasional", "BANK", "882-019-2811", 50_000_000.0, 50_000_000.0),
            AccountEntity(3, "Kas Tunai Lapangan (Petty Cash)", "CASH", "-", 10_000_000.0, 10_000_000.0)
        )
        database.accountDao().insertAll(accounts)

        // 2. Kategori Transaksi Bore Pile
        val categories = listOf(
            // Project Revenue
            CategoryEntity(1, "Uang Muka / DP Proyek", "INCOME", "PROJECT_REVENUE"),
            CategoryEntity(2, "Pembayaran Termin Proyek", "INCOME", "PROJECT_REVENUE"),
            CategoryEntity(3, "Pelunasan Kontrak Proyek", "INCOME", "PROJECT_REVENUE"),

            // Project Costs (HPP)
            CategoryEntity(4, "Material - Beton Ready Mix", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(5, "Material - Besi Tulangan & Kawat", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(6, "Material - Bentonite / Polimer Bor", "EXPENSE", "PROJECT_COST", "MATERIAL"),
            CategoryEntity(7, "Upah - Operator Rig & Mekanik Bor", "EXPENSE", "PROJECT_COST", "LABOR"),
            CategoryEntity(8, "Upah - Mandor & Tenaga Bor", "EXPENSE", "PROJECT_COST", "LABOR"),
            CategoryEntity(9, "Mobilisasi & Demobilisasi Rig Bore Pile", "EXPENSE", "PROJECT_COST", "MOBILIZATION"),
            CategoryEntity(10, "BBM Solar & Pelumas Alat Berat", "EXPENSE", "PROJECT_COST", "FUEL"),
            CategoryEntity(11, "Sewa Genset, Casing Bor & Kompresor", "EXPENSE", "PROJECT_COST", "EQUIPMENT"),
            CategoryEntity(12, "Perawatan & Mata Bor Lapangan", "EXPENSE", "PROJECT_COST", "MAINTENANCE"),
            CategoryEntity(13, "Koordinasi Lapangan & Perizinan", "EXPENSE", "PROJECT_COST", "OTHER"),

            // Operational Costs
            CategoryEntity(14, "Gaji Staff Kantor & Manajemen", "EXPENSE", "OPERATIONAL_EXPENSE"),
            CategoryEntity(15, "Listrik, Air & Internet Kantor", "EXPENSE", "OPERATIONAL_EXPENSE"),
            CategoryEntity(16, "ATK, Kebutuhan Kantor & Workshop", "EXPENSE", "OPERATIONAL_EXPENSE"),

            // Debt & Capital
            CategoryEntity(17, "Penerimaan Pinjaman Modal / Bank", "INCOME", "DEBT"),
            CategoryEntity(18, "Pembayaran Pokok Pinjaman / Utang", "EXPENSE", "DEBT"),
            CategoryEntity(19, "Penyetoran Modal Pemilik", "INCOME", "EQUITY"),
            CategoryEntity(20, "Prive / Penarikan Pemilik", "EXPENSE", "EQUITY"),

            // Internal Transfer
            CategoryEntity(21, "Mutasi Antar Kas / Bank", "TRANSFER", "INTERNAL_TRANSFER")
        )
        database.categoryDao().insertAll(categories)

        // 3. Proyek Bore Pile Bawaan
        val projects = listOf(
            ProjectEntity(
                id = 1,
                projectCode = "PRJ-2026-001",
                name = "Bore Pile Gudang Logistik Marunda",
                clientName = "PT Megah Perkasa Logistik",
                location = "Marunda, Jakarta Utara",
                startDate = "2026-09-01",
                targetDate = "2026-10-25",
                contractAmount = 180_000_000.0,
                status = "ACTIVE",
                notes = "Pengeboran diameter 60cm, kedalaman 24 meter, total 48 titik."
            ),
            ProjectEntity(
                id = 2,
                projectCode = "PRJ-2026-002",
                name = "Pondasi Bore Pile Gedung Serpong",
                clientName = "PT Cipta Graha Mandiri",
                location = "BSD City, Tangerang Selatan",
                startDate = "2026-09-15",
                targetDate = "2026-11-10",
                contractAmount = 320_000_000.0,
                status = "ACTIVE",
                notes = "Diameter 80cm kedalaman 28m, 62 titik tiang pancang bor."
            ),
            ProjectEntity(
                id = 3,
                projectCode = "PRJ-2026-003",
                name = "Bore Pile Jembatan Flyover Cibitung",
                clientName = "PT Waskita Karya Subkon",
                location = "Cibitung, Bekasi",
                startDate = "2026-07-10",
                targetDate = "2026-08-30",
                contractAmount = 250_000_000.0,
                status = "COMPLETED", // Status COMPLETED: tidak boleh muncul di dropdown pengeluaran baru!
                notes = "Proyek telah selesai 100% dan lolos uji PDA Test."
            )
        )
        database.projectDao().insertAll(projects)

        // 4. Sample Transaksi Awal Terintegrasi
        val transactions = listOf(
            // Transaksi 1: DP Proyek Marunda
            TransactionEntity(
                id = 1,
                trxNumber = "TRX-20260901-0001",
                date = "2026-09-01",
                type = "MONEY_IN",
                amount = 54_000_000.0,
                description = "Penerimaan DP 30% Bore Pile Marunda",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 1,
                categoryName = "Uang Muka / DP Proyek",
                classification = "PROJECT",
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda"
            ),
            // Transaksi 2: Mobilisasi Rig Marunda
            TransactionEntity(
                id = 2,
                trxNumber = "TRX-20260903-0002",
                date = "2026-09-03",
                type = "MONEY_OUT",
                amount = 12_500_000.0,
                description = "Sewa Tronton Mobilisasi Rig Bore Pile ke Marunda",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 9,
                categoryName = "Mobilisasi & Demobilisasi Rig Bore Pile",
                classification = "PROJECT",
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda",
                costGroup = "MOBILIZATION"
            ),
            // Transaksi 3: BBM Solar Marunda
            TransactionEntity(
                id = 3,
                trxNumber = "TRX-20260905-0003",
                date = "2026-09-05",
                type = "MONEY_OUT",
                amount = 8_200_000.0,
                description = "Solar Industri 800 Liter Rig Pengeboran Marunda",
                paymentMethod = "TRANSFER",
                sourceAccountId = 2,
                sourceAccountName = "Bank BCA Operasional",
                categoryId = 10,
                categoryName = "BBM Solar & Pelumas Alat Berat",
                classification = "PROJECT",
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda",
                costGroup = "FUEL"
            ),
            // Transaksi 4: Beton Ready Mix Marunda
            TransactionEntity(
                id = 4,
                trxNumber = "TRX-20260910-0004",
                date = "2026-09-10",
                type = "MONEY_OUT",
                amount = 22_000_000.0,
                description = "Cor Beton Ready Mix K-350 25 m3 Marunda",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 4,
                categoryName = "Material - Beton Ready Mix",
                classification = "PROJECT",
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda",
                costGroup = "MATERIAL"
            ),
            // Transaksi 5: Upah Operator Marunda
            TransactionEntity(
                id = 5,
                trxNumber = "TRX-20260915-0005",
                date = "2026-09-15",
                type = "MONEY_OUT",
                amount = 9_600_000.0,
                description = "Upah Bor Tim Operator & Kenek Marunda Tahap 1",
                paymentMethod = "CASH",
                sourceAccountId = 3,
                sourceAccountName = "Kas Tunai Lapangan (Petty Cash)",
                categoryId = 7,
                categoryName = "Upah - Operator Rig & Mekanik Bor",
                classification = "PROJECT",
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda",
                costGroup = "LABOR"
            ),
            // Transaksi 6: DP Proyek Serpong
            TransactionEntity(
                id = 6,
                trxNumber = "TRX-20260916-0006",
                date = "2026-09-16",
                type = "MONEY_IN",
                amount = 80_000_000.0,
                description = "Penerimaan DP 25% Pondasi Bore Pile Serpong",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 1,
                categoryName = "Uang Muka / DP Proyek",
                classification = "PROJECT",
                projectId = 2,
                projectName = "Pondasi Bore Pile Gedung Serpong"
            ),
            // Transaksi 7: Historis Proyek Selesai (Jembatan Flyover Cibitung)
            TransactionEntity(
                id = 7,
                trxNumber = "TRX-20260825-0007",
                date = "2026-08-25",
                type = "MONEY_IN",
                amount = 250_000_000.0,
                description = "Pelunasan 100% Bore Pile Jembatan Cibitung",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 3,
                categoryName = "Pelunasan Kontrak Proyek",
                classification = "PROJECT",
                projectId = 3,
                projectName = "Bore Pile Jembatan Flyover Cibitung"
            ),
            TransactionEntity(
                id = 8,
                trxNumber = "TRX-20260815-0008",
                date = "2026-08-15",
                type = "MONEY_OUT",
                amount = 145_000_000.0,
                description = "Total Biaya Material & Bor Cibitung (HPP)",
                paymentMethod = "TRANSFER",
                sourceAccountId = 1,
                sourceAccountName = "Bank Mandiri Utama",
                categoryId = 4,
                categoryName = "Material - Beton Ready Mix",
                classification = "PROJECT",
                projectId = 3,
                projectName = "Bore Pile Jembatan Flyover Cibitung",
                costGroup = "MATERIAL"
            )
        )
        database.transactionDao().insertAll(transactions)

        // 5. Sample Piutang & Utang
        val receivables = listOf(
            ReceivableEntity(
                id = 1,
                projectId = 1,
                projectName = "Bore Pile Gudang Logistik Marunda",
                clientName = "PT Megah Perkasa Logistik",
                invoiceNumber = "INV/MRD/2026/001",
                description = "Tagihan Termin 1 (Progres 50% Marunda)",
                totalAmount = 54_000_000.0,
                paidAmount = 0.0,
                dueDate = "2026-10-10",
                status = "UNPAID"
            )
        )
        database.receivableDao().insertAll(receivables)

        val payables = listOf(
            PayableEntity(
                id = 1,
                creditorName = "PT Holcim Semen Nusantara",
                type = "SUPPLIER",
                description = "Tagihan Pengadaan Beton Ready Mix Proyek Marunda",
                totalAmount = 35_000_000.0,
                paidAmount = 10_000_000.0,
                dueDate = "2026-10-05",
                status = "PARTIAL"
            )
        )
        database.payableDao().insertAll(payables)

        // 6. Sample Invoice Mandor / Pekerja Bore Pile
        val workerInvoice = com.example.data.model.WorkerInvoiceEntity(
            id = 1,
            invoiceNumber = "INV-WRK-202609-001",
            projectId = 1,
            projectName = "Bore Pile Gudang Logistik Marunda",
            workerLeaderName = "Mandor Syarif & Tim",
            date = "2026-09-25",
            status = "LUNAS",
            notes = "Pekerjaan pengeboran pondasi tahap 1 selesai sesuai spesifikasi."
        )
        database.workerInvoiceDao().insertInvoice(workerInvoice)

        val jobItems = listOf(
            com.example.data.model.WorkerJobItemEntity(
                id = 1,
                invoiceId = 1,
                jobName = "Bore pile diameter 40cm",
                pointCount = 50,
                depthMeters = 24.0,
                volumeMeters = 1200.0,
                unitPricePerMeter = 40000.0,
                subtotal = 48000000.0
            ),
            com.example.data.model.WorkerJobItemEntity(
                id = 2,
                invoiceId = 1,
                jobName = "Bore pile diameter 50cm",
                pointCount = 20,
                depthMeters = 29.0,
                volumeMeters = 580.0,
                unitPricePerMeter = 50000.0,
                subtotal = 29000000.0
            )
        )
        database.workerInvoiceDao().insertJobItems(jobItems)

        val loanItems = listOf(
            com.example.data.model.WorkerLoanItemEntity(
                id = 1,
                invoiceId = 1,
                date = "2026-09-21",
                description = "Pembayaran DP naik anggota 5 orang",
                trxType = "TRANSFER",
                amount = 5000000.0,
                deductionDescription = "ongkos keberangkatan",
                deductionAmount = 1500000.0
            )
        )
        database.workerInvoiceDao().insertLoanItems(loanItems)
    }
}
