package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.style.TextOverflow
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Invoice Pekerja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Borongan tiang & kasbon berkala",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpenAddInvoice,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("add_worker_invoice_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Invoice", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    Button(
                        onClick = onOpenAddInvoice,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Invoice", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+", fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Invoice No, Role, Status, and Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inv.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (inv.workerRole == "MANDOR") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (inv.workerRole == "MANDOR") "MANDOR" else "PEKERJA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inv.workerRole == "MANDOR") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (inv.status == "LUNAS") ColorProfit.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = inv.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inv.status == "LUNAS") ColorProfit else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Action: Edit (Logo Only)
                    IconButton(
                        onClick = onEditHeader,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_header_invoice_${inv.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Action: Cetak Print / PDF (Logo Only)
                    IconButton(
                        onClick = onPrint,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("print_invoice_${inv.id}")
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print PDF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Action: Hapus (Logo Only)
                    IconButton(
                        onClick = onDeleteInvoice,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_invoice_${inv.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = ColorExpense,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Project & Mandor Info + Date in one compact line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${inv.projectName} • ${inv.workerLeaderName}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = inv.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (inv.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Catatan: ${inv.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compact Financial Summary Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Penghasilan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRupiah(item.totalEarnings), fontWeight = FontWeight.Bold, color = ColorProfit, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kasbon", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRupiah(item.totalLoans), fontWeight = FontWeight.Bold, color = ColorExpense, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Sisa Bersih", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRupiah(item.remainingBalance), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                    text = if (expanded) "Tutup Rincian" else "Rincian (${item.jobItems.size} Pekerjaan, ${item.loanItems.size} Kasbon)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // ==========================================
                    // A. DETAIL PEKERJAAN (BORONGAN BORE PILE)
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A. HASIL PEKERJAAN TIANG (${item.jobItems.size})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Tombol Tambah Pekerjaan (+)
                        Button(
                            onClick = onAddJob,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("add_job_btn_${inv.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Pekerjaan", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (item.jobItems.isEmpty()) {
                        Text(
                            text = "Belum ada item pekerjaan. Ketuk tombol '+' di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        item.jobItems.forEachIndexed { i, job ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${i + 1}. ${job.jobName}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onEditJob(job) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("edit_job_${job.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteJob(job) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("delete_job_${job.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = ColorExpense,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${job.pointCount} titik × ${job.depthMeters} m = ${job.volumeMeters} m' (@${formatRupiah(job.unitPricePerMeter)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatRupiah(job.subtotal),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ColorProfit
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // B. DETAIL KASBON / PINJAMAN PEKERJA
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "B. KASBON / PINJAMAN (${item.loanItems.size})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Tombol Tambah Kasbon (+)
                        Button(
                            onClick = onAddLoan,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("add_loan_btn_${inv.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Kasbon", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (item.loanItems.isEmpty()) {
                        Text(
                            text = "Belum ada kasbon tercatat. Ketuk tombol '+' di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        item.loanItems.forEachIndexed { i, loan ->
                            val netLoan = loan.amount - loan.deductionAmount
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${i + 1}. ${loan.date}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (loan.trxType == "TRANSFER") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Text(loan.trxType, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatRupiah(netLoan),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ColorExpense
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { onEditLoan(loan) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("edit_loan_${loan.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteLoan(loan) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("delete_loan_${loan.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = ColorExpense,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(loan.description, style = MaterialTheme.typography.labelSmall)

                                    if (loan.deductionAmount > 0) {
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
