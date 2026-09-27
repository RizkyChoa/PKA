package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProjectEntity
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.ui.theme.ColorTransfer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<AccountEntity>,
    activeProjects: List<ProjectEntity>, // STRICT: Only active/planning projects! Completed projects are excluded.
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        date: String,
        type: String,
        amount: Double,
        description: String,
        paymentMethod: String,
        sourceAccountId: Long,
        destinationAccountId: Long?,
        categoryId: Long,
        classification: String,
        projectId: Long?,
        costGroup: String
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Step 1 Fields
    var date by remember { mutableStateOf(todayStr) }
    var type by remember { mutableStateOf("MONEY_OUT") } // MONEY_IN, MONEY_OUT, TRANSFER
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("TRANSFER") } // TRANSFER, CASH, GIRO
    var selectedAccountId by remember {
        mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L)
    }
    var destinationAccountId by remember {
        mutableLongStateOf(accounts.getOrNull(1)?.id ?: 2L)
    }

    // Step 2 Fields (Classification)
    var classification by remember { mutableStateOf("PROJECT") } // PROJECT, OPERATIONAL, DEBT, RECEIVABLE, EQUITY, TRANSFER
    var selectedProjectId by remember {
        mutableLongStateOf(activeProjects.firstOrNull()?.id ?: 0L)
    }
    var selectedCostGroup by remember { mutableStateOf("MATERIAL") } // MATERIAL, LABOR, MOBILIZATION, FUEL, EQUIPMENT, MAINTENANCE, OTHER
    var selectedCategoryId by remember {
        mutableLongStateOf(categories.firstOrNull()?.id ?: 1L)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                            text = if (step == 1) "Input Transaksi (Tahap 1/2)" else "Klasifikasi Transaksi (Tahap 2/2)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (step == 1) "Masukkan data dasar nominal & rekening" else "Hubungkan transaksi ke Proyek atau Operasional",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_trx_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (step == 1) {
                    // STEP 1 CONTENT
                    // Transaction Type Selector
                    Text(
                        text = "Jenis Transaksi",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Uang Keluar
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = "MONEY_OUT" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == "MONEY_OUT") ColorExpense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ColorExpense)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Uang Keluar", fontWeight = FontWeight.Bold, color = if (type == "MONEY_OUT") ColorExpense else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }

                        // Uang Masuk
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = "MONEY_IN" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == "MONEY_IN") ColorProfit.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ColorProfit)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Uang Masuk", fontWeight = FontWeight.Bold, color = if (type == "MONEY_IN") ColorProfit else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }

                        // Transfer Internal
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = "TRANSFER" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == "TRANSFER") ColorTransfer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = ColorTransfer)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Mutasi Kas", fontWeight = FontWeight.Bold, color = if (type == "TRANSFER") ColorTransfer else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nominal
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal Transaksi (Rp)") },
                        placeholder = { Text("Contoh: 15000000") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tanggal
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("date_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deskripsi
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Keterangan / Deskripsi Transaksi") },
                        placeholder = { Text("Contoh: Beli Semen Cor K-350 / Solar Rig") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("desc_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rekening Sumber
                    var expandedSource by remember { mutableStateOf(false) }
                    val currentAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                    ExposedDropdownMenuBox(
                        expanded = expandedSource,
                        onExpandedChange = { expandedSource = it }
                    ) {
                        OutlinedTextField(
                            value = currentAccountName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (type == "TRANSFER") "Rekening Asal" else "Rekening / Kas") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSource) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSource,
                            onDismissRequest = { expandedSource = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type})") },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expandedSource = false
                                    }
                                )
                            }
                        }
                    }

                    if (type == "TRANSFER") {
                        Spacer(modifier = Modifier.height(12.dp))
                        var expandedDest by remember { mutableStateOf(false) }
                        val destAccountName = accounts.find { it.id == destinationAccountId }?.name ?: "Pilih Rekening Tujuan"
                        ExposedDropdownMenuBox(
                            expanded = expandedDest,
                            onExpandedChange = { expandedDest = it }
                        ) {
                            OutlinedTextField(
                                value = destAccountName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Rekening Tujuan") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDest) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDest,
                                onDismissRequest = { expandedDest = false }
                            ) {
                                accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${acc.type})") },
                                        onClick = {
                                            destinationAccountId = acc.id
                                            expandedDest = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metode Pembayaran
                    var expandedMethod by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedMethod,
                        onExpandedChange = { expandedMethod = it }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Metode Pembayaran") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMethod,
                            onDismissRequest = { expandedMethod = false }
                        ) {
                            listOf("TRANSFER", "CASH", "GIRO").forEach { meth ->
                                DropdownMenuItem(
                                    text = { Text(meth) },
                                    onClick = {
                                        paymentMethod = meth
                                        expandedMethod = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Next Button
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0) {
                                errorMessage = "Nominal harus lebih besar dari 0"
                                return@Button
                            }
                            if (description.isBlank()) {
                                errorMessage = "Deskripsi / keterangan transaksi wajib diisi"
                                return@Button
                            }
                            if (type == "TRANSFER") {
                                if (selectedAccountId == destinationAccountId) {
                                    errorMessage = "Rekening asal dan tujuan tidak boleh sama"
                                    return@Button
                                }
                                // Direct save for Transfer Internal
                                onSaveTransaction(
                                    date,
                                    type,
                                    amt,
                                    description,
                                    paymentMethod,
                                    selectedAccountId,
                                    destinationAccountId,
                                    21L,
                                    "INTERNAL_TRANSFER",
                                    null,
                                    ""
                                )
                                onDismiss()
                            } else {
                                errorMessage = null
                                step = 2
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("next_step_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (type == "TRANSFER") "Simpan Mutasi Antar Kas" else "Lanjut ke Klasifikasi (Step 2)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    // STEP 2 CONTENT (CLASSIFICATION)
                    Text(
                        text = "Transaksi ini berkaitan dengan apa?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Classification Chips / Cards
                    val classificationOptions = listOf(
                        "PROJECT" to "Proyek Bore Pile",
                        "OPERATIONAL" to "Operasional Kantor",
                        "DEBT" to "Utang / Pinjaman",
                        "EQUITY" to "Modal Pemilik"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        classificationOptions.forEach { (key, label) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { classification = key },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (classification == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = if (classification == key) FontWeight.Bold else FontWeight.Normal,
                                        color = if (classification == key) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (classification == key) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (classification == "PROJECT") {
                        // STRICT RULE: Only active projects are listed!
                        Text(
                            text = "Pilih Proyek Bore Pile Aktif:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Proyek yang sudah berstatus 'Selesai' tidak dimunculkan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (activeProjects.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text(
                                    text = "Tidak ada proyek aktif. Silakan buat proyek baru atau buka kembali proyek yang selesai.",
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            var expandedProject by remember { mutableStateOf(false) }
                            val activeProj = activeProjects.find { it.id == selectedProjectId } ?: activeProjects.first()
                            ExposedDropdownMenuBox(
                                expanded = expandedProject,
                                onExpandedChange = { expandedProject = it }
                            ) {
                                OutlinedTextField(
                                    value = "${activeProj.name} (${activeProj.projectCode})",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Proyek yang Sedang Berjalan") },
                                    leadingIcon = { Icon(Icons.Default.Construction, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedProject,
                                    onDismissRequest = { expandedProject = false }
                                ) {
                                    activeProjects.forEach { proj ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(proj.name, fontWeight = FontWeight.Bold)
                                                    Text("${proj.clientName} • ${proj.projectCode}", style = MaterialTheme.typography.bodySmall)
                                                }
                                            },
                                            onClick = {
                                                selectedProjectId = proj.id
                                                expandedProject = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (type == "MONEY_OUT") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Kategori Biaya HPP Bore Pile:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val costGroupOptions = listOf(
                                "MATERIAL" to "Material (Beton Ready Mix, Besi Tulangan, Bentonite)",
                                "LABOR" to "Upah (Operator Rig, Mandor & Kenek Bor)",
                                "MOBILIZATION" to "Mobilisasi & Demobilisasi Rig Bore Pile",
                                "FUEL" to "BBM Solar & Pelumas Alat Berat",
                                "EQUIPMENT" to "Sewa Alat / Genset / Casing Bor",
                                "MAINTENANCE" to "Perawatan & Mata Bor Lapangan",
                                "OTHER" to "Koordinasi Lingkungan & Lainnya"
                            )

                            var expandedCost by remember { mutableStateOf(false) }
                            val currentCostLabel = costGroupOptions.find { it.first == selectedCostGroup }?.second ?: "Material"
                            ExposedDropdownMenuBox(
                                expanded = expandedCost,
                                onExpandedChange = { expandedCost = it }
                            ) {
                                OutlinedTextField(
                                    value = currentCostLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Grup Biaya HPP") },
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCost) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCost,
                                    onDismissRequest = { expandedCost = false }
                                ) {
                                    costGroupOptions.forEach { (grp, lbl) ->
                                        DropdownMenuItem(
                                            text = { Text(lbl) },
                                            onClick = {
                                                selectedCostGroup = grp
                                                expandedCost = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Money In Project (DP / Termin)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Jenis Penerimaan Proyek:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("DP Proyek" to 1L, "Termin Proyek" to 2L, "Pelunasan" to 3L).forEach { (lbl, catId) ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedCategoryId = catId },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedCategoryId == catId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = lbl,
                                            modifier = Modifier.padding(10.dp),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else if (classification == "OPERATIONAL") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Biaya Operasional Umum / Kantor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons (Back & Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kembali")
                        }

                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                val projId = if (classification == "PROJECT") {
                                    if (activeProjects.isEmpty()) {
                                        errorMessage = "Pilih proyek aktif yang valid"
                                        return@Button
                                    }
                                    if (selectedProjectId == 0L) activeProjects.first().id else selectedProjectId
                                } else null

                                onSaveTransaction(
                                    date,
                                    type,
                                    amt,
                                    description,
                                    paymentMethod,
                                    selectedAccountId,
                                    null,
                                    selectedCategoryId,
                                    classification,
                                    projId,
                                    if (classification == "PROJECT") selectedCostGroup else ""
                                )
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("save_transaction_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Transaksi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
