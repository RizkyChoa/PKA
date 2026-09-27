package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

data class TempJobItem(
    var jobName: String = "",
    var pointCountText: String = "",
    var depthText: String = "",
    var unitPriceText: String = ""
) {
    val pointCount: Int get() = pointCountText.toIntOrNull() ?: 0
    val depth: Double get() = depthText.toDoubleOrNull() ?: 0.0
    val volume: Double get() = pointCount * depth
    val unitPrice: Double get() = unitPriceText.toDoubleOrNull() ?: 0.0
    val subtotal: Double get() = volume * unitPrice
}

data class TempLoanItem(
    var date: String = "",
    var description: String = "",
    var trxType: String = "TRANSFER", // TRANSFER / CASH
    var amountText: String = "",
    var deductionDesc: String = "",
    var deductionAmountText: String = ""
) {
    val amount: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val deductionAmount: Double get() = deductionAmountText.toDoubleOrNull() ?: 0.0
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
        date: String,
        notes: String,
        jobItems: List<WorkerJobItemEntity>,
        loanItems: List<WorkerLoanItemEntity>
    ) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedProjectId by remember { mutableLongStateOf(projects.firstOrNull()?.id ?: 0L) }
    var workerLeaderName by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(todayStr) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic Lists
    val jobList = remember {
        mutableStateListOf(
            TempJobItem(jobName = "Bore pile diameter 40cm", pointCountText = "50", depthText = "24", unitPriceText = "40000"),
            TempJobItem(jobName = "Bore pile diameter 50cm", pointCountText = "20", depthText = "29", unitPriceText = "50000")
        )
    }

    val loanList = remember {
        mutableStateListOf(
            TempLoanItem(
                date = todayStr,
                description = "Pembayaran DP naik anggota 5 orang",
                trxType = "TRANSFER",
                amountText = "5000000",
                deductionDesc = "ongkos keberangkatan",
                deductionAmountText = "1500000"
            )
        )
    }

    val totalEarnings = jobList.sumOf { it.subtotal }
    val totalLoans = loanList.sumOf { it.netLoan }
    val remainingBalance = totalEarnings - totalLoans

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Buat Invoice Pekerja / Mandor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Perhitungan volume meter lari & kasbon pekerja per proyek",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Project Selector from database
                var expandedProj by remember { mutableStateOf(false) }
                val currentProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()

                ExposedDropdownMenuBox(
                    expanded = expandedProj,
                    onExpandedChange = { expandedProj = it }
                ) {
                    OutlinedTextField(
                        value = currentProject?.let { "${it.name} (${it.projectCode})" } ?: "Pilih Proyek",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Proyek Bore Pile (Dari Database) *") },
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
                        projects.forEach { prj ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prj.name, fontWeight = FontWeight.Bold)
                                        Text("${prj.clientName} • ${prj.projectCode}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedProjectId = prj.id
                                    expandedProj = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mandor / Tim Pekerja
                OutlinedTextField(
                    value = workerLeaderName,
                    onValueChange = { workerLeaderName = it },
                    label = { Text("Nama Mandor / Tim Pekerja *") },
                    placeholder = { Text("Contoh: Mandor Syarif & Tim") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = invoiceDate,
                    onValueChange = { invoiceDate = it },
                    label = { Text("Tanggal Invoice (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: TABEL HASIL PEKERJAAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. DAFTAR PEKERJAAN BORE PILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { jobList.add(TempJobItem()) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Pekerjaan", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                jobList.forEachIndexed { index, job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pekerjaan #${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (jobList.size > 1) {
                                    IconButton(
                                        onClick = { jobList.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = job.jobName,
                                onValueChange = { job.jobName = it },
                                label = { Text("Nama Pekerjaan / Item") },
                                placeholder = { Text("Contoh: Bore pile diameter 40cm") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = job.pointCountText,
                                    onValueChange = { job.pointCountText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Jumlah Titik") },
                                    placeholder = { Text("50") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = job.depthText,
                                    onValueChange = { job.depthText = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Kedalaman (m)") },
                                    placeholder = { Text("24") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = job.unitPriceText,
                                    onValueChange = { job.unitPriceText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Harga / meter (Rp)") },
                                    placeholder = { Text("40000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Volume: ${job.volume} m'", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Subtotal: ${formatRupiah(job.subtotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ColorProfit
                                    )
                                }
                            }
                        }
                    }
                }

                // Total Earnings preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ColorProfit.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL PENGHASILAN KOTOR:", fontWeight = FontWeight.Bold, color = ColorProfit)
                        Text(formatRupiah(totalEarnings), fontWeight = FontWeight.ExtraBold, color = ColorProfit)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: TABEL KASBON / PINJAMAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. DAFTAR KASBON / PINJAMAN PEKERJA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { loanList.add(TempLoanItem(date = todayStr)) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Kasbon", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                loanList.forEachIndexed { index, loan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kasbon #${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                IconButton(
                                    onClick = { loanList.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = loan.date,
                                    onValueChange = { loan.date = it },
                                    label = { Text("Tanggal") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = loan.trxType,
                                    onValueChange = { loan.trxType = it },
                                    label = { Text("Jenis (Transfer/Tunai)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = loan.description,
                                onValueChange = { loan.description = it },
                                label = { Text("Keterangan Kasbon") },
                                placeholder = { Text("Contoh: Pembayaran DP naik anggota 5 orang") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = loan.amountText,
                                    onValueChange = { loan.amountText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Nominal Kasbon (Rp)") },
                                    placeholder = { Text("5000000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = loan.deductionAmountText,
                                    onValueChange = { loan.deductionAmountText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Potongan Opsional (Rp)") },
                                    placeholder = { Text("1500000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = loan.deductionDesc,
                                onValueChange = { loan.deductionDesc = it },
                                label = { Text("Keterangan Potongan (Opsional)") },
                                placeholder = { Text("Contoh: ongkos keberangkatan") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Net Kasbon Ini: ${formatRupiah(loan.netLoan)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorExpense
                            )
                        }
                    }
                }

                // Total Loans preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ColorExpense.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL PINJAMAN / KASBON:", fontWeight = FontWeight.Bold, color = ColorExpense)
                        Text(formatRupiah(totalLoans), fontWeight = FontWeight.ExtraBold, color = ColorExpense)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: RINGKASAN AKHIR INVOICE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "RINGKASAN AKHIR INVOICE PEKERJA",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Penghasilan:", style = MaterialTheme.typography.bodyMedium)
                            Text(formatRupiah(totalEarnings), fontWeight = FontWeight.Bold, color = ColorProfit)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pinjaman / Kasbon:", style = MaterialTheme.typography.bodyMedium)
                            Text("-${formatRupiah(totalLoans)}", fontWeight = FontWeight.Bold, color = ColorExpense)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SISA HASIL PROYEK (BERSIH):", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                formatRupiah(remainingBalance),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (currentProject == null) {
                                errorMessage = "Pilih proyek dari database"
                                return@Button
                            }
                            if (workerLeaderName.isBlank()) {
                                errorMessage = "Nama Mandor / Tim Pekerja wajib diisi"
                                return@Button
                            }
                            if (jobList.isEmpty() || jobList.all { it.volume <= 0 || it.unitPrice <= 0 }) {
                                errorMessage = "Isi minimal 1 pekerjaan dengan titik, kedalaman, dan harga yang valid"
                                return@Button
                            }

                            val convertedJobs = jobList.map {
                                WorkerJobItemEntity(
                                    invoiceId = 0,
                                    jobName = it.jobName.ifBlank { "Pekerjaan Bore Pile" },
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
                                    description = it.description.ifBlank { "Kasbon Pekerja" },
                                    trxType = it.trxType,
                                    amount = it.amount,
                                    deductionDescription = it.deductionDesc,
                                    deductionAmount = it.deductionAmount
                                )
                            }

                            onSaveInvoice(
                                currentProject.id,
                                currentProject.name,
                                workerLeaderName,
                                invoiceDate,
                                notes,
                                convertedJobs,
                                convertedLoans
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Invoice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
