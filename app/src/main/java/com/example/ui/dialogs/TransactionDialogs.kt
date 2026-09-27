package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.data.model.ProjectEntity
import com.example.ui.components.Formatters
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<CashAccountEntity>,
    activeProjects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        date: String,
        type: String,
        amount: Double,
        accountId: Int,
        accountName: String,
        classification: String,
        category: String,
        description: String,
        projectId: Int?,
        projectName: String?,
        paymentMethod: String,
        referenceNumber: String
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Step 1 State
    var transactionType by remember { mutableStateOf("MONEY_OUT") } // MONEY_IN or MONEY_OUT
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(Formatters.todayDateString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1) }
    var paymentMethod by remember { mutableStateOf("Transfer") }
    var description by remember { mutableStateOf("") }
    var refNumber by remember { mutableStateOf("") }

    // Step 2 State (Classification)
    var classification by remember { mutableStateOf("PROJECT") }
    var selectedProjectId by remember { mutableStateOf(activeProjects.firstOrNull()?.id) }
    var category by remember { mutableStateOf("Material (Beton / Besi / Bentonite)") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()

    // Dynamic Categories based on Type & Classification
    val categories = remember(transactionType, classification) {
        when (classification) {
            "PROJECT" -> {
                if (transactionType == "MONEY_IN") {
                    listOf(
                        "Uang Muka / DP Proyek",
                        "Termin Proyek (Progress Billing)",
                        "Pelunasan Akhir Proyek",
                        "Klaim Tambah Kurang (CCO/VO)"
                    )
                } else {
                    listOf(
                        "Material (Beton / Besi / Bentonite)",
                        "Upah Tenaga Kerja & Operator",
                        "Mobilisasi & Demobilisasi Alat",
                        "Bahan Bakar Solar & Oli",
                        "Sewa Alat Tambahan & Genset",
                        "Perbaikan & Sparepart Alat",
                        "Koordinasi Lapangan & Perizinan",
                        "Biaya Proyek Lainnya"
                    )
                }
            }
            "OPERATIONAL" -> listOf(
                "Operasional Kantor & Administrasi",
                "Gaji Staf & Manajemen Kantor",
                "Sewa Kantor & Utilitas (Listrik/Air)",
                "Biaya Bank & Pajak Perusahaan",
                "Konsumsi & Logistik Kantor"
            )
            "DEBT" -> {
                if (transactionType == "MONEY_IN") {
                    listOf("Penerimaan Pinjaman Bank / Pihak Ketiga")
                } else {
                    listOf("Pembayaran Pokok Utang / Pinjaman")
                }
            }
            "RECEIVABLE" -> {
                if (transactionType == "MONEY_IN") {
                    listOf("Penerimaan Pelunasan Piutang")
                } else {
                    listOf("Pemberian Pinjaman / Piutang")
                }
            }
            "CAPITAL" -> {
                if (transactionType == "MONEY_IN") {
                    listOf("Setoran Modal Pemilik / Investor")
                } else {
                    listOf("Penarikan Modal / Prive Pemilik")
                }
            }
            "ASSET" -> listOf("Pembelian Aset Mesin Rig / Kendaraan / Alat Berat")
            else -> listOf("Lain-Lain")
        }
    }

    LaunchedEffect(categories) {
        if (!categories.contains(category)) {
            category = categories.firstOrNull() ?: "Lain-Lain"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
                .testTag("add_transaction_dialog"),
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
                    Column {
                        Text(
                            text = if (step == 1) "Input Transaksi (Langkah 1/2)" else "Klasifikasi Akuntansi (Langkah 2/2)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (step == 1) "Data dasar arus kas" else "Menghubungkan ke Proyek & Buku Besar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_transaction_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error Banner
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (step == 1) {
                        // === STEP 1: TRANSACTION BASICS ===
                        Text("Jenis Arus Kas:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Money In Button
                            Button(
                                onClick = { transactionType = "MONEY_IN" },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("select_money_in"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (transactionType == "MONEY_IN") Emerald600 else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (transactionType == "MONEY_IN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Uang Masuk (+)")
                            }

                            // Money Out Button
                            Button(
                                onClick = { transactionType = "MONEY_OUT" },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("select_money_out"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (transactionType == "MONEY_OUT") Rose600 else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (transactionType == "MONEY_OUT") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Uang Keluar (-)")
                            }
                        }

                        // Nominal Input
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                            label = { Text("Nominal (Rp) *") },
                            placeholder = { Text("Contoh: 15000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input"),
                            singleLine = true,
                            supportingText = {
                                val amountVal = amountText.toDoubleOrNull() ?: 0.0
                                if (amountVal > 0) {
                                    Text("Terbilang: ${Formatters.rupiah(amountVal)}", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        // Tanggal
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = { dateText = it },
                            label = { Text("Tanggal Transaksi (YYYY-MM-DD) *") },
                            modifier = Modifier.fillMaxWidth().testTag("transaction_date_input"),
                            singleLine = true
                        )

                        // Akun Kas / Bank
                        Text("Sumber / Destinasi Uang (Akun):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            accounts.forEach { acc ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedAccountId == acc.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAccountId = acc.id }
                                        .testTag("account_option_${acc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (selectedAccountId == acc.id),
                                            onClick = { selectedAccountId = acc.id }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(acc.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${acc.type} • ${acc.accountNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Metode Pembayaran
                        Text("Metode Pembayaran:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Transfer", "Tunai", "Cek/Giro").forEach { method ->
                                FilterChip(
                                    selected = paymentMethod == method,
                                    onClick = { paymentMethod = method },
                                    label = { Text(method) }
                                )
                            }
                        }

                        // Deskripsi
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi / Keterangan Transaksi *") },
                            placeholder = { Text("Contoh: Pembelian solar 500L genset rig") },
                            modifier = Modifier.fillMaxWidth().testTag("transaction_desc_input")
                        )

                        // No Referensi / Kuitansi
                        OutlinedTextField(
                            value = refNumber,
                            onValueChange = { refNumber = it },
                            label = { Text("No. Bukti / Kuitansi / Invoice (Opsional)") },
                            placeholder = { Text("Contoh: KWT-0021 / INV-09") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // === STEP 2: CLASSIFICATION (ACCOUNTING LOGIC) ===
                        Text(
                            "Transaksi ini berkaitan dengan apa?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Classification Chips
                        val classifications = listOf(
                            "PROJECT" to "Proyek Konstruksi / Bore Pile",
                            "OPERATIONAL" to "Operasional Kantor",
                            "DEBT" to "Utang / Pinjaman",
                            "RECEIVABLE" to "Piutang Usaha",
                            "CAPITAL" to "Modal Pemilik (Equity)",
                            "ASSET" to "Pembelian Aset Tetap",
                            "OTHER" to "Lainnya"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            classifications.forEach { (key, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (classification == key) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { classification = key }
                                        .testTag("classification_$key")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (classification == key),
                                            onClick = { classification = key }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(label, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // Project Selector (If Classification == PROJECT)
                        if (classification == "PROJECT") {
                            HorizontalDivider()
                            Text("Pilih Proyek Aktif:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                            if (activeProjects.isEmpty()) {
                                Text(
                                    "Tidak ada proyek aktif. Silakan buat proyek baru terlebih dahulu.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    activeProjects.forEach { prj ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedProjectId == prj.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedProjectId = prj.id }
                                                .testTag("project_option_${prj.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = (selectedProjectId == prj.id),
                                                    onClick = { selectedProjectId = prj.id }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(prj.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Text(
                                                        "${prj.client} • Kontrak: ${Formatters.rupiah(prj.contractValue)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Category Dropdown / Selection
                        HorizontalDivider()
                        Text("Kategori Transaksi:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (category == cat) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { category = cat }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (category == cat),
                                            onClick = { category = cat }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        // Accounting Notification Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Prinsip Akuntansi Otomatis:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                val note = when (classification) {
                                    "CAPITAL" -> "Uang Masuk Modal Pemilik menambah Saldo Kas & Ekuitas, TIDAK dihitung sebagai Pendapatan/Laba."
                                    "DEBT" -> if (transactionType == "MONEY_IN") "Pencairan pinjaman menambah Saldo Kas & Liabilitas (Utang), TIDAK dihitung Pendapatan." else "Pembayaran cicilan pokok utang mengurangi Kas & Utang, TIDAK dihitung Beban Operasional."
                                    "ASSET" -> "Pembelian aset rig/alat berat menambah Aset Tetap, TIDAK langsung dibebankan ke Laba Rugi."
                                    "PROJECT" -> if (transactionType == "MONEY_IN") "Penerimaan proyek dicatat ke Arus Kas dan Pendapatan Proyek." else "Biaya operasional lapangan mengurangi kas dan langsung dialokasikan ke Beban Pokok Proyek."
                                    else -> "Transaksi akan otomatis masuk ke Arus Kas dan Buku Besar yang sesuai."
                                }
                                Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Navigation Buttons (Back, Next, Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step == 2) {
                        OutlinedButton(
                            onClick = { step = 1; errorMessage = null },
                            modifier = Modifier.testTag("step_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kembali")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (step == 1) {
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    errorMessage = "Nominal transaksi harus lebih dari Rp 0"
                                } else if (description.trim().isEmpty()) {
                                    errorMessage = "Deskripsi transaksi wajib diisi"
                                } else if (dateText.trim().isEmpty()) {
                                    errorMessage = "Tanggal transaksi wajib diisi"
                                } else {
                                    errorMessage = null
                                    step = 2
                                }
                            },
                            modifier = Modifier.testTag("step_next_button")
                        ) {
                            Text("Lanjut ke Klasifikasi")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                val proj = if (classification == "PROJECT") activeProjects.find { it.id == selectedProjectId } else null

                                if (classification == "PROJECT" && proj == null) {
                                    errorMessage = "Wajib memilih proyek aktif untuk transaksi proyek"
                                } else {
                                    onSubmit(
                                        dateText,
                                        transactionType,
                                        amt,
                                        selectedAccountId,
                                        selectedAccount?.name ?: "Kas",
                                        classification,
                                        category,
                                        description,
                                        proj?.id,
                                        proj?.name,
                                        paymentMethod,
                                        refNumber
                                    )
                                }
                            },
                            modifier = Modifier.testTag("save_transaction_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Transaksi")
                        }
                    }
                }
            }
        }
    }
}
