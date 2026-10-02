package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.model.CreditorLedger
import com.example.data.model.DailyCashSummary
import com.example.data.model.GlobalFinancialSummary
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.TransactionEntity
import com.example.data.model.WorkerInvoiceWithDetails
import com.example.data.model.formatRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintHelper {

    fun printHtml(context: Context, jobName: String, htmlContent: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Layanan Cetak (Print) tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager.print(jobName, printAdapter, attributes)
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memulai cetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 1. Print Laba Rugi per Proyek
    fun generateProjectPnLHtml(ps: ProjectFinancialSummary): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val isProfit = ps.grossProfit >= 0

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Laba Rugi Proyek - ${ps.project.name}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; }
                .header { border-bottom: 3px solid #0D2B45; padding-bottom: 12px; margin-bottom: 20px; }
                .company-name { font-size: 22px; font-weight: bold; color: #0D2B45; }
                .report-title { font-size: 16px; font-weight: 600; color: #475569; margin-top: 4px; }
                .meta-table { width: 100%; margin-bottom: 20px; font-size: 13px; }
                .meta-table td { padding: 4px 0; }
                .section-title { font-size: 14px; font-weight: bold; color: #0D2B45; margin-top: 15px; margin-bottom: 8px; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px; }
                table.data { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 13px; }
                table.data th { background-color: #f1f5f9; padding: 8px 10px; text-align: left; border-bottom: 1px solid #cbd5e1; }
                table.data td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .profit { color: #15803d; }
                .expense { color: #dc2626; }
                .highlight-box { background: ${if (isProfit) "#f0fdf4" else "#fef2f2"}; border: 1px solid ${if (isProfit) "#bbf7d0" else "#fecaca"}; border-radius: 8px; padding: 15px; margin-top: 15px; }
                .footer { margin-top: 40px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="company-name">BORE PILE FINANCE MANAGEMENT</div>
                <div class="report-title">LAPORAN LABA RUGI &amp; BIAYA PROYEK</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Dicetak pada: $today</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td width="20%"><strong>Kode Proyek</strong></td>
                    <td width="30%">: ${ps.project.projectCode}</td>
                    <td width="20%"><strong>Tanggal Mulai</strong></td>
                    <td width="30%">: ${ps.project.startDate}</td>
                </tr>
                <tr>
                    <td><strong>Nama Proyek</strong></td>
                    <td>: <strong>${ps.project.name}</strong></td>
                    <td><strong>Target Selesai</strong></td>
                    <td>: ${ps.project.targetDate}</td>
                </tr>
                <tr>
                    <td><strong>Client / Kontraktor</strong></td>
                    <td>: ${ps.project.clientName}</td>
                    <td><strong>Status Proyek</strong></td>
                    <td>: ${if (ps.project.status == "COMPLETED") "SELESAI (COMPLETED)" else "SEDANG BERJALAN"}</td>
                </tr>
                <tr>
                    <td><strong>Lokasi</strong></td>
                    <td>: ${ps.project.location}</td>
                    <td><strong>Nilai Kontrak</strong></td>
                    <td>: <strong>${formatRupiah(ps.contractAmount)}</strong></td>
                </tr>
            </table>

            <div class="section-title">1. PENDAPATAN PROYEK (CASH IN)</div>
            <table class="data">
                <tr>
                    <td>Total Pendapatan Diterima (DP + Termin)</td>
                    <td class="text-right bold profit">${formatRupiah(ps.cashReceived)}</td>
                </tr>
                <tr>
                    <td>Sisa Nilai Kontrak Belum Cair</td>
                    <td class="text-right">${formatRupiah(ps.remainingContractBalance)}</td>
                </tr>
            </table>

            <div class="section-title">2. RINCIAN BIAYA POKOK PROYEK (HPP BORE PILE)</div>
            <table class="data">
                <thead>
                    <tr>
                        <th>Komponen Biaya Lapangan</th>
                        <th class="text-right">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td>Material (Beton Ready Mix &amp; Besi Tulangan)</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.material)}</td></tr>
                    <tr style="background:#f1f5f9;"><td colspan="2" style="font-weight:bold; color:#0D2B45; font-size:12px;">RINCIAN KATEGORI UPAH (TERPISAH):</td></tr>
                    <tr><td style="padding-left:16px;">1. <strong>Upah Mandor &amp; Supervisi Lapangan</strong></td><td class="text-right expense bold">${formatRupiah(ps.costBreakdown.laborMandor)}</td></tr>
                    <tr><td style="padding-left:16px;">2. <strong>Upah Pekerja / Operator Rig &amp; Tenaga Bor</strong></td><td class="text-right expense bold">${formatRupiah(ps.costBreakdown.laborWorker)}</td></tr>
                    <tr style="font-style:italic; background:#f8fafc;"><td style="padding-left:16px;">Subtotal Seluruh Upah Tenaga Kerja</td><td class="text-right expense bold">${formatRupiah(ps.costBreakdown.laborTotal)}</td></tr>
                    <tr><td>Mobilisasi &amp; Demobilisasi Rig Bore Pile</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.mobilization)}</td></tr>
                    <tr><td>BBM Solar &amp; Pelumas Alat Berat</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.fuel)}</td></tr>
                    <tr><td>Sewa Alat Tambahan (Genset, Casing Bor &amp; Kompresor)</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.equipment)}</td></tr>
                    <tr><td>Perawatan, Servis &amp; Penggantian Mata Bor</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.maintenance)}</td></tr>
                    <tr><td>Koordinasi Lapangan, Keamanan &amp; Perizinan</td><td class="text-right expense">${formatRupiah(ps.costBreakdown.other)}</td></tr>
                    <tr style="background:#f8fafc; font-weight:bold;">
                        <td>TOTAL BIAYA HPP PROYEK</td>
                        <td class="text-right expense">${formatRupiah(ps.totalCost)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="highlight-box">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <div style="font-size: 13px; font-weight: bold; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${if (isProfit) "LABA KOTOR PROYEK (GROSS PROFIT)" else "RUGI PROYEK"}
                            </div>
                            <div style="font-size: 20px; font-weight: 800; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${formatRupiah(ps.grossProfit)}
                            </div>
                        </td>
                        <td style="text-align: right; vertical-align: middle;">
                            <div style="font-size: 13px; color: #475569;">Persentase Margin Laba:</div>
                            <div style="font-size: 18px; font-weight: bold; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${String.format(Locale.US, "%.1f%%", ps.marginPercent)}
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 2. Print Rekap Arus Kas
    fun generateCashFlowHtml(summary: GlobalFinancialSummary, dailySummaries: List<DailyCashSummary>): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val dailyRows = StringBuilder()
        for (d in dailySummaries) {
            dailyRows.append(
                """
                <tr>
                    <td class="bold">${d.date}</td>
                    <td class="text-right profit">${formatRupiah(d.totalIn)}</td>
                    <td class="text-right expense">${formatRupiah(d.totalOut)}</td>
                    <td class="text-right bold ${if (d.netFlow >= 0) "profit" else "expense"}">
                        ${if (d.netFlow >= 0) "+" else ""}${formatRupiah(d.netFlow)}
                    </td>
                    <td>${d.transactions.size} transaksi</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Laporan Arus Kas</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; }
                .header { border-bottom: 3px solid #0D2B45; padding-bottom: 12px; margin-bottom: 20px; }
                .company-name { font-size: 22px; font-weight: bold; color: #0D2B45; }
                .report-title { font-size: 16px; font-weight: 600; color: #475569; margin-top: 4px; }
                .summary-grid { display: flex; width: 100%; margin-bottom: 20px; }
                table.data { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px; }
                table.data th { background-color: #0D2B45; color: white; padding: 8px 10px; text-align: left; }
                table.data td { padding: 8px 10px; border-bottom: 1px solid #e2e8f0; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .profit { color: #15803d; }
                .expense { color: #dc2626; }
                .footer { margin-top: 40px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="company-name">BORE PILE FINANCE MANAGEMENT</div>
                <div class="report-title">LAPORAN ARUS KAS &amp; REKAP HARIAN (CASH FLOW)</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Dicetak pada: $today</div>
            </div>

            <table style="width: 100%; margin-bottom: 20px; font-size: 13px; background: #f8fafc; padding: 12px; border-radius: 8px;">
                <tr>
                    <td><strong>Total Saldo Kas &amp; Bank</strong></td>
                    <td class="text-right bold" style="font-size: 16px; color: #0D2B45;">${formatRupiah(summary.totalCash)}</td>
                </tr>
                <tr>
                    <td>Total Penerimaan Kas (Uang Masuk)</td>
                    <td class="text-right bold profit">${formatRupiah(summary.totalCashIn)}</td>
                </tr>
                <tr>
                    <td>Total Pengeluaran Kas (Uang Keluar)</td>
                    <td class="text-right bold expense">${formatRupiah(summary.totalCashOut)}</td>
                </tr>
                <tr style="border-top: 1px solid #cbd5e1;">
                    <td><strong>Net Cash Flow Berjalan</strong></td>
                    <td class="text-right bold ${if (summary.netCashFlow >= 0) "profit" else "expense"}">
                        ${formatRupiah(summary.netCashFlow)}
                    </td>
                </tr>
            </table>

            <div style="font-weight: bold; font-size: 14px; margin-top: 15px; color: #0D2B45;">REKAP ARUS KAS HARIAN</div>
            <table class="data">
                <thead>
                    <tr>
                        <th>Tanggal</th>
                        <th class="text-right">Total Masuk</th>
                        <th class="text-right">Total Keluar</th>
                        <th class="text-right">Net Cash Flow</th>
                        <th>Keterangan</th>
                    </tr>
                </thead>
                <tbody>
                    $dailyRows
                </tbody>
            </table>

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 3. Print Daftar Transaksi
    fun generateTransactionsHtml(transactions: List<TransactionEntity>): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val rows = StringBuilder()
        for (t in transactions) {
            val isVoid = t.status == "VOID"
            val isIn = t.type == "MONEY_IN"
            val isTransfer = t.type == "TRANSFER"
            val color = if (isVoid) "#94a3b8" else if (isTransfer) "#2563eb" else if (isIn) "#15803d" else "#dc2626"
            val prefix = if (isTransfer) "" else if (isIn) "+" else "-"

            rows.append(
                """
                <tr style="${if (isVoid) "background:#f8fafc; text-decoration: line-through;" else ""}">
                    <td><strong>${t.trxNumber}</strong><br><small>${t.date}</small></td>
                    <td>${t.description}${if (isVoid) "<br><small style='color:red;'>VOID: ${t.voidReason}</small>" else ""}</td>
                    <td>${t.projectName ?: "-"}</td>
                    <td>${t.sourceAccountName}</td>
                    <td>${t.paymentMethod}</td>
                    <td class="text-right bold" style="color: $color;">
                        $prefix${formatRupiah(t.amount)}
                    </td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Laporan Seluruh Transaksi</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; }
                .header { border-bottom: 3px solid #0D2B45; padding-bottom: 12px; margin-bottom: 20px; }
                .company-name { font-size: 22px; font-weight: bold; color: #0D2B45; }
                .report-title { font-size: 16px; font-weight: 600; color: #475569; margin-top: 4px; }
                table.data { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px; }
                table.data th { background-color: #0D2B45; color: white; padding: 8px 10px; text-align: left; }
                table.data td { padding: 8px 10px; border-bottom: 1px solid #e2e8f0; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .footer { margin-top: 40px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="company-name">BORE PILE FINANCE MANAGEMENT</div>
                <div class="report-title">BUKU CATATAN TRANSAKSI KEUANGAN</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Dicetak pada: $today • Total ${transactions.size} transaksi</div>
            </div>

            <table class="data">
                <thead>
                    <tr>
                        <th>No. Trx &amp; Tanggal</th>
                        <th>Keterangan / Deskripsi</th>
                        <th>Proyek</th>
                        <th>Rekening</th>
                        <th>Metode</th>
                        <th class="text-right">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 4. Print Invoice Pekerja / Mandor Per Proyek (SESUAI REQUEST USER)
    fun generateWorkerInvoiceHtml(invoiceWithDetails: WorkerInvoiceWithDetails): String {
        val inv = invoiceWithDetails.invoice
        val today = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

        val jobRows = StringBuilder()
        for ((idx, job) in invoiceWithDetails.jobItems.withIndex()) {
            jobRows.append(
                """
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>${job.jobName}</strong></td>
                    <td class="text-right">${job.pointCount} titik</td>
                    <td class="text-right">${job.depthMeters} m</td>
                    <td class="text-right bold">${job.volumeMeters} m'</td>
                    <td class="text-right">${formatRupiah(job.unitPricePerMeter)}</td>
                    <td class="text-right bold">${formatRupiah(job.subtotal)}</td>
                </tr>
                """.trimIndent()
            )
        }

        val loanRows = StringBuilder()
        if (invoiceWithDetails.loanItems.isEmpty()) {
            loanRows.append("""<tr><td colspan="6" style="text-align:center; color:#94a3b8;">Tidak ada kasbon / pinjaman tercatat.</td></tr>""")
        } else {
            for ((idx, loan) in invoiceWithDetails.loanItems.withIndex()) {
                val netLoan = loan.amount - loan.deductionAmount
                loanRows.append(
                    """
                    <tr>
                        <td>${idx + 1}</td>
                        <td>${loan.date}</td>
                        <td>${loan.description}</td>
                        <td>${loan.trxType}</td>
                        <td class="text-right">${formatRupiah(loan.amount)}</td>
                        <td class="text-right" style="color: #64748b;">
                            ${if (loan.deductionAmount > 0) "${formatRupiah(loan.deductionAmount)} (${loan.deductionDescription})" else "-"}
                        </td>
                        <td class="text-right bold" style="color: #dc2626;">${formatRupiah(netLoan)}</td>
                    </tr>
                    """.trimIndent()
                )
            }
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Invoice Pekerja - ${inv.workerLeaderName}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; }
                .header { border-bottom: 3px solid #0D2B45; padding-bottom: 12px; margin-bottom: 18px; }
                .company-name { font-size: 20px; font-weight: bold; color: #0D2B45; }
                .report-title { font-size: 15px; font-weight: bold; color: #C97A13; margin-top: 2px; }
                .meta-table { width: 100%; margin-bottom: 18px; font-size: 13px; }
                .meta-table td { padding: 4px 0; }
                .section-header { font-size: 13px; font-weight: bold; color: #0D2B45; margin-top: 18px; margin-bottom: 6px; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px; }
                table.data { width: 100%; border-collapse: collapse; margin-bottom: 12px; font-size: 12px; }
                table.data th { background-color: #f1f5f9; padding: 7px 8px; text-align: left; border-bottom: 1px solid #cbd5e1; font-weight: bold; }
                table.data td { padding: 7px 8px; border-bottom: 1px solid #f1f5f9; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .summary-box { background: #f8fafc; border: 2px solid #0D2B45; border-radius: 8px; padding: 14px; margin-top: 20px; }
                .sign-container { margin-top: 35px; width: 100%; font-size: 12px; }
                .sign-box { width: 30%; text-align: center; float: left; }
                .sign-space { height: 60px; }
                .footer { clear: both; margin-top: 50px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 8px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="company-name">BORE PILE FINANCE MANAGEMENT</div>
                <div class="report-title">INVOICE &amp; SPK PEKERJA BORE PILE</div>
                <div style="font-size: 11px; color: #64748b; margin-top: 2px;">No. Dokumen: <strong>${inv.invoiceNumber}</strong></div>
            </div>

            <table class="meta-table">
                <tr>
                    <td width="20%"><strong>Proyek</strong></td>
                    <td width="40%">: <strong>${inv.projectName}</strong></td>
                    <td width="18%"><strong>Tanggal Invoice</strong></td>
                    <td width="22%">: ${inv.date}</td>
                </tr>
                <tr>
                    <td><strong>Mandor / Pekerja</strong></td>
                    <td>: <strong>${inv.workerLeaderName}</strong></td>
                    <td><strong>Status</strong></td>
                    <td>: <span style="background:#dcfce7; color:#15803d; padding:2px 6px; border-radius:4px; font-weight:bold;">${inv.status}</span></td>
                </tr>
                <tr>
                    <td><strong>Catatan</strong></td>
                    <td colspan="3">: ${inv.notes.ifBlank { "-" }}</td>
                </tr>
            </table>

            <div class="section-header">A. TABEL HASIL PEKERJAAN BORE PILE</div>
            <table class="data">
                <thead>
                    <tr>
                        <th width="5%">No</th>
                        <th width="35%">Pekerjaan</th>
                        <th width="12%" class="text-right">Titik</th>
                        <th width="12%" class="text-right">Kedalaman</th>
                        <th width="12%" class="text-right">Volume</th>
                        <th width="12%" class="text-right">Harga / m'</th>
                        <th width="12%" class="text-right">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    $jobRows
                    <tr style="background:#f1f5f9; font-weight:bold;">
                        <td colspan="6">TOTAL PENGHASILAN PEKERJA</td>
                        <td class="text-right" style="color: #15803d; font-size: 13px;">${formatRupiah(invoiceWithDetails.totalEarnings)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="section-header">B. TABEL KASBON / PINJAMAN PEKERJA</div>
            <table class="data">
                <thead>
                    <tr>
                        <th width="4%">No</th>
                        <th width="14%">Tanggal</th>
                        <th width="30%">Keterangan</th>
                        <th width="10%">Jenis</th>
                        <th width="14%" class="text-right">Nominal</th>
                        <th width="14%" class="text-right">Potongan (Opsional)</th>
                        <th width="14%" class="text-right">Net Kasbon</th>
                    </tr>
                </thead>
                <tbody>
                    $loanRows
                    <tr style="background:#f1f5f9; font-weight:bold;">
                        <td colspan="6">TOTAL KASBON / PINJAMAN PEKERJA</td>
                        <td class="text-right" style="color: #dc2626; font-size: 13px;">${formatRupiah(invoiceWithDetails.totalLoans)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="summary-box">
                <table style="width: 100%; font-size: 13px;">
                    <tr>
                        <td>Total Penghasilan Borongan</td>
                        <td class="text-right bold" style="color: #15803d;">${formatRupiah(invoiceWithDetails.totalEarnings)}</td>
                    </tr>
                    <tr>
                        <td>Total Kasbon / Pinjaman &amp; Uang Muka</td>
                        <td class="text-right bold" style="color: #dc2626;">-${formatRupiah(invoiceWithDetails.totalLoans)}</td>
                    </tr>
                    <tr style="border-top: 2px solid #0D2B45; font-size: 15px;">
                        <td style="padding-top: 8px;"><strong>SISA HASIL PROYEK / UPAH BERSIH DITERIMA</strong></td>
                        <td class="text-right bold" style="padding-top: 8px; color: #0D2B45; font-size: 17px;">
                            ${formatRupiah(invoiceWithDetails.remainingBalance)}
                        </td>
                    </tr>
                </table>
            </div>

            <div class="sign-container">
                <div class="sign-box">
                    <div>Penerima / Mandor,</div>
                    <div class="sign-space"></div>
                    <div>( <strong>${inv.workerLeaderName}</strong> )</div>
                </div>
                <div class="sign-box" style="margin-left: 5%;">
                    <div>Diperiksa Oleh (Pelaksana),</div>
                    <div class="sign-space"></div>
                    <div>( ..................................... )</div>
                </div>
                <div class="sign-box" style="float: right;">
                    <div>Disetujui (Pimpinan/Owner),</div>
                    <div class="sign-space"></div>
                    <div>( ..................................... )</div>
                </div>
            </div>

            <div class="footer">
                Invoice ini sah dan dikeluarkan secara resmi oleh Sistem Aplikasi Manajemen Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 5. Print Laba Rugi Perusahaan (Global Company P&L) dengan Pemisahan Upah Mandor vs Pekerja
    fun generateCompanyPnLHtml(summary: GlobalFinancialSummary): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val isProfit = summary.netProfit >= 0
        val netMargin = if (summary.totalRevenue > 0) (summary.netProfit / summary.totalRevenue) * 100 else 0.0
        val grossProfit = summary.totalRevenue - summary.totalProjectCost

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Laba Rugi Perusahaan</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; }
                .header { border-bottom: 3px solid #0D2B45; padding-bottom: 12px; margin-bottom: 20px; }
                .company-name { font-size: 22px; font-weight: bold; color: #0D2B45; }
                .report-title { font-size: 16px; font-weight: 600; color: #475569; margin-top: 4px; }
                table.data { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 13px; }
                table.data th { background-color: #f1f5f9; padding: 8px 10px; text-align: left; border-bottom: 1px solid #cbd5e1; }
                table.data td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .profit { color: #15803d; }
                .expense { color: #dc2626; }
                .highlight-box { background: ${if (isProfit) "#f0fdf4" else "#fef2f2"}; border: 1px solid ${if (isProfit) "#bbf7d0" else "#fecaca"}; border-radius: 8px; padding: 15px; margin-top: 15px; }
                .footer { margin-top: 40px; font-size: 11px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 10px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="company-name">BORE PILE FINANCE MANAGEMENT</div>
                <div class="report-title">LAPORAN LABA RUGI PERUSAHAAN (COMPANY PROFIT &amp; LOSS)</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Dicetak pada: $today</div>
            </div>

            <div style="font-size: 14px; font-weight: bold; color: #0D2B45; margin-bottom: 8px;">1. PENDAPATAN PROYEK (REVENUE)</div>
            <table class="data">
                <tr>
                    <td>Pendapatan Proyek Bore Pile (DP, Termin &amp; Pelunasan Kontrak)</td>
                    <td class="text-right bold profit">${formatRupiah(summary.totalRevenue)}</td>
                </tr>
                <tr style="background: #f8fafc; font-weight: bold;">
                    <td>TOTAL PENDAPATAN OPERASIONAL</td>
                    <td class="text-right profit">${formatRupiah(summary.totalRevenue)}</td>
                </tr>
            </table>

            <div style="font-size: 14px; font-weight: bold; color: #0D2B45; margin-top: 15px; margin-bottom: 8px;">2. BIAYA POKOK PROYEK / HPP BORE PILE</div>
            <table class="data">
                <thead>
                    <tr>
                        <th>Komponen Biaya Proyek</th>
                        <th class="text-right">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td>• Material (Ready Mix, Besi Tulangan &amp; Bentonite)</td><td class="text-right expense">${formatRupiah(summary.totalMaterial)}</td></tr>
                    <tr style="background:#f1f5f9;"><td colspan="2" style="font-weight:bold; color:#0D2B45; font-size:12px;">RINCIAN KATEGORI UPAH (TERPISAH):</td></tr>
                    <tr><td style="padding-left:16px;">1. <strong>Upah Mandor &amp; Supervisi Lapangan</strong></td><td class="text-right expense bold">${formatRupiah(summary.totalLaborMandor)}</td></tr>
                    <tr><td style="padding-left:16px;">2. <strong>Upah Pekerja / Operator Rig &amp; Tenaga Bor</strong></td><td class="text-right expense bold">${formatRupiah(summary.totalLaborWorker)}</td></tr>
                    <tr style="font-style:italic; background:#f8fafc;"><td style="padding-left:16px;">Subtotal Seluruh Upah Tenaga Kerja</td><td class="text-right expense bold">${formatRupiah(summary.totalLaborMandor + summary.totalLaborWorker)}</td></tr>
                    <tr><td>• Mobilisasi &amp; Demobilisasi Rig Bore Pile</td><td class="text-right expense">${formatRupiah(summary.totalMobilization)}</td></tr>
                    <tr><td>• BBM Solar &amp; Pelumas Alat Berat</td><td class="text-right expense">${formatRupiah(summary.totalFuel)}</td></tr>
                    <tr><td>• Sewa Alat, Genset &amp; Casing Bor</td><td class="text-right expense">${formatRupiah(summary.totalEquipment)}</td></tr>
                    <tr><td>• Perawatan &amp; Mata Bor Lapangan</td><td class="text-right expense">${formatRupiah(summary.totalMaintenance)}</td></tr>
                    <tr><td>• Koordinasi Lapangan &amp; Perizinan</td><td class="text-right expense">${formatRupiah(summary.totalOtherCost)}</td></tr>
                    <tr style="background:#f8fafc; font-weight:bold;">
                        <td>TOTAL BIAYA HPP PROYEK</td>
                        <td class="text-right expense">${formatRupiah(summary.totalProjectCost)}</td>
                    </tr>
                    <tr style="background:#f1f5f9; font-weight:bold;">
                        <td>LABA KOTOR (GROSS PROFIT)</td>
                        <td class="text-right ${if (grossProfit >= 0) "profit" else "expense"}">${formatRupiah(grossProfit)}</td>
                    </tr>
                </tbody>
            </table>

            <div style="font-size: 14px; font-weight: bold; color: #0D2B45; margin-top: 15px; margin-bottom: 8px;">3. BIAYA OPERASIONAL &amp; UMUM</div>
            <table class="data">
                <tr>
                    <td>Beban Operasional Kantor / Workshop / Umum</td>
                    <td class="text-right expense">${formatRupiah(summary.totalOperationalCost)}</td>
                </tr>
                <tr style="background:#f8fafc; font-weight:bold;">
                    <td>TOTAL BIAYA &amp; BEBAN PERUSAHAAN</td>
                    <td class="text-right expense">${formatRupiah(summary.totalExpense)}</td>
                </tr>
            </table>

            <div class="highlight-box">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <div style="font-size: 13px; font-weight: bold; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${if (isProfit) "LABA BERSIH PERUSAHAAN (NET PROFIT)" else "RUGI BERSIH PERUSAHAAN"}
                            </div>
                            <div style="font-size: 22px; font-weight: 800; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${formatRupiah(summary.netProfit)}
                            </div>
                        </td>
                        <td style="text-align: right; vertical-align: middle;">
                            <div style="font-size: 13px; color: #475569;">Net Profit Margin:</div>
                            <div style="font-size: 18px; font-weight: bold; color: ${if (isProfit) "#15803d" else "#dc2626"};">
                                ${String.format(Locale.US, "%.1f%%", netMargin)}
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 5. Print Laporan Pembagian Hasil Profit
    fun generateProfitShareHtml(
        projectSummaries: List<ProjectFinancialSummary>,
        partners: List<com.example.data.model.ProfitPartnerEntity>,
        transactions: List<TransactionEntity>
    ): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val totalGrossProfit = projectSummaries.sumOf { it.grossProfit }

        val partnerCardsHtml = partners.joinToString("") { partner ->
            val grossShare = totalGrossProfit * (partner.sharePercentage / 100.0)
            val deductions = transactions.filter {
                it.status == "VALID" &&
                it.classification == "PENGAMBILAN_PROFIT" &&
                (it.profitPartnerId == partner.id || it.profitPartnerName.equals(partner.name, ignoreCase = true))
            }.sumOf { it.amount }
            val netShare = grossShare - deductions

            val perProjectRows = projectSummaries.joinToString("") { ps ->
                val prjGross = ps.grossProfit
                val prjShare = prjGross * (partner.sharePercentage / 100.0)
                """
                <tr>
                    <td style="padding: 6px 10px; border-bottom: 1px solid #f1f5f9;">${ps.project.name}</td>
                    <td style="padding: 6px 10px; text-align: right; border-bottom: 1px solid #f1f5f9;">${formatRupiah(prjGross)}</td>
                    <td style="padding: 6px 10px; text-align: right; font-weight: bold; border-bottom: 1px solid #f1f5f9; color: #1e3a8a;">${formatRupiah(prjShare)}</td>
                </tr>
                """.trimIndent()
            }

            """
            <div style="background: #ffffff; border: 1px solid #cbd5e1; border-radius: 8px; padding: 16px; margin-bottom: 16px;">
                <table style="width: 100%; margin-bottom: 10px;">
                    <tr>
                        <td>
                            <h3 style="margin: 0; color: #0f172a; font-size: 16px;">${partner.name}</h3>
                            <span style="font-size: 12px; color: #64748b;">Porsi Bagian: <b>${partner.sharePercentage.toInt()}%</b></span>
                        </td>
                        <td style="text-align: right;">
                            <div style="font-size: 12px; color: #64748b;">Sisa Bersih Profit Diterima:</div>
                            <div style="font-size: 18px; font-weight: 800; color: #15803d;">${formatRupiah(netShare)}</div>
                        </td>
                    </tr>
                </table>

                <table style="width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 10px;">
                    <thead>
                        <tr style="background: #f8fafc; color: #475569; text-align: left;">
                            <th style="padding: 6px 10px;">Proyek</th>
                            <th style="padding: 6px 10px; text-align: right;">Laba Kotor Proyek</th>
                            <th style="padding: 6px 10px; text-align: right;">Jatah (${partner.sharePercentage.toInt()}%)</th>
                        </tr>
                    </thead>
                    <tbody>
                        $perProjectRows
                    </tbody>
                </table>

                <table style="width: 100%; font-size: 12px; background: #f8fafc; padding: 8px; border-radius: 6px;">
                    <tr>
                        <td>Subtotal Jatah Kotor:</td>
                        <td style="text-align: right; font-weight: bold;">${formatRupiah(grossShare)}</td>
                    </tr>
                    <tr>
                        <td style="color: #dc2626;">Pengambilan Profit (Dipotong):</td>
                        <td style="text-align: right; font-weight: bold; color: #dc2626;">-${formatRupiah(deductions)}</td>
                    </tr>
                    <tr style="border-top: 1px solid #cbd5e1; font-weight: bold;">
                        <td style="padding-top: 4px; color: #15803d;">SISA PROFIT BERSIH:</td>
                        <td style="padding-top: 4px; text-align: right; color: #15803d; font-size: 14px;">${formatRupiah(netShare)}</td>
                    </tr>
                </table>
            </div>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Laporan Pembagian Hasil Profit</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; background: #f8fafc; }
                .header { border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 16px; }
                .header h1 { margin: 0; color: #0f172a; font-size: 20px; text-transform: uppercase; }
                .total-banner { background: #1e3a8a; color: white; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
                .footer { font-size: 10px; color: #94a3b8; text-align: center; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Laporan Pembagian Hasil Profit Partner</h1>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Tanggal Cetak: $today</div>
            </div>

            <div class="total-banner">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <div style="font-size: 13px; opacity: 0.9;">TOTAL LABA KOTOR SELURUH PROYEK:</div>
                            <div style="font-size: 24px; font-weight: 800;">${formatRupiah(totalGrossProfit)}</div>
                        </td>
                        <td style="text-align: right; vertical-align: middle;">
                            <div style="font-size: 13px; opacity: 0.9;">Jumlah Mitra: ${partners.size} Orang</div>
                        </td>
                    </tr>
                </table>
            </div>

            $partnerCardsHtml

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    // 7. Print Detail Buku Besar Hutang / Pinjaman per Pemberi Pinjaman
    fun generateCreditorLedgerHtml(ledger: CreditorLedger): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val statusText = if (ledger.isSettled) "LUNAS" else "BELUM LUNAS"
        val statusColor = if (ledger.isSettled) "#16a34a" else "#dc2626"

        val loansRowsHtml = if (ledger.loans.isEmpty()) {
            "<tr><td colspan='5' style='text-align:center; color:#94a3b8;'>Belum ada data pinjaman</td></tr>"
        } else {
            ledger.loans.mapIndexed { index, loan ->
                """
                <tr>
                    <td style="text-align: center;">${index + 1}</td>
                    <td>${loan.date}</td>
                    <td><span style="background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 11px;">${loan.type}</span></td>
                    <td>${loan.description}</td>
                    <td style="text-align: right; font-weight: 600;">${formatRupiah(loan.amount)}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        val repaymentsRowsHtml = if (ledger.repayments.isEmpty()) {
            "<tr><td colspan='4' style='text-align:center; color:#94a3b8;'>Belum ada riwayat pembayaran / pelunasan</td></tr>"
        } else {
            ledger.repayments.mapIndexed { index, rep ->
                """
                <tr>
                    <td style="text-align: center;">${index + 1}</td>
                    <td>${rep.date}</td>
                    <td>${rep.description} (${rep.accountName} - ${rep.paymentMethod})</td>
                    <td style="text-align: right; font-weight: 600; color: #dc2626;">${formatRupiah(rep.amount)}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Ledger Hutang - ${ledger.creditorName}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; background: #fff; }
                .header { border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 16px; }
                .header h1 { margin: 0; color: #0f172a; font-size: 20px; text-transform: uppercase; }
                .badge { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
                .summary-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 13px; }
                th { background: #f1f5f9; color: #475569; padding: 8px; text-align: left; border-bottom: 2px solid #cbd5e1; }
                td { padding: 8px; border-bottom: 1px solid #e2e8f0; }
                .section-title { font-size: 14px; font-weight: bold; margin-top: 18px; margin-bottom: 6px; color: #0f172a; }
                .footer { font-size: 10px; color: #94a3b8; text-align: center; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="header">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <h1>Buku Besar Hutang / Pinjaman</h1>
                            <div style="font-size: 14px; font-weight: bold; color: #2563eb; margin-top: 4px;">Pihak / Kreditur: ${ledger.creditorName}</div>
                            <div style="font-size: 12px; color: #64748b;">Kelas: ${ledger.classes.joinToString(", ")}</div>
                        </td>
                        <td style="text-align: right; vertical-align: top;">
                            <span class="badge" style="background: ${statusColor}20; color: $statusColor; border: 1px solid $statusColor;">$statusText</span>
                            <div style="font-size: 11px; color: #64748b; margin-top: 6px;">Dicetak: $today</div>
                            ${if (ledger.isSettled && ledger.lastSettlementDate != null) "<div style='font-size: 11px; font-weight: bold; color: #16a34a; margin-top: 2px;'>Pelunasan Terakhir: " + ledger.lastSettlementDate + "</div>" else ""}
                        </td>
                    </tr>
                </table>
            </div>

            <div class="summary-card">
                <table style="width: 100%; font-size: 13px;">
                    <tr>
                        <td>
                            <div style="color: #64748b; font-size: 11px;">TOTAL PINJAMAN / UTANG:</div>
                            <div style="font-size: 16px; font-weight: 800; color: #0f172a;">${formatRupiah(ledger.totalLoanAmount)}</div>
                        </td>
                        <td style="text-align: center;">
                            <div style="color: #64748b; font-size: 11px;">TOTAL PEMBAYARAN:</div>
                            <div style="font-size: 16px; font-weight: 800; color: #16a34a;">${formatRupiah(ledger.totalRepaymentAmount)}</div>
                        </td>
                        <td style="text-align: right;">
                            <div style="color: #64748b; font-size: 11px;">SISA HUTANG:</div>
                            <div style="font-size: 18px; font-weight: 800; color: ${if (ledger.isSettled) "#16a34a" else "#dc2626"};">${formatRupiah(ledger.remainingBalance)}</div>
                        </td>
                    </tr>
                </table>
            </div>

            <div class="section-title">A. DETAIL PINJAMAN / UTANG DITERIMA</div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 40px; text-align: center;">No</th>
                        <th style="width: 100px;">Tanggal</th>
                        <th style="width: 140px;">Kelas</th>
                        <th>Keterangan</th>
                        <th style="width: 130px; text-align: right;">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    $loansRowsHtml
                    <tr style="background: #f8fafc; font-weight: bold;">
                        <td colspan="4" style="text-align: right;">Subtotal Pinjaman:</td>
                        <td style="text-align: right; color: #0f172a;">${formatRupiah(ledger.totalLoanAmount)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="section-title">B. RIWAYAT PELUNASAN / PEMBAYARAN</div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 40px; text-align: center;">No</th>
                        <th style="width: 100px;">Tanggal</th>
                        <th>Keterangan & Sumber Pembayaran</th>
                        <th style="width: 130px; text-align: right;">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    $repaymentsRowsHtml
                    <tr style="background: #f8fafc; font-weight: bold;">
                        <td colspan="3" style="text-align: right;">Total Pembayaran:</td>
                        <td style="text-align: right; color: #16a34a;">${formatRupiah(ledger.totalRepaymentAmount)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="section-title" style="margin-top: 24px;">C. PERHITUNGAN SISA HUTANG</div>
            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; font-size: 13px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                    <span>Total Pinjaman:</span>
                    <span style="font-weight: bold;">${formatRupiah(ledger.totalLoanAmount)}</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                    <span>Total Pembayaran:</span>
                    <span style="font-weight: bold; color: #16a34a;">- ${formatRupiah(ledger.totalRepaymentAmount)}</span>
                </div>
                <hr style="border: 0; border-top: 1px solid #cbd5e1; margin: 6px 0;">
                <div style="display: flex; justify-content: space-between; font-size: 15px; font-weight: bold;">
                    <span>Sisa Hutang:</span>
                    <span style="color: ${if (ledger.isSettled) "#16a34a" else "#dc2626"};">${formatRupiah(ledger.remainingBalance)}</span>
                </div>
                <div style="margin-top: 6px; font-size: 12px; color: #64748b;">
                    Status: <strong style="color: $statusColor;">$statusText</strong>
                    ${if (ledger.isSettled && ledger.lastSettlementDate != null) " &bull; Tanggal Pelunasan Terakhir: <strong>" + ledger.lastSettlementDate + "</strong>" else ""}
                </div>
            </div>

            <div class="footer">
                Dokumen ini dicetak otomatis dari Sistem Aplikasi Manajemen Keuangan Bore Pile.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generatePeriodArchiveHtml(
        period: com.example.data.model.BookPeriodEntity,
        summary: com.example.data.model.GlobalFinancialSummary,
        transactions: List<com.example.data.model.TransactionEntity>
    ): String {
        val today = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val statusText = if (period.isArchived) "ARSIP (CLOSED)" else "PERIODE AKTIF"
        val statusColor = if (period.isArchived) "#64748b" else "#16a34a"

        val rowsHtml = if (transactions.isEmpty()) {
            "<tr><td colspan='6' style='text-align:center; color:#94a3b8;'>Tidak ada transaksi dalam periode ini</td></tr>"
        } else {
            transactions.take(50).mapIndexed { idx, trx ->
                val amtColor = if (trx.type == "MONEY_IN") "#16a34a" else "#dc2626"
                val prefix = if (trx.type == "MONEY_IN") "+" else "-"
                """
                <tr>
                    <td style="text-align: center;">${idx + 1}</td>
                    <td>${trx.date}</td>
                    <td><strong>${trx.trxNumber}</strong></td>
                    <td>${trx.description}</td>
                    <td><span style="font-size:11px; background:#f1f5f9; padding:2px 6px; border-radius:4px;">${trx.classification}</span></td>
                    <td style="text-align: right; font-weight: bold; color: $amtColor;">$prefix ${formatRupiah(trx.amount)}</td>
                </tr>
                """.trimIndent()
            }.joinToString("\n")
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Arsip Pembukuan - ${period.name}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 25px; color: #1e293b; background: #fff; }
                .header { border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 16px; }
                .header h1 { margin: 0; color: #0f172a; font-size: 20px; }
                .badge { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
                .card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px; }
                .card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; }
                .card-title { font-size: 11px; color: #64748b; font-weight: bold; text-transform: uppercase; }
                .card-value { font-size: 16px; font-weight: 800; margin-top: 4px; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 12px; }
                th { background: #f1f5f9; color: #475569; padding: 8px; text-align: left; border-bottom: 2px solid #cbd5e1; }
                td { padding: 8px; border-bottom: 1px solid #e2e8f0; }
                .footer { font-size: 10px; color: #94a3b8; text-align: center; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="header">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <h1>LAPORAN ARSIP PEMBUKUAN PKA</h1>
                            <div style="font-size: 15px; font-weight: bold; color: #2563eb; margin-top: 4px;">${period.name}</div>
                            <div style="font-size: 12px; color: #64748b;">Rentang Periode: ${period.startDate} s/d ${period.endDate}</div>
                        </td>
                        <td style="text-align: right; vertical-align: top;">
                            <span class="badge" style="background: ${statusColor}20; color: $statusColor; border: 1px solid $statusColor;">$statusText</span>
                            <div style="font-size: 11px; color: #64748b; margin-top: 6px;">Dicetak: $today</div>
                        </td>
                    </tr>
                </table>
            </div>

            <div class="card-grid">
                <div class="card">
                    <div class="card-title">Saldo Awal Kas</div>
                    <div class="card-value" style="color: #2563eb;">${formatRupiah(period.openingCashBalance)}</div>
                </div>
                <div class="card">
                    <div class="card-title">Arus Kas Masuk (Cash In)</div>
                    <div class="card-value" style="color: #16a34a;">${formatRupiah(summary.totalCashIn)}</div>
                </div>
                <div class="card">
                    <div class="card-title">Arus Kas Keluar (Cash Out)</div>
                    <div class="card-value" style="color: #dc2626;">${formatRupiah(summary.totalCashOut)}</div>
                </div>
                <div class="card">
                    <div class="card-title">Saldo Akhir Kas</div>
                    <div class="card-value" style="color: #0f172a;">${formatRupiah(if (period.closingCashBalance > 0) period.closingCashBalance else summary.totalCash)}</div>
                </div>
                <div class="card">
                    <div class="card-title">Total Pendapatan (Revenue)</div>
                    <div class="card-value" style="color: #16a34a;">${formatRupiah(summary.totalRevenue)}</div>
                </div>
                <div class="card">
                    <div class="card-title">Laba Bersih (Net Profit)</div>
                    <div class="card-value" style="color: ${if (summary.netProfit >= 0) "#16a34a" else "#dc2626"};">${formatRupiah(summary.netProfit)}</div>
                </div>
            </div>

            <div style="font-weight: bold; font-size: 14px; margin-top: 16px; margin-bottom: 6px;">HISTORI TRANSAKSI PERIODE (${transactions.size} Total)</div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 35px; text-align: center;">No</th>
                        <th style="width: 90px;">Tanggal</th>
                        <th style="width: 130px;">No. Trx</th>
                        <th>Keterangan</th>
                        <th style="width: 120px;">Klasifikasi</th>
                        <th style="width: 130px; text-align: right;">Nominal</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            <div class="footer">
                Arsip Pembukuan PKA Bore Pile &bull; Dicetak Resmi untuk Arsip Perusahaan &bull; Single Source of Truth
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
