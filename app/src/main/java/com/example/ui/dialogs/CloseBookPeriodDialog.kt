package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.BookPeriodEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.CurrencyFormatter

@Composable
fun CloseBookPeriodDialog(
    currentPeriod: BookPeriodEntity,
    accounts: List<AccountEntity>,
    periodTransactions: List<TransactionEntity>,
    payables: List<PayableEntity>,
    receivables: List<ReceivableEntity>,
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onConfirmCloseAndArchive: (
        archiveName: String,
        newPeriodName: String,
        newStartDate: String,
        newEndDate: String,
        notes: String
    ) -> Unit
) {
    val totalCash = remember(accounts) { accounts.sumOf { it.currentBalance } }
    val outstandingPayables = remember(payables) { payables.sumOf { it.remainingAmount } }
    val outstandingReceivables = remember(receivables) { receivables.sumOf { it.remainingAmount } }
    val activeProjectsCount = remember(projects) { projects.count { it.status == "ACTIVE" || it.status == "PLANNING" } }
    val totalCashIn = remember(periodTransactions) {
        periodTransactions.filter { it.type == "MONEY_IN" }.sumOf { it.amount }
    }
    val totalCashOut = remember(periodTransactions) {
        periodTransactions.filter { it.type == "MONEY_OUT" }.sumOf { it.amount }
    }

    // Default suggestions for archive and next period names
    var archiveName by remember {
        mutableStateOf(currentPeriod.name)
    }
    var newPeriodName by remember {
        val nextPeriod = if (currentPeriod.name.contains("Januari - April", ignoreCase = true)) {
            "Data ${currentPeriod.year} Mei - Desember"
        } else if (currentPeriod.name.contains("Semester 1", ignoreCase = true)) {
            "Data ${currentPeriod.year} Semester 2"
        } else {
            "Data ${currentPeriod.year} Periode Baru"
        }
        mutableStateOf(nextPeriod)
    }
    var newStartDate by remember {
        // Suggest start date right after current period end date or today
        mutableStateOf(CurrencyFormatter.todayString())
    }
    var newEndDate by remember {
        mutableStateOf("${currentPeriod.year}-12-31")
    }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFinalConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Tutup Buku & Arsipkan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Periode: ${currentPeriod.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PRE-CLOSING VALIDATION & SUMMARY CHECKLIST
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "RINGKASAN & VALIDASI PENUTUPAN BUKU:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SummaryChecklistRow(
                            label = "Rentang Periode",
                            value = "${currentPeriod.startDate} s/d ${currentPeriod.endDate}"
                        )
                        SummaryChecklistRow(
                            label = "Total Transaksi Periode",
                            value = "${periodTransactions.size} Transaksi"
                        )
                        SummaryChecklistRow(
                            label = "Arus Kas Masuk (Cash In)",
                            value = formatRupiah(totalCashIn),
                            valueColor = ColorProfit
                        )
                        SummaryChecklistRow(
                            label = "Arus Kas Keluar (Cash Out)",
                            value = formatRupiah(totalCashOut),
                            valueColor = ColorExpense
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        SummaryChecklistRow(
                            label = "Saldo Akhir Kas (Carry-Forward)",
                            value = formatRupiah(totalCash),
                            isBold = true
                        )
                        SummaryChecklistRow(
                            label = "Hutang Berjalan (Carry-Forward)",
                            value = formatRupiah(outstandingPayables),
                            valueColor = ColorExpense,
                            isBold = true
                        )
                        SummaryChecklistRow(
                            label = "Piutang Berjalan (Carry-Forward)",
                            value = formatRupiah(outstandingReceivables),
                            valueColor = ColorProfit,
                            isBold = true
                        )
                        SummaryChecklistRow(
                            label = "Proyek Berjalan (Carry-Forward)",
                            value = "$activeProjectsCount Proyek Aktif"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // REASSURANCE / INFORMATIVE BANNER
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Setelah penutupan buku, data periode ini akan dibekukan menjadi Arsip (Read-Only) sehingga tampilan utama bersih seperti baru. Seluruh saldo berjalan (kas, hutang, piutang, proyek) akan menjadi Saldo Awal periode baru tanpa membuat transaksi ganda.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // INPUT NAMA ARSIP
                OutlinedTextField(
                    value = archiveName,
                    onValueChange = { archiveName = it },
                    label = { Text("Nama Arsip untuk Periode Ini *") },
                    placeholder = { Text("Contoh: Data 2026 Januari - April") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // INPUT PERIODE BARU
                Text(
                    "PENGATURAN PERIODE AKTIF BARU:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newPeriodName,
                    onValueChange = { newPeriodName = it },
                    label = { Text("Nama Periode Baru *") },
                    placeholder = { Text("Contoh: Data 2026 Mei - Desember") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newStartDate,
                        onValueChange = { newStartDate = CurrencyFormatter.formatDateInput(it) },
                        label = { Text("Tgl Mulai Baru *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEndDate,
                        onValueChange = { newEndDate = CurrencyFormatter.formatDateInput(it) },
                        label = { Text("Tgl Akhir Baru *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Memo Penutupan (Opsional)") },
                    placeholder = { Text("Contoh: Tutup buku kuartal 1 proyek tol & perumahan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (archiveName.isBlank()) {
                                errorMessage = "Nama arsip tidak boleh kosong"
                                return@Button
                            }
                            if (newPeriodName.isBlank()) {
                                errorMessage = "Nama periode baru tidak boleh kosong"
                                return@Button
                            }
                            if (newStartDate.isBlank() || newEndDate.isBlank()) {
                                errorMessage = "Tanggal mulai dan akhir periode baru wajib diisi"
                                return@Button
                            }
                            errorMessage = null
                            showFinalConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tutup Buku & Arsipkan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // MODAL KONFIRMASI PENUTUPAN BUKU SESUAI REQUIREMENT
    if (showFinalConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinalConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Tutup Buku?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Periode:\n${currentPeriod.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Data yang akan diarsipkan:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✓ Transaksi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Arus Kas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Proyek", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Pekerja", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Hutang/Piutang", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Laporan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("✓ Data terkait periode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Setelah ditutup, data periode ini menjadi read-only dan tidak dapat diubah secara normal.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalConfirmDialog = false
                        onConfirmCloseAndArchive(
                            archiveName.trim(),
                            newPeriodName.trim(),
                            newStartDate.trim(),
                            newEndDate.trim(),
                            notes.trim()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Tutup Buku & Arsipkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SummaryChecklistRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ColorProfit,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
