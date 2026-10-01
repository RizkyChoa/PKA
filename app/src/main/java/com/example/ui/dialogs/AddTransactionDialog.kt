package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProfitPartnerEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.ui.theme.ColorTransfer
import com.example.util.AppDatePickerDialog
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    transactionToEdit: TransactionEntity? = null,
    accounts: List<AccountEntity>,
    activeProjects: List<ProjectEntity>,
    categories: List<CategoryEntity>,
    profitPartners: List<ProfitPartnerEntity> = emptyList(),
    payables: List<PayableEntity> = emptyList(),
    receivables: List<ReceivableEntity> = emptyList(),
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
        costGroup: String,
        receivableId: Long?,
        payableId: Long?,
        profitPartnerId: Long?,
        profitPartnerName: String?
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val todayStr = remember { CurrencyFormatter.todayString() }
    val yesterdayStr = remember { CurrencyFormatter.yesterdayString() }

    // Date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Step 1 Fields
    var date by remember { mutableStateOf(transactionToEdit?.date ?: todayStr) }
    var type by remember { mutableStateOf(transactionToEdit?.type ?: "MONEY_OUT") }
    var amountText by remember {
        mutableStateOf(
            if (transactionToEdit != null && transactionToEdit.amount > 0)
                CurrencyFormatter.formatInput(transactionToEdit.amount.toLong().toString())
            else ""
        )
    }
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var paymentMethod by remember { mutableStateOf(transactionToEdit?.paymentMethod ?: "TRANSFER") }
    var selectedAccountId by remember {
        mutableLongStateOf(transactionToEdit?.sourceAccountId ?: (accounts.firstOrNull()?.id ?: 1L))
    }
    var destinationAccountId by remember {
        mutableLongStateOf(transactionToEdit?.destinationAccountId ?: (accounts.getOrNull(1)?.id ?: 2L))
    }

    // Step 2 Fields (Classification & Extra links)
    var classification by remember { mutableStateOf(transactionToEdit?.classification ?: "PROJECT") }
    var selectedProjectId by remember {
        mutableLongStateOf(transactionToEdit?.projectId ?: (activeProjects.firstOrNull()?.id ?: 0L))
    }
    var selectedCostGroup by remember {
        mutableStateOf(transactionToEdit?.costGroup?.ifBlank { "MATERIAL" } ?: "MATERIAL")
    }
    var selectedCategoryId by remember {
        mutableLongStateOf(transactionToEdit?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }

    // Extra linking
    var selectedProfitPartnerId by remember {
        mutableStateOf(transactionToEdit?.profitPartnerId ?: profitPartners.firstOrNull()?.id)
    }
    var selectedProfitPartnerName by remember {
        mutableStateOf(transactionToEdit?.profitPartnerName ?: profitPartners.firstOrNull()?.name)
    }
    var selectedPayableId by remember { mutableStateOf(transactionToEdit?.payableId) }
    var selectedReceivableId by remember { mutableStateOf(transactionToEdit?.receivableId) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = date,
            onDateSelected = { picked -> date = picked },
            onDismiss = { showDatePicker = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 16.dp),
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
                    // 1. TOP HEADER (Sticky)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (transactionToEdit != null) {
                                    if (step == 1) "Edit Transaksi (1/2)" else "Klasifikasi Edit (2/2)"
                                } else {
                                    if (step == 1) "Input Transaksi (Tahap 1/2)" else "Klasifikasi Transaksi (Tahap 2/2)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (step == 1) "Data nominal & rekening kas" else "Tentukan peruntukan transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_trx_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. SCROLLABLE BODY
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
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
                            // ========================================================
                            // STEP 1 CONTENT: NOMINAL, JENIS, TANGGAL, REKENING
                            // ========================================================
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
                                        Text(
                                            "Uang Keluar",
                                            fontWeight = FontWeight.Bold,
                                            color = if (type == "MONEY_OUT") ColorExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
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
                                        Text(
                                            "Uang Masuk",
                                            fontWeight = FontWeight.Bold,
                                            color = if (type == "MONEY_IN") ColorProfit else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
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
                                        Text(
                                            "Mutasi Kas",
                                            fontWeight = FontWeight.Bold,
                                            color = if (type == "TRANSFER") ColorTransfer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Nominal with auto thousand formatting (e.g. 20.000, 5.000.000)
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    amountText = CurrencyFormatter.formatInput(input)
                                },
                                label = { Text("Nominal Transaksi (Rp)") },
                                placeholder = { Text("Contoh: 5.000.000") },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("amount_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tanggal Transaksi (with calendar picker & quick chips)
                            Column {
                                OutlinedTextField(
                                    value = date,
                                    onValueChange = { input ->
                                        date = CurrencyFormatter.formatDateInput(input)
                                    },
                                    label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                                    placeholder = { Text("2026-09-30 atau ketik 8 angka") },
                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih Kalender", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("date_input"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(
                                        onClick = { date = todayStr },
                                        label = { Text("Hari Ini", fontSize = 11.sp) }
                                    )
                                    AssistChip(
                                        onClick = { date = yesterdayStr },
                                        label = { Text("Kemarin", fontSize = 11.sp) }
                                    )
                                    AssistChip(
                                        onClick = { showDatePicker = true },
                                        label = { Text("Buka Kalender", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Deskripsi
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Keterangan / Uraian Transaksi") },
                                placeholder = { Text("Contoh: Beli Semen Cor K-350 / Solar Rig / Pengambilan Profit") },
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
                                            text = { Text("${acc.name} (${acc.type}) - Saldo: ${formatRupiah(acc.currentBalance)}") },
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
                                        accounts.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text("${acc.name} (${acc.type}) - Saldo: ${formatRupiah(acc.currentBalance)}") },
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

                            // Metode Bayar
                            Text("Metode Pembayaran:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("TRANSFER" to "Transfer Bank", "CASH" to "Tunai / Kas", "GIRO" to "Giro / Cek").forEach { (mKey, mLbl) ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { paymentMethod = mKey },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (paymentMethod == mKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = mLbl,
                                            modifier = Modifier.padding(8.dp),
                                            fontSize = 11.sp,
                                            fontWeight = if (paymentMethod == mKey) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            // ========================================================
                            // STEP 2 CONTENT (CLASSIFICATION & LINKING)
                            // ========================================================
                            Text(
                                text = "Transaksi ini berkaitan dengan apa?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Classification Chips / Cards
                            val classificationOptions = listOf(
                                "PROJECT" to "Proyek Bore Pile (HPP / Pendapatan)",
                                "OPERATIONAL" to "Operasional Kantor / Workshop",
                                "PENGAMBILAN_PROFIT" to "Pengambilan Profit / Bagi Hasil",
                                "DEBT" to "Utang / Pinjaman Usaha",
                                "RECEIVABLE" to "Piutang Proyek / Klien",
                                "EQUITY" to "Modal Pemilik / Setoran Modal"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                classificationOptions.forEach { (key, label) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                classification = key
                                                if (key == "PENGAMBILAN_PROFIT") {
                                                    type = "MONEY_OUT"
                                                    selectedCategoryId = 23L
                                                    if (description.isBlank() || description.startsWith("Pengambilan")) {
                                                        description = "Pengambilan Profit - ${selectedProfitPartnerName ?: "Partner"}"
                                                    }
                                                }
                                            },
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

                            // 1. DETAIL PROYEK
                            if (classification == "PROJECT") {
                                Text(
                                    text = "Pilih Proyek Bore Pile Aktif:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
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
                                        "MATERIAL" to "Material (Ready Mix, Besi Tulangan, Bentonite)",
                                        "LABOR_MANDOR" to "Upah Mandor & Supervisi Lapangan",
                                        "LABOR_WORKER" to "Upah Pekerja / Operator Rig & Tenaga Bor",
                                        "CONSUMPTION" to "Konsumsi Anggota Bore pile",
                                        "MOBILIZATION" to "Mobilisasi & Demobilisasi Rig Bore Pile",
                                        "FUEL" to "BBM Solar & Pelumas Alat Berat",
                                        "EQUIPMENT" to "Sewa Genset, Casing Bor & Kompresor",
                                        "MAINTENANCE" to "Perawatan & Mata Bor Lapangan",
                                        "OTHER" to "Koordinasi Lapangan & Perizinan"
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
                                    // Money In Project (DP / Termin / Progress / Pelunasan)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Jenis Penerimaan Pendapatan Proyek:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            "Uang Muka / DP Proyek" to 1L,
                                            "Pembayaran Termin Proyek" to 2L,
                                            "Pembayaran Progress Proyek" to 4L,
                                            "Pelunasan Kontrak Proyek" to 3L
                                        ).forEach { (lbl, catId) ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedCategoryId = catId },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selectedCategoryId == catId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = lbl, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    if (selectedCategoryId == catId) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (classification == "PENGAMBILAN_PROFIT") {
                                // 2. PENGAMBILAN PROFIT / BAGI HASIL
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Jatah Bagi Hasil Penerima Profit:",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Pilih ke jatah siapa penarikan profit ini akan dipotongkan pada Laporan Pembagian Profit.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (profitPartners.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text(
                                            text = "Belum ada mitra/partner penerima profit terdaftar. Silakan tambahkan mitra di Laporan Pembagian Profit.",
                                            modifier = Modifier.padding(12.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Pilih Penerima Profit:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    var expandedPartner by remember { mutableStateOf(false) }
                                    val currentPartner = profitPartners.find { it.id == selectedProfitPartnerId } ?: profitPartners.first()

                                    ExposedDropdownMenuBox(
                                        expanded = expandedPartner,
                                        onExpandedChange = { expandedPartner = it }
                                    ) {
                                        OutlinedTextField(
                                            value = "${currentPartner.name} (${currentPartner.sharePercentage.toInt()}%)",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Nama Penerima Profit (Dipotongkan Ke)") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPartner) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedPartner,
                                            onDismissRequest = { expandedPartner = false }
                                        ) {
                                            profitPartners.forEach { partner ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(partner.name, fontWeight = FontWeight.Bold)
                                                            Text("${partner.sharePercentage.toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedProfitPartnerId = partner.id
                                                        selectedProfitPartnerName = partner.name
                                                        description = "Pengambilan Profit - ${partner.name}"
                                                        expandedPartner = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (classification == "DEBT") {
                                // 3. UTANG / PINJAMAN
                                Spacer(modifier = Modifier.height(8.dp))
                                if (type == "MONEY_OUT" && payables.isNotEmpty()) {
                                    Text("Pilih Utang Usaha / Supplier yang Dilunasi:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    var expandedPay by remember { mutableStateOf(false) }
                                    val currentPay = payables.find { it.id == selectedPayableId }
                                    val payLabel = currentPay?.let { "${it.creditorName} - Sisa: ${formatRupiah(it.remainingAmount)}" } ?: "Tanpa Tautan / Utang Baru"

                                    ExposedDropdownMenuBox(
                                        expanded = expandedPay,
                                        onExpandedChange = { expandedPay = it }
                                    ) {
                                        OutlinedTextField(
                                            value = payLabel,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Tautkan ke Daftar Utang") },
                                            leadingIcon = { Icon(Icons.Default.Handshake, contentDescription = null) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPay) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedPay,
                                            onDismissRequest = { expandedPay = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Tanpa Tautan Langsung / Utang Lain") },
                                                onClick = {
                                                    selectedPayableId = null
                                                    expandedPay = false
                                                }
                                            )
                                            payables.filter { it.remainingAmount > 0 }.forEach { p ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(p.creditorName, fontWeight = FontWeight.Bold)
                                                            Text("${p.description} • Sisa: ${formatRupiah(p.remainingAmount)}", style = MaterialTheme.typography.bodySmall, color = ColorExpense)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedPayableId = p.id
                                                        expandedPay = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("Pencatatan Pinjaman Modal / Utang Baru", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else if (classification == "RECEIVABLE") {
                                // 4. PIUTANG
                                Spacer(modifier = Modifier.height(8.dp))
                                if (type == "MONEY_IN" && receivables.isNotEmpty()) {
                                    Text("Pilih Piutang yang Diterima Pembayarannya:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    var expandedRec by remember { mutableStateOf(false) }
                                    val currentRec = receivables.find { it.id == selectedReceivableId }
                                    val recLabel = currentRec?.let { "${it.clientName} (${it.projectName}) - Sisa: ${formatRupiah(it.remainingAmount)}" } ?: "Tanpa Tautan / Piutang Lain"

                                    ExposedDropdownMenuBox(
                                        expanded = expandedRec,
                                        onExpandedChange = { expandedRec = it }
                                    ) {
                                        OutlinedTextField(
                                            value = recLabel,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Tautkan ke Daftar Piutang") },
                                            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRec) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedRec,
                                            onDismissRequest = { expandedRec = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Tanpa Tautan Langsung / Piutang Lain") },
                                                onClick = {
                                                    selectedReceivableId = null
                                                    expandedRec = false
                                                }
                                            )
                                            receivables.filter { it.remainingAmount > 0 }.forEach { r ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text("${r.clientName} - ${r.projectName}", fontWeight = FontWeight.Bold)
                                                            Text("Sisa: ${formatRupiah(r.remainingAmount)}", style = MaterialTheme.typography.bodySmall, color = ColorProfit)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedReceivableId = r.id
                                                        expandedRec = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // 3. STICKY BOTTOM ACTION BAR (Never obscured by navigation bar)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (step == 1) {
                                    OutlinedButton(
                                        onClick = onDismiss,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Text("Batal")
                                    }

                                    Button(
                                        onClick = {
                                            val amt = CurrencyFormatter.parseInput(amountText)
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
                                                onSaveTransaction(
                                                    date,
                                                    type,
                                                    amt,
                                                    description,
                                                    paymentMethod,
                                                    selectedAccountId,
                                                    destinationAccountId,
                                                    24L,
                                                    "INTERNAL_TRANSFER",
                                                    null,
                                                    "",
                                                    null,
                                                    null,
                                                    null,
                                                    null
                                                )
                                                onDismiss()
                                            } else {
                                                errorMessage = null
                                                step = 2
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(44.dp)
                                            .testTag("next_step_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (type == "TRANSFER") "Simpan" else "Lanjut",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kembali")
                                    }

                                    Button(
                                        onClick = {
                                            val amt = CurrencyFormatter.parseInput(amountText)
                                            val projId = if (classification == "PROJECT") {
                                                if (activeProjects.isEmpty()) {
                                                    errorMessage = "Pilih proyek aktif yang valid"
                                                    return@Button
                                                }
                                                if (selectedProjectId == 0L) activeProjects.first().id else selectedProjectId
                                            } else null

                                            val partnerId = if (classification == "PENGAMBILAN_PROFIT") selectedProfitPartnerId else null
                                            val partnerName = if (classification == "PENGAMBILAN_PROFIT") {
                                                profitPartners.find { it.id == selectedProfitPartnerId }?.name ?: selectedProfitPartnerName
                                            } else null

                                            val finalCostGroup = if (classification == "PROJECT") {
                                                selectedCostGroup
                                            } else if (classification == "PENGAMBILAN_PROFIT") {
                                                "PENGAMBILAN_PROFIT"
                                            } else ""

                                            val catId = if (classification == "PENGAMBILAN_PROFIT") {
                                                23L
                                            } else {
                                                selectedCategoryId
                                            }

                                            onSaveTransaction(
                                                date,
                                                type,
                                                amt,
                                                description,
                                                paymentMethod,
                                                selectedAccountId,
                                                null,
                                                catId,
                                                classification,
                                                projId,
                                                finalCostGroup,
                                                selectedReceivableId,
                                                selectedPayableId,
                                                partnerId,
                                                partnerName
                                            )
                                            onDismiss()
                                        },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(44.dp)
                                            .testTag("save_transaction_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Selesai", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
