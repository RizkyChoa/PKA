package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProjectEntity
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TempJobItem(
    jobName: String = "",
    pointCountText: String = "",
    depthText: String = "",
    unitPriceText: String = ""
) {
    var jobName by mutableStateOf(jobName)
    var pointCountText by mutableStateOf(pointCountText)
    var depthText by mutableStateOf(depthText)
    var unitPriceText by mutableStateOf(unitPriceText)

    val pointCount: Int get() = pointCountText.toIntOrNull() ?: 0
    val depth: Double get() = depthText.toDoubleOrNull() ?: 0.0
    val volume: Double get() = pointCount * depth
    val unitPrice: Double get() = com.example.util.CurrencyFormatter.parseInput(unitPriceText)
    val subtotal: Double get() = volume * unitPrice
}

class TempLoanItem(
    date: String = "",
    description: String = "",
    trxType: String = "TRANSFER",
    amountText: String = "",
    deductionDesc: String = "",
    deductionAmountText: String = ""
) {
    var date by mutableStateOf(date)
    var description by mutableStateOf(description)
    var trxType by mutableStateOf(trxType)
    var amountText by mutableStateOf(amountText)
    var deductionDesc by mutableStateOf(deductionDesc)
    var deductionAmountText by mutableStateOf(deductionAmountText)

    val amount: Double get() = com.example.util.CurrencyFormatter.parseInput(amountText)
    val deductionAmount: Double get() = com.example.util.CurrencyFormatter.parseInput(deductionAmountText)
    val netLoan: Double get() = (amount - deductionAmount).coerceAtLeast(0.0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerInvoiceDialog(
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSaveInvoice: (
        projectId: Long,
        projectName: String,
        workerLeaderName: String,
        workerRole: String,
        invoiceDate: String,
        notes: String,
        jobItems: List<WorkerJobItemEntity>,
        loanItems: List<WorkerLoanItemEntity>
    ) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedProjectId by remember { mutableLongStateOf(projects.firstOrNull()?.id ?: 0L) }
    var workerRole by remember { mutableStateOf("PEKERJA") } // "MANDOR" atau "PEKERJA"
    var workerLeaderName by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(todayStr) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Job Items - starts ready for user input
    val jobList = remember {
        mutableStateListOf(
            TempJobItem(
                jobName = "Bore Pile D-60",
                pointCountText = "",
                depthText = "",
                unitPriceText = ""
            )
        )
    }

    // Loan Items (Kasbon) - clean empty list
    val loanList = remember {
        mutableStateListOf<TempLoanItem>()
    }

    val totalEarnings = jobList.sumOf { it.subtotal }
    val totalLoans = loanList.sumOf { it.netLoan }
    val remainingBalance = totalEarnings - totalLoans

    val currentProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()

    fun doSubmit() {
        if (currentProject == null) {
            errorMessage = "Pilih proyek dari database terlebih dahulu"
            return
        }
        if (workerLeaderName.isBlank()) {
            errorMessage = if (workerRole == "MANDOR") "Nama Mandor / Supervisi Lapangan wajib diisi" else "Nama Tim Pekerja / Operator Rig wajib diisi"
            return
        }
        if (jobList.isEmpty() || jobList.all { it.volume <= 0 || it.unitPrice <= 0 }) {
            errorMessage = "Isi minimal 1 pekerjaan dengan titik, kedalaman, dan harga yang valid"
            return
        }

        val convertedJobs = jobList.map {
            WorkerJobItemEntity(
                invoiceId = 0,
                jobName = it.jobName.ifBlank { if (workerRole == "MANDOR") "Supervisi Bore Pile" else "Pekerjaan Bore Pile" },
                pointCount = it.pointCount,
                depthMeters = it.depth,
                volumeMeters = it.volume,
                unitPricePerMeter = it.unitPrice,
                subtotal = it.subtotal
            )
        }

        val convertedLoans = loanList.map {
            WorkerLoanItemEntity(
                invoiceId = 0,
                date = it.date.ifBlank { invoiceDate },
                description = it.description.ifBlank { if (workerRole == "MANDOR") "Kasbon Mandor" else "Kasbon Pekerja" },
                trxType = it.trxType,
                amount = it.amount,
                deductionDescription = it.deductionDesc,
                deductionAmount = it.deductionAmount
            )
        }

        onSaveInvoice(
            currentProject.id,
            currentProject.name,
            workerLeaderName.trim(),
            workerRole,
            invoiceDate.trim(),
            notes.trim(),
            convertedJobs,
            convertedLoans
        )
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Outer Container: strictly handles statusBars, navigationBars & IME (keyboard)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ========================================================
                    // 1. STICKY TOP HEADER (Selalu terlihat & bisa submit langsung)
                    // ========================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Buat Invoice Upah",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pisahkan Upah Mandor vs Upah Pekerja",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Submit Button in Header
                            Button(
                                onClick = { doSubmit() },
                                modifier = Modifier
                                    .testTag("top_submit_worker_invoice_button")
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Selesai", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .testTag("close_add_invoice_button")
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // ========================================================
                    // 2. SCROLLABLE CONTENT BODY (Bisa digulir leluasa)
                    // ========================================================
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Project Selector from database
                        var expandedProj by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedProj,
                            onExpandedChange = { expandedProj = it }
                        ) {
                            OutlinedTextField(
                                value = currentProject?.let { "${it.name} (${it.projectCode})" } ?: "Pilih Proyek",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Proyek Bore Pile *") },
                                leadingIcon = { Icon(Icons.Default.Construction, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProj) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedProj,
                                onDismissRequest = { expandedProj = false }
                            ) {
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text("${proj.name} (${proj.clientName})") },
                                        onClick = {
                                            selectedProjectId = proj.id
                                            expandedProj = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // KATEGORI PENERIMA UPAH: Mandor vs Pekerja (Sesuai Permintaan User!)
                        Text(
                            text = "Kategori Penerima Upah Lapangan *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = workerRole == "MANDOR",
                                onClick = { workerRole = "MANDOR" },
                                label = {
                                    Text(
                                        "Upah Mandor & Supervisi",
                                        fontWeight = if (workerRole == "MANDOR") FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = workerRole == "PEKERJA",
                                onClick = { workerRole = "PEKERJA" },
                                label = {
                                    Text(
                                        "Upah Pekerja / Tenaga Bor",
                                        fontWeight = if (workerRole == "PEKERJA") FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = workerLeaderName,
                                onValueChange = { workerLeaderName = it },
                                label = { Text(if (workerRole == "MANDOR") "Nama Mandor Lapangan *" else "Nama Tim Pekerja / Operator *") },
                                placeholder = { Text(if (workerRole == "MANDOR") "Contoh: Mandor Syarif" else "Contoh: Tim Bor Mas Joko") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = invoiceDate,
                                onValueChange = { invoiceDate = it },
                                label = { Text("Tanggal") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ========================================================
                        // SECTION 1: HASIL PEKERJAAN BORE PILE
                        // ========================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1. HASIL PEKERJAAN BORE PILE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Header "+ Tambah Pekerjaan" Button
                            Button(
                                onClick = {
                                    jobList.add(TempJobItem(jobName = "Bore Pile D-${(jobList.size + 4) * 10}"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .testTag("btn_add_worker_job")
                                    .height(38.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Pekerjaan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        jobList.forEachIndexed { index, job ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Pekerjaan Bor #${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (jobList.size > 1) {
                                            IconButton(
                                                onClick = { jobList.removeAt(index) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = job.jobName,
                                        onValueChange = { job.jobName = it },
                                        label = { Text("Jenis Pekerjaan / Diameter") },
                                        placeholder = { Text("Contoh: Bore pile diameter 60cm") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = job.pointCountText,
                                            onValueChange = { job.pointCountText = it.filter { c -> c.isDigit() } },
                                            label = { Text("Titik") },
                                            placeholder = { Text("50") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = job.depthText,
                                            onValueChange = { job.depthText = it.filter { c -> c.isDigit() || c == '.' } },
                                            label = { Text("Kedalaman(m)") },
                                            placeholder = { Text("24") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1.2f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = job.unitPriceText,
                                            onValueChange = { job.unitPriceText = com.example.util.CurrencyFormatter.formatInput(it) },
                                            label = { Text("Harga/m (Rp)") },
                                            placeholder = { Text("40.000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1.5f),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Subtotal calculation per job item
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Volume: ${job.volume.toInt()} m' (${job.pointCount} ttk × ${job.depth} m)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Subtotal: ${formatRupiah(job.subtotal)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ColorProfit
                                        )
                                    }
                                }
                            }
                        }

                        // TOMBOL TAMBAH PEKERJAAN LAINNYA (Full Width, Menjolok & Jelas)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                jobList.add(TempJobItem(jobName = "Bore Pile D-${(jobList.size + 4) * 10}"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_add_another_job"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Tambah Pekerjaan / Diameter Bor Lainnya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ========================================================
                        // SECTION 2: KASBON & PINJAMAN PEKERJA
                        // ========================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = ColorExpense, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. KASBON & PINJAMAN BERKALA",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorExpense
                                )
                            }

                            // Header "+ Tambah Kasbon" Button
                            Button(
                                onClick = { loanList.add(TempLoanItem(date = todayStr)) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                modifier = Modifier
                                    .testTag("btn_add_worker_loan")
                                    .height(38.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Kasbon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (loanList.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "Belum ada kasbon tercatat. Ketuk tombol 'Tambah Kasbon' jika ada kasbon.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            loanList.forEachIndexed { index, loan ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Kasbon #${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = ColorExpense,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            IconButton(
                                                onClick = { loanList.removeAt(index) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = loan.date,
                                                onValueChange = { loan.date = com.example.util.CurrencyFormatter.formatDateInput(it) },
                                                label = { Text("Tgl Kasbon") },
                                                placeholder = { Text("YYYY-MM-DD") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = loan.amountText,
                                                onValueChange = { loan.amountText = com.example.util.CurrencyFormatter.formatInput(it) },
                                                label = { Text("Nominal Kasbon (Rp)") },
                                                placeholder = { Text("5.000.000") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1.3f),
                                                singleLine = true
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = loan.description,
                                            onValueChange = { loan.description = it },
                                            label = { Text("Keperluan / Keterangan Kasbon") },
                                            placeholder = { Text("Contoh: DP naik anggota / biaya hidup") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = loan.deductionDesc,
                                                onValueChange = { loan.deductionDesc = it },
                                                label = { Text("Ket. Potongan (Opsional)") },
                                                placeholder = { Text("Contoh: Ongkos mobil") },
                                                modifier = Modifier.weight(1.2f),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = loan.deductionAmountText,
                                                onValueChange = { loan.deductionAmountText = com.example.util.CurrencyFormatter.formatInput(it) },
                                                label = { Text("Potongan (Rp)") },
                                                placeholder = { Text("1.500.000") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                "Kasbon Bersih: ${formatRupiah(loan.netLoan)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorExpense
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // TOMBOL TAMBAH KASBON LAINNYA (Full Width, Menjolok & Jelas)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { loanList.add(TempLoanItem(date = todayStr)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_add_another_loan"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Tambah Kasbon Lainnya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Catatan
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Catatan Invoice (Opsional)") },
                            placeholder = { Text("Keterangan mandor, lembur, kondisi tanah lapangan...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ringkasan Penghasilan vs Kasbon
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Borongan:", style = MaterialTheme.typography.bodySmall)
                                    Text(formatRupiah(totalEarnings), fontWeight = FontWeight.Bold, color = ColorProfit)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Kasbon / Pinjaman:", style = MaterialTheme.typography.bodySmall)
                                    Text("-${formatRupiah(totalLoans)}", fontWeight = FontWeight.Bold, color = ColorExpense)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        if (workerRole == "MANDOR") "SISA UPAH MANDOR:" else "SISA UPAH PEKERJA:",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        formatRupiah(remainingBalance),
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Extra breathing space at bottom of scrollable area so everything scrolls well above the bottom bar
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // ========================================================
                    // 3. STICKY BOTTOM ACTION BAR (SELALU TERLIHAT & TIDAK TERTUTUP NAVIGASI HP!)
                    // ========================================================
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (workerRole == "MANDOR") "Sisa Bersih Mandor" else "Sisa Bersih Pekerja",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatRupiah(remainingBalance),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text("Batal")
                                    }

                                    Button(
                                        onClick = { doSubmit() },
                                        modifier = Modifier
                                            .height(44.dp)
                                            .testTag("submit_worker_invoice_button"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Selesai & Simpan", fontWeight = FontWeight.Bold)
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
