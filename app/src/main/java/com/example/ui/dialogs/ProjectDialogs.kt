package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CashAccountEntity
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.Formatters
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.ProjectCostBreakdown

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        client: String,
        location: String,
        startDate: String,
        targetEndDate: String,
        contractValue: Double,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Formatters.todayDateString()) }
    var targetEndDate by remember { mutableStateOf("") }
    var contractValueText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp)
                .testTag("add_project_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Buat Proyek Bore Pile Baru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Kelola anggaran & pemantauan laba rugi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Proyek *") },
                        placeholder = { Text("Contoh: Bore Pile Flyover Kalimalang") },
                        modifier = Modifier.fillMaxWidth().testTag("project_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Klien / Pemberi Kerja *") },
                        placeholder = { Text("Contoh: PT Adhi Karya Tbk") },
                        modifier = Modifier.fillMaxWidth().testTag("project_client_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi Pekerjaan *") },
                        placeholder = { Text("Contoh: Bekasi Barat, Jawa Barat") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contractValueText,
                        onValueChange = { contractValueText = it.filter { c -> c.isDigit() } },
                        label = { Text("Nilai Kontrak (Rp) *") },
                        placeholder = { Text("Contoh: 350000000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("project_contract_input"),
                        singleLine = true,
                        supportingText = {
                            val v = contractValueText.toDoubleOrNull() ?: 0.0
                            if (v > 0) Text("Terbilang: ${Formatters.rupiah(v)}", color = MaterialTheme.colorScheme.primary)
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Tgl Mulai *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetEndDate,
                            onValueChange = { targetEndDate = it },
                            label = { Text("Target Selesai") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Teknis / Spesifikasi") },
                        placeholder = { Text("Jumlah titik bor, diameter pipa casing, kedalaman rata-rata...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cv = contractValueText.toDoubleOrNull() ?: 0.0
                            if (name.trim().isEmpty()) {
                                errorMessage = "Nama proyek wajib diisi"
                            } else if (client.trim().isEmpty()) {
                                errorMessage = "Nama klien wajib diisi"
                            } else if (cv <= 0) {
                                errorMessage = "Nilai kontrak harus lebih dari Rp 0"
                            } else {
                                onSubmit(name, client, location, startDate, targetEndDate, cv, notes)
                            }
                        },
                        modifier = Modifier.testTag("save_new_project_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Proyek")
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectDetailDialog(
    project: ProjectEntity,
    breakdown: ProjectCostBreakdown,
    projectTransactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onToggleStatus: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project.code,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (project.status == "ACTIVE") Emerald600 else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (project.status == "ACTIVE") "AKTIF" else "SELESAI",
                                    color = if (project.status == "ACTIVE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project.client} • ${project.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Financial Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ringkasan Keuangan Proyek", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nilai Kontrak:")
                                Text(Formatters.rupiah(breakdown.contractValue), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pembayaran Diterima:")
                                Text(Formatters.rupiah(breakdown.revenueReceived), fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pengeluaran Proyek:")
                                Text(Formatters.rupiah(breakdown.totalCost), fontWeight = FontWeight.Bold, color = Rose600)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Laba Kotor Proyek:", fontWeight = FontWeight.Bold)
                                Text(
                                    Formatters.rupiah(breakdown.grossProfit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (breakdown.grossProfit >= 0) Emerald600 else Rose600
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Margin Profitabilitas:", fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%.1f", breakdown.marginPercentage)}%",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sisa Piutang Kontrak:")
                                Text(Formatters.rupiah(breakdown.remainingReceivable), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Breakdown Biaya Detail (Penting untuk Bore Pile)
                    Text("Rincian Pengeluaran Lapangan:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BreakdownItemRow("Material (Beton, Besi, Bentonite)", breakdown.materialCost)
                            BreakdownItemRow("Upah Operator & Tenaga Kerja", breakdown.laborCost)
                            BreakdownItemRow("Mobilisasi & Demobilisasi Rig", breakdown.mobilizationCost)
                            BreakdownItemRow("Bahan Bakar Solar & Oli", breakdown.fuelCost)
                            BreakdownItemRow("Sewa Alat & Genset Tambahan", breakdown.equipmentCost)
                            BreakdownItemRow("Biaya Lainnya & Koordinasi", breakdown.otherCost)
                        }
                    }

                    // Riwayat Transaksi Khusus Proyek Ini
                    Text("Semua Transaksi Proyek Ini (${projectTransactions.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    if (projectTransactions.isEmpty()) {
                        Text("Belum ada transaksi untuk proyek ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            projectTransactions.forEach { trx ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (trx.isVoid) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${Formatters.simpleDate(trx.date)} • ${trx.code}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(trx.description, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                                            Text(trx.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            val sign = if (trx.type == "MONEY_IN") "+" else "-"
                                            val col = if (trx.type == "MONEY_IN") Emerald600 else Rose600
                                            Text(
                                                text = "$sign${Formatters.rupiah(trx.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                color = col,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (trx.isVoid) {
                                                Text("BATAL (VOID)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar (Tutup, Selesaikan Proyek / Buka Kembali)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (project.status == "ACTIVE") {
                        Button(
                            onClick = { onToggleStatus("COMPLETED") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Tandai Proyek Selesai")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onToggleStatus("ACTIVE") }
                        ) {
                            Text("Buka Kembali Proyek")
                        }
                    }

                    Button(onClick = onDismiss) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownItemRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(Formatters.rupiah(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun VoidTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Batalkan Transaksi (Audit Trail)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Transaksi tidak dihapus permanen agar riwayat akuntansi tetap rapi. Transaksi akan ditandai sebagai BATAL (VOID) dan tidak mempengaruhi saldo.")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${transaction.code} • ${Formatters.rupiah(transaction.amount)}",
                    fontWeight = FontWeight.Bold
                )
                Text(transaction.description, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it; error = false },
                    label = { Text("Alasan Pembatalan *") },
                    placeholder = { Text("Contoh: Salah nominal input / dobel transfer") },
                    modifier = Modifier.fillMaxWidth().testTag("void_reason_input"),
                    isError = error
                )
                if (error) {
                    Text("Alasan pembatalan wajib diisi", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().isEmpty()) {
                        error = true
                    } else {
                        onConfirm(reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_void_button")
            ) {
                Text("Batalkan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun SettleDebtDialog(
    item: DebtReceivableEntity,
    accounts: List<CashAccountEntity>,
    onDismiss: () -> Unit,
    onSettle: (amount: Double, accountId: Int, accountName: String, date: String, method: String, notes: String) -> Unit
) {
    val remaining = item.totalAmount - item.paidAmount
    var amountText by remember { mutableStateOf(remaining.toInt().toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1) }
    var method by remember { mutableStateOf("Transfer") }
    var dateText by remember { mutableStateOf(Formatters.todayDateString()) }
    var notes by remember { mutableStateOf("") }
    val selectedAcc = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (item.type == "RECEIVABLE") "Catat Pembayaran Piutang" else "Catat Pembayaran Utang")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${item.partyName} - ${item.title}", fontWeight = FontWeight.Bold)
                Text("Sisa Kewajiban: ${Formatters.rupiah(remaining)}", color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Pembayaran (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rekening Kas/Bank:")
                accounts.forEach { acc ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (selectedAccountId == acc.id),
                            onClick = { selectedAccountId = acc.id }
                        )
                        Text(acc.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / No. Kuitansi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSettle(
                            amt,
                            selectedAccountId,
                            selectedAcc?.name ?: "Kas",
                            dateText,
                            method,
                            notes
                        )
                    }
                }
            ) {
                Text("Simpan Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
