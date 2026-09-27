package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerInvoiceWithDetails
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import com.example.ui.dialogs.EditWorkerInvoiceHeaderDialog
import com.example.ui.dialogs.WorkerJobDialog
import com.example.ui.dialogs.WorkerLoanDialog
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.PrintHelper
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WorkerInvoicesScreen(
    invoices: List<WorkerInvoiceWithDetails>,
    onOpenAddInvoice: () -> Unit,
    onDeleteInvoice: (Long) -> Unit,
    // CRUD Pekerjaan (Job Items)
    onAddJobItem: (invoiceId: Long, jobName: String, pointCount: Int, depthMeters: Double, unitPricePerMeter: Double) -> Unit,
    onUpdateJobItem: (id: Long, invoiceId: Long, jobName: String, pointCount: Int, depthMeters: Double, unitPricePerMeter: Double) -> Unit,
    onDeleteJobItem: (Long) -> Unit,
    // CRUD Kasbon / Pinjaman (Loan Items)
    onAddLoanItem: (invoiceId: Long, date: String, description: String, trxType: String, amount: Double, deductionDesc: String, deductionAmount: Double) -> Unit,
    onUpdateLoanItem: (id: Long, invoiceId: Long, date: String, description: String, trxType: String, amount: Double, deductionDesc: String, deductionAmount: Double) -> Unit,
    onDeleteLoanItem: (Long) -> Unit,
    // Update Header Invoice
    onUpdateInvoiceHeader: (WorkerInvoiceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Dialog state for Jobs
    var activeInvoiceIdForJob by remember { mutableStateOf<Long?>(null) }
    var jobToEdit by remember { mutableStateOf<WorkerJobItemEntity?>(null) }
    var jobToDelete by remember { mutableStateOf<WorkerJobItemEntity?>(null) }

    // Dialog state for Loans
    var activeInvoiceIdForLoan by remember { mutableStateOf<Long?>(null) }
    var loanToEdit by remember { mutableStateOf<WorkerLoanItemEntity?>(null) }
    var loanToDelete by remember { mutableStateOf<WorkerLoanItemEntity?>(null) }

    // Dialog state for Edit Invoice Header & Delete Invoice
    var invoiceToEditHeader by remember { mutableStateOf<WorkerInvoiceEntity?>(null) }
    var invoiceIdToDelete by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pembukuan Invoice Pekerja",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Kelola borongan tiang, kasbon berkala, & cetak SPK",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpenAddInvoice,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_worker_invoice_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buat Invoice", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Belum ada invoice pekerja / mandor tercatat.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenAddInvoice) {
                        Text("+ Buat Invoice Pekerja Pertama")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(invoices, key = { it.invoice.id }) { item ->
                    WorkerInvoiceCard(
                        item = item,
                        onPrint = {
                            PrintHelper.printHtml(
                                context = context,
                                jobName = "Invoice-${item.invoice.invoiceNumber}",
                                htmlContent = PrintHelper.generateWorkerInvoiceHtml(item)
                            )
                        },
                        // Pekerjaan Callbacks
                        onAddJob = {
                            jobToEdit = null
                            activeInvoiceIdForJob = item.invoice.id
                        },
                        onEditJob = { job ->
                            jobToEdit = job
                            activeInvoiceIdForJob = item.invoice.id
                        },
                        onDeleteJob = { job ->
                            jobToDelete = job
                        },
                        // Kasbon Callbacks
                        onAddLoan = {
                            loanToEdit = null
                            activeInvoiceIdForLoan = item.invoice.id
                        },
                        onEditLoan = { loan ->
                            loanToEdit = loan
                            activeInvoiceIdForLoan = item.invoice.id
                        },
                        onDeleteLoan = { loan ->
                            loanToDelete = loan
                        },
                        // Invoice Header & Deletion Callbacks
                        onEditHeader = {
                            invoiceToEditHeader = item.invoice
                        },
                        onDeleteInvoice = {
                            invoiceIdToDelete = item.invoice.id
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal Dialog: Add or Edit Item Pekerjaan
    if (activeInvoiceIdForJob != null) {
        WorkerJobDialog(
            invoiceId = activeInvoiceIdForJob!!,
            initialJob = jobToEdit,
            onDismiss = {
                activeInvoiceIdForJob = null
                jobToEdit = null
            },
            onSave = { jobId, invId, jobName, pointCount, depthMeters, unitPrice ->
                if (jobId != null) {
                    onUpdateJobItem(jobId, invId, jobName, pointCount, depthMeters, unitPrice)
                } else {
                    onAddJobItem(invId, jobName, pointCount, depthMeters, unitPrice)
                }
                activeInvoiceIdForJob = null
                jobToEdit = null
            }
        )
    }

    // Confirmation Dialog: Delete Job Item
    if (jobToDelete != null) {
        val job = jobToDelete!!
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            title = { Text("Hapus Item Pekerjaan?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus item pekerjaan bore pile ini dari invoice?")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Pekerjaan: ${job.jobName}", fontWeight = FontWeight.SemiBold)
                    Text("• Rincian: ${job.pointCount} titik × ${job.depthMeters} m = ${job.volumeMeters} m'")
                    Text(
                        "• Subtotal: ${formatRupiah(job.subtotal)}",
                        fontWeight = FontWeight.Bold,
                        color = ColorProfit
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteJobItem(job.id)
                        jobToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Pekerjaan")
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Dialog: Add or Edit Kasbon / Pinjaman
    if (activeInvoiceIdForLoan != null) {
        WorkerLoanDialog(
            invoiceId = activeInvoiceIdForLoan!!,
            initialLoan = loanToEdit,
            onDismiss = {
                activeInvoiceIdForLoan = null
                loanToEdit = null
            },
            onSave = { loanId, date, description, trxType, amount, deductionDesc, deductionAmount ->
                if (loanId != null) {
                    onUpdateLoanItem(
                        loanId,
                        activeInvoiceIdForLoan!!,
                        date,
                        description,
                        trxType,
                        amount,
                        deductionDesc,
                        deductionAmount
                    )
                } else {
                    onAddLoanItem(
                        activeInvoiceIdForLoan!!,
                        date,
                        description,
                        trxType,
                        amount,
                        deductionDesc,
                        deductionAmount
                    )
                }
                activeInvoiceIdForLoan = null
                loanToEdit = null
            }
        )
    }

    // Confirmation Dialog: Delete Kasbon
    if (loanToDelete != null) {
        val loan = loanToDelete!!
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("Hapus Kasbon / Pinjaman?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus data kasbon ini?")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Tanggal: ${loan.date}", fontWeight = FontWeight.SemiBold)
                    Text("• Keterangan: ${loan.description}")
                    Text(
                        "• Nominal Bersih: ${formatRupiah(loan.amount - loan.deductionAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = ColorExpense
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLoanItem(loan.id)
                        loanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Kasbon")
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Dialog: Edit Invoice Header
    if (invoiceToEditHeader != null) {
        EditWorkerInvoiceHeaderDialog(
            invoice = invoiceToEditHeader!!,
            onDismiss = { invoiceToEditHeader = null },
            onSave = { updatedInvoice ->
                onUpdateInvoiceHeader(updatedInvoice)
                invoiceToEditHeader = null
            }
        )
    }

    // Confirmation Dialog: Delete Entire Invoice
    if (invoiceIdToDelete != null) {
        val id = invoiceIdToDelete!!
        AlertDialog(
            onDismissRequest = { invoiceIdToDelete = null },
            title = { Text("Hapus Seluruh Invoice?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Semua data rincian pekerjaan tiang dan riwayat kasbon pada invoice ini akan dihapus secara permanen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteInvoice(id)
                        invoiceIdToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceIdToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun WorkerInvoiceCard(
    item: WorkerInvoiceWithDetails,
    onPrint: () -> Unit,
    // Pekerjaan
    onAddJob: () -> Unit,
    onEditJob: (WorkerJobItemEntity) -> Unit,
    onDeleteJob: (WorkerJobItemEntity) -> Unit,
    // Kasbon
    onAddLoan: () -> Unit,
    onEditLoan: (WorkerLoanItemEntity) -> Unit,
    onDeleteLoan: (WorkerLoanItemEntity) -> Unit,
    // Header & Delete
    onEditHeader: () -> Unit,
    onDeleteInvoice: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val inv = item.invoice

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Invoice No, Status, Print & Edit Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inv.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (inv.status == "LUNAS") ColorProfit.copy(alpha = 0.15f) else MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = inv.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inv.status == "LUNAS") ColorProfit else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Tanggal: ${inv.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tombol Edit Data Header Invoice
                    IconButton(
                        onClick = onEditHeader,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_header_invoice_${inv.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Data Invoice",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Action: Cetak / Print PDF
                    Button(
                        onClick = onPrint,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("print_invoice_${inv.id}")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Project & Mandor Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = inv.projectName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mandor / Pekerja: ${inv.workerLeaderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (inv.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${inv.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Preview Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Penghasilan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(item.totalEarnings), fontWeight = FontWeight.Bold, color = ColorProfit)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Kasbon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(item.totalLoans), fontWeight = FontWeight.Bold, color = ColorExpense)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Sisa Bersih Diterima", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatRupiah(item.remainingBalance),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expand / Collapse Details Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Sembunyikan Rincian" else "Kelola Rincian Tiang & Kasbon (${item.jobItems.size} Pekerjaan, ${item.loanItems.size} Kasbon)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // ==========================================
                    // A. DETAIL PEKERJAAN (BORONGAN BORE PILE)
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A. RINCIAN HASIL PEKERJAAN BORE PILE:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Tombol Tambah Pekerjaan
                        Button(
                            onClick = onAddJob,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("add_job_btn_${inv.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Pekerjaan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (item.jobItems.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Belum ada item pekerjaan bore pile tercatat.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = onAddJob,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Tambah Pekerjaan Pertama", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        item.jobItems.forEachIndexed { i, job ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Row 1: Judul Pekerjaan & Action Buttons (Edit / Delete)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${i + 1}. ${job.jobName}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Edit Button
                                            IconButton(
                                                onClick = { onEditJob(job) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("edit_job_${job.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Pekerjaan",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Delete Button
                                            IconButton(
                                                onClick = { onDeleteJob(job) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("delete_job_${job.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus Pekerjaan",
                                                    tint = ColorExpense,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: Perhitungan titik x kedalaman = volume (@harga) & subtotal
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${job.pointCount} titik × ${job.depthMeters} m = ${job.volumeMeters} m' (@${formatRupiah(job.unitPricePerMeter)})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatRupiah(job.subtotal),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ColorProfit
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ==========================================
                    // B. DETAIL KASBON / PINJAMAN PEKERJA
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "B. RINCIAN KASBON / PINJAMAN PEKERJA:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Tombol Tambah Kasbon Berkala
                        Button(
                            onClick = onAddLoan,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.testTag("add_loan_btn_${inv.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Kasbon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (item.loanItems.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Belum ada kasbon / pinjaman tercatat untuk invoice ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = onAddLoan,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Catat Kasbon Pertama", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        item.loanItems.forEachIndexed { i, loan ->
                            val netLoan = loan.amount - loan.deductionAmount
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Row 1: Date, TrxType, Net Amount & Action Buttons (Edit / Delete)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${i + 1}. ${loan.date}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (loan.trxType == "TRANSFER") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (loan.trxType == "TRANSFER") Icons.Default.CreditCard else Icons.Default.Payments,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(loan.trxType, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatRupiah(netLoan),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ColorExpense
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Edit Button
                                            IconButton(
                                                onClick = { onEditLoan(loan) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("edit_loan_${loan.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Kasbon",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Delete Button
                                            IconButton(
                                                onClick = { onDeleteLoan(loan) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("delete_loan_${loan.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus Kasbon",
                                                    tint = ColorExpense,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: Description
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(loan.description, style = MaterialTheme.typography.bodySmall)

                                    // Row 3: Deduction info if applicable
                                    if (loan.deductionAmount > 0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Nominal: ${formatRupiah(loan.amount)} - Potongan: ${formatRupiah(loan.deductionAmount)} (${loan.deductionDescription})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ==========================================
                    // RINGKASAN AKHIR PERHITUNGAN INVOICE
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Borongan:", style = MaterialTheme.typography.bodySmall)
                                Text(formatRupiah(item.totalEarnings), fontWeight = FontWeight.Bold, color = ColorProfit, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Kasbon / Pinjaman:", style = MaterialTheme.typography.bodySmall)
                                Text("-${formatRupiah(item.totalLoans)}", fontWeight = FontWeight.Bold, color = ColorExpense, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = DividerDefaults.color)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SISA HASIL BERSIH:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    formatRupiah(item.remainingBalance),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Buttons: Edit Header, Delete Invoice, Print PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onEditHeader,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Data", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onDeleteInvoice,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ColorExpense, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus", color = ColorExpense, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = onPrint,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak / Print PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
