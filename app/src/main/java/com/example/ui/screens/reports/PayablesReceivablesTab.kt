package com.example.ui.screens.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.CreditorLedger
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.ReceivableEntity
import com.example.data.model.formatNumberWithDots
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.CurrencyFormatter
import com.example.util.PrintHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayablesReceivablesTab(
    projectSummaries: List<ProjectFinancialSummary>,
    receivables: List<ReceivableEntity>,
    payables: List<PayableEntity>,
    creditorLedgers: List<CreditorLedger> = emptyList(),
    accounts: List<AccountEntity>,
    onPayPayablesBatch: (payableIds: List<Long>, sourceAccountId: Long, date: String, method: String, notes: String) -> Unit,
    onPayReceivablesBatch: (receivableIds: List<Long>, destinationAccountId: Long, date: String, method: String, notes: String) -> Unit,
    onPayCreditor: (creditorName: String, amount: Double, sourceAccountId: Long, date: String, method: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
    onCreatePayable: (creditorName: String, type: String, description: String, totalAmount: Double, dueDate: String, destinationAccountId: Long?, transactionDate: String) -> Unit,
    onDeletePayable: (id: Long) -> Unit,
    onCreateReceivable: (clientName: String, projectName: String, invoiceNumber: String, description: String, totalAmount: Double, dueDate: String, projectId: Long?) -> Unit,
    onDeleteReceivable: (id: Long) -> Unit,
    onReceiveProjectPayment: (projectId: Long, amount: Double, destinationAccountId: Long, date: String, method: String, notes: String) -> Unit
) {
    var subTabFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Piutang Saja, 2: Utang Saja
    var debtFilterStatus by remember { mutableIntStateOf(0) } // 0: Semua, 1: Belum Lunas, 2: Lunas
    var debtSearchQuery by remember { mutableStateOf("") }

    // Multi-Selection State for Receivables
    val selectedReceivableIds = remember { mutableStateListOf<Long>() }

    // Dialog States
    var showAddPayableDialog by remember { mutableStateOf(false) }
    var prefilledCreditorName by remember { mutableStateOf("") }
    var creditorToPay by remember { mutableStateOf<CreditorLedger?>(null) }
    var showAddReceivableDialog by remember { mutableStateOf(false) }
    var showBatchReceiveModal by remember { mutableStateOf(false) }

    // Single Project Payment Modal
    var projectToReceivePayment by remember { mutableStateOf<ProjectFinancialSummary?>(null) }

    // Sisa Piutang Proyek (Proyek yang sisa kontraknya belum cair)
    val projectsWithReceivable = remember(projectSummaries) {
        projectSummaries.filter { it.remainingContractBalance > 0 }
    }

    val totalProjectReceivables = remember(projectsWithReceivable) {
        projectsWithReceivable.sumOf { it.remainingContractBalance }
    }

    val unpaidReceivables = remember(receivables) {
        receivables.filter { it.remainingAmount > 0 }
    }

    val totalOtherReceivables = remember(unpaidReceivables) {
        unpaidReceivables.sumOf { it.remainingAmount }
    }

    val totalAllReceivables = totalProjectReceivables + totalOtherReceivables

    val totalAllPayables = remember(creditorLedgers, payables) {
        if (creditorLedgers.isNotEmpty()) {
            creditorLedgers.sumOf { it.remainingBalance }
        } else {
            payables.sumOf { it.remainingAmount }
        }
    }

    val existingCreditorNames = remember(creditorLedgers, payables) {
        (creditorLedgers.map { it.creditorName } + payables.map { it.creditorName }).distinct().filter { it.isNotBlank() }
    }

    // Filtered creditor ledgers
    val filteredCreditorLedgers = remember(creditorLedgers, debtFilterStatus, debtSearchQuery) {
        creditorLedgers.filter { ledger ->
            val matchStatus = when (debtFilterStatus) {
                1 -> !ledger.isSettled
                2 -> ledger.isSettled
                else -> true
            }
            val matchQuery = if (debtSearchQuery.isBlank()) true else {
                ledger.creditorName.contains(debtSearchQuery, ignoreCase = true) ||
                ledger.loans.any { it.description.contains(debtSearchQuery, ignoreCase = true) }
            }
            matchStatus && matchQuery
        }
    }

    // Modals
    if (showAddPayableDialog) {
        AddPayableModal(
            accounts = accounts,
            initialCreditorName = prefilledCreditorName,
            existingCreditorNames = existingCreditorNames,
            onDismiss = {
                showAddPayableDialog = false
                prefilledCreditorName = ""
            },
            onSave = { name, type, desc, amt, due, accId, trxDate ->
                onCreatePayable(name, type, desc, amt, due, accId, trxDate)
                showAddPayableDialog = false
                prefilledCreditorName = ""
            }
        )
    }

    if (creditorToPay != null) {
        PayCreditorModal(
            creditor = creditorToPay!!,
            accounts = accounts,
            onDismiss = { creditorToPay = null },
            onConfirm = { amt, accId, date, method, notes ->
                onPayCreditor(creditorToPay!!.creditorName, amt, accId, date, method, notes)
                creditorToPay = null
            }
        )
    }

    if (showAddReceivableDialog) {
        AddReceivableModal(
            projectSummaries = projectSummaries,
            onDismiss = { showAddReceivableDialog = false },
            onSave = { client, projName, invNum, desc, amt, due, pId ->
                onCreateReceivable(client, projName, invNum, desc, amt, due, pId)
                showAddReceivableDialog = false
            }
        )
    }

    if (projectToReceivePayment != null) {
        ProjectPaymentModal(
            projectSummary = projectToReceivePayment!!,
            accounts = accounts,
            onDismiss = { projectToReceivePayment = null },
            onSave = { pId, amt, accId, date, method, notes ->
                onReceiveProjectPayment(pId, amt, accId, date, method, notes)
                projectToReceivePayment = null
            }
        )
    }

    if (showBatchReceiveModal) {
        val selectedRecList = unpaidReceivables.filter { selectedReceivableIds.contains(it.id) }
        val sumToReceive = selectedRecList.sumOf { it.remainingAmount }

        BatchSettlementModal(
            title = "Terima Pelunasan ${selectedRecList.size} Piutang",
            subtitle = "Pilih rekening kas penampung dana masuk:",
            totalAmount = sumToReceive,
            accounts = accounts,
            onDismiss = { showBatchReceiveModal = false },
            onConfirm = { accountId, date, method, notes ->
                onPayReceivablesBatch(selectedReceivableIds.toList(), accountId, date, method, notes)
                selectedReceivableIds.clear()
                showBatchReceiveModal = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // FILTER CHIPS UTAMA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = subTabFilter == 0,
                    onClick = { subTabFilter = 0 },
                    label = { Text("Semua", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = subTabFilter == 1,
                    onClick = { subTabFilter = 1 },
                    label = { Text("Piutang (${formatRupiah(totalAllReceivables)})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = subTabFilter == 2,
                    onClick = { subTabFilter = 2 },
                    label = { Text("Hutang & Pinjaman (${formatRupiah(totalAllPayables)})", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ========================================================
                // 1. PIUTANG SISA KONTRAK PROYEK BELUM CAIR
                // ========================================================
                if (subTabFilter == 0 || subTabFilter == 1) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("PIUTANG KONTRAK PROYEK", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("Termin / Sisa Tagihan Kontrak Bore Pile", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    formatRupiah(totalProjectReceivables),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (projectsWithReceivable.isEmpty()) {
                        item {
                            Text("Semua pembayaran kontrak proyek telah lunas 100%.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(projectsWithReceivable) { ps ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(ps.project.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ps.project.projectCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Klien: ${ps.project.clientName} • Lokasi: ${ps.project.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Nilai Kontrak: ${formatRupiah(ps.contractAmount)}", style = MaterialTheme.typography.labelSmall)
                                        Text("Telah Cair: ${formatRupiah(ps.cashReceived)}", style = MaterialTheme.typography.labelSmall, color = ColorProfit)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Sisa Piutang: ${formatRupiah(ps.remainingContractBalance)}", fontWeight = FontWeight.Bold, color = ColorExpense, fontSize = 13.sp)
                                        Button(
                                            onClick = { projectToReceivePayment = ps },
                                            modifier = Modifier.height(32.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = "Terima Termin", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Terima Termin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ========================================================
                    // 2. PIUTANG NON-PROYEK / INVOICE KLIEN LAINNYA
                    // ========================================================
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PIUTANG KLIEN LAINNYA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Button(
                                onClick = { showAddReceivableDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Piutang", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (unpaidReceivables.isEmpty()) {
                        item {
                            Text("Tidak ada piutang klien lainnya tercatat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(unpaidReceivables) { rec ->
                            val isChecked = selectedReceivableIds.contains(rec.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedReceivableIds.remove(rec.id) else selectedReceivableIds.add(rec.id)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedReceivableIds.add(rec.id) else selectedReceivableIds.remove(rec.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(rec.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(rec.status, fontWeight = FontWeight.Bold, color = if (rec.status == "PAID") ColorProfit else ColorExpense, fontSize = 11.sp)
                                        }
                                        Text("Invoice: ${rec.invoiceNumber} • Jatuh Tempo: ${rec.dueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(rec.description, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total: ${formatRupiah(rec.totalAmount)}", style = MaterialTheme.typography.labelSmall)
                                            Text("Sisa: ${formatRupiah(rec.remainingAmount)}", fontWeight = FontWeight.Bold, color = ColorProfit, fontSize = 13.sp)
                                        }
                                    }
                                    IconButton(onClick = { onDeleteReceivable(rec.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // ========================================================
                // 3. DETAIL LAPORAN HUTANG / PINJAMAN (LEDGER PER PEMBERI)
                // ========================================================
                if (subTabFilter == 0 || subTabFilter == 2) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "LAPORAN & LEDGER HUTANG",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Total Sisa Hutang: ${formatRupiah(totalAllPayables)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ColorExpense,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    prefilledCreditorName = ""
                                    showAddPayableDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Hutang", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SUB-FILTER: Semua / Belum Lunas / Lunas & Search
                    item {
                        val countAll = creditorLedgers.size
                        val countUnpaid = creditorLedgers.count { !it.isSettled }
                        val countPaid = creditorLedgers.count { it.isSettled }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = debtFilterStatus == 0,
                                    onClick = { debtFilterStatus = 0 },
                                    label = { Text("Semua ($countAll)", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = debtFilterStatus == 1,
                                    onClick = { debtFilterStatus = 1 },
                                    label = { Text("Belum Lunas ($countUnpaid)", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = debtFilterStatus == 2,
                                    onClick = { debtFilterStatus = 2 },
                                    label = { Text("Lunas ($countPaid)", fontSize = 10.sp) }
                                )
                            }

                            if (creditorLedgers.size > 2) {
                                OutlinedTextField(
                                    value = debtSearchQuery,
                                    onValueChange = { debtSearchQuery = it },
                                    placeholder = { Text("Cari nama pemberi pinjaman...", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    trailingIcon = {
                                        if (debtSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { debtSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    if (filteredCreditorLedgers.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (debtSearchQuery.isNotBlank()) "Tidak ditemukan pemberi pinjaman '$debtSearchQuery'" else "Belum ada catatan hutang / pinjaman.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(onClick = {
                                        prefilledCreditorName = ""
                                        showAddPayableDialog = true
                                    }) {
                                        Text("+ Catat Pinjaman / Utang Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredCreditorLedgers, key = { it.creditorName }) { ledger ->
                            CreditorLedgerCard(
                                ledger = ledger,
                                onPay = {
                                    creditorToPay = ledger
                                },
                                onAddLoan = {
                                    prefilledCreditorName = ledger.creditorName
                                    showAddPayableDialog = true
                                },
                                onDeleteLoan = { loanId ->
                                    onDeletePayable(loanId)
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // STICKY BOTTOM ACTION BAR FOR MULTIPLE RECEIVABLE SELECTION
        if (selectedReceivableIds.isNotEmpty()) {
            val sumRec = unpaidReceivables.filter { selectedReceivableIds.contains(it.id) }.sumOf { it.remainingAmount }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${selectedReceivableIds.size} Piutang Terpilih", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Total: ${formatRupiah(sumRec)}", fontWeight = FontWeight.ExtraBold, color = ColorProfit, fontSize = 15.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { selectedReceivableIds.clear() }, modifier = Modifier.height(40.dp)) {
                            Text("Batal", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showBatchReceiveModal = true },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Terima Terpilih", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable / Collapsible Creditor Ledger Card
 * Fulfills all user requirements 1 to 10:
 * - Grouped by creditor
 * - Distinct individual loans
 * - Subtotal pinjaman
 * - Distinct individual repayments
 * - Total pembayaran
 * - Formula: Total Pinjaman - Total Pembayaran = Sisa Hutang
 * - Status BELUM LUNAS / LUNAS
 * - Tanggal pelunasan terakhir (from transaction that made balance Rp0)
 * - Retains full history even when LUNAS
 */
@Composable
fun CreditorLedgerCard(
    ledger: CreditorLedger,
    onPay: () -> Unit,
    onAddLoan: () -> Unit,
    onDeleteLoan: (Long) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ledger.isSettled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (ledger.isSettled) ColorProfit.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // TOP HEADER: NAMA PEMBERI & STATUS BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ledger.creditorName.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (ledger.isSettled) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorProfit.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ColorProfit, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("LUNAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProfit)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorExpense.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "BELUM LUNAS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorExpense,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // KELOMPOK / JENIS BADGES
            if (ledger.classes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ledger.classes.forEach { cls ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = cls,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // RINGKASAN METRIK (KOMPAK 3-KOLOM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Pinjaman", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(formatRupiah(ledger.totalLoanAmount), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text("Total Dibayar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(formatRupiah(ledger.totalRepaymentAmount), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorProfit)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Sisa Hutang", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(
                        formatRupiah(ledger.remainingBalance),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (ledger.isSettled) ColorProfit else ColorExpense
                    )
                }
            }

            // TANGGAL PELUNASAN TERAKHIR (JIKA SUDAH LUNAS)
            if (ledger.isSettled && !ledger.lastSettlementDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ColorProfit.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ColorProfit, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pelunasan Terakhir: ${ledger.lastSettlementDate}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorProfit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // ACTION BAR PADA KARTU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!ledger.isSettled) {
                        Button(
                            onClick = onPay,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = "Bayar", modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Bayar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onAddLoan,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Pinjaman Baru", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Pinjaman", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = {
                            PrintHelper.printHtml(
                                context = context,
                                jobName = "Ledger-${ledger.creditorName}",
                                htmlContent = PrintHelper.generateCreditorLedgerHtml(ledger)
                            )
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (expanded) "Tutup" else "Detail", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // EXPANDED DETAIL VIEW
            AnimatedVisibility(visible = expanded) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // 1. DETAIL PINJAMAN / HUTANG
                        Text(
                            text = "PINJAMAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ledger.loans.forEach { loan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${loan.date} • ${loan.description}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${loan.type} | Jatuh Tempo: ${loan.dueDate} | Sisa: ${formatRupiah(loan.remainingAmount)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatRupiah(loan.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    IconButton(
                                        onClick = { onDeleteLoan(loan.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus Pinjaman",
                                            tint = ColorExpense,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Pinjaman", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                formatRupiah(ledger.totalLoanAmount),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. DETAIL PELUNASAN
                        Text(
                            text = "RIWAYAT PELUNASAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorProfit
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (ledger.repayments.isEmpty()) {
                            Text(
                                text = "Belum ada riwayat pembayaran / pelunasan.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        } else {
                            ledger.repayments.forEach { rep ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${rep.date} • ${rep.description}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${rep.accountName} • ${rep.paymentMethod}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatRupiah(rep.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = ColorProfit
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                formatRupiah(ledger.totalRepaymentAmount),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = ColorProfit
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. SISA HUTANG & FORMULA AGREGASI
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "FORMULA PERHITUNGAN SISA HUTANG:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Pinjaman", fontSize = 11.sp)
                                    Text(formatRupiah(ledger.totalLoanAmount), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("- Total Pembayaran", fontSize = 11.sp, color = ColorProfit)
                                    Text("- ${formatRupiah(ledger.totalRepaymentAmount)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ColorProfit)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sisa Hutang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        formatRupiah(ledger.remainingBalance),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (ledger.isSettled) ColorProfit else ColorExpense
                                    )
                                }
                                if (ledger.isSettled) {
                                    Text(
                                        text = "Status: LUNAS • Tanggal Pelunasan Terakhir: ${ledger.lastSettlementDate ?: "-"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = ColorProfit,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Status: BELUM LUNAS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = ColorExpense,
                                        modifier = Modifier.padding(top = 4.dp)
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

/**
 * Modal untuk membayar / melunasi hutang kepada kreditor tertentu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayCreditorModal(
    creditor: CreditorLedger,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, sourceAccountId: Long, date: String, method: String, notes: String) -> Unit
) {
    var amountStr by remember {
        mutableStateOf(CurrencyFormatter.formatInput(creditor.remainingBalance.toLong().toString()))
    }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var expandedAccount by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var method by remember { mutableStateOf("TRANSFER") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pelunasan / Bayar Pinjaman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "KREDITUR: ${creditor.creditorName.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Hutang:", fontSize = 11.sp)
                            Text(
                                formatRupiah(creditor.remainingBalance),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = ColorExpense
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = CurrencyFormatter.formatInput(it)
                        errorMessage = null
                    },
                    label = { Text("Nominal Pembayaran (Rp) *") },
                    placeholder = { Text("Contoh: 3.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = {
                            amountStr = CurrencyFormatter.formatInput(creditor.remainingBalance.toLong().toString())
                        }) {
                            Text("Lunas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rekening Sumber Kas/Bank
                val currentAccount = accounts.find { it.id == selectedAccountId }
                val currentAccountName = currentAccount?.let { "${it.name} (${formatRupiah(it.currentBalance)})" } ?: "Pilih Rekening"
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it }
                ) {
                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dibayar dari Rekening Kas/Bank *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Saldo: ${formatRupiah(acc.currentBalance)} (${acc.type})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedAccountId = acc.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Tanggal Pembayaran (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Metode Pembayaran Chips
                Text("Metode Pembayaran:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TRANSFER" to "Transfer", "CASH" to "Tunai", "GIRO" to "Giro").forEach { (key, label) ->
                        FilterChip(
                            selected = method == key,
                            onClick = { method = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Cicilan pinjaman termin 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = CurrencyFormatter.parseInput(amountStr)
                            if (amt <= 0) {
                                errorMessage = "Nominal pembayaran harus lebih dari 0"
                                return@Button
                            }
                            if (amt > creditor.remainingBalance) {
                                errorMessage = "Nominal melebihi sisa hutang (${formatRupiah(creditor.remainingBalance)})"
                                return@Button
                            }
                            onConfirm(amt, selectedAccountId, date.trim(), method, notes.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Simpan Pembayaran", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Modal Tambah Utang / Pinjaman Baru
 * Support prefilling or selecting existing creditor to avoid duplicates/typos
 * Distinguishes "PINJAMAN_DANA" (generates Cash In) vs "SUPPLIER" (no Cash In)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayableModal(
    accounts: List<AccountEntity>,
    initialCreditorName: String = "",
    existingCreditorNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, desc: String, amt: Double, due: String, destinationAccountId: Long?, transactionDate: String) -> Unit
) {
    var creditorName by remember { mutableStateOf(initialCreditorName) }
    var expandedCreditorSuggestions by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("PINJAMAN_DANA") } // "PINJAMAN_DANA" or "SUPPLIER"
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var dueDate by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var expandedAccount by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Catat Hutang / Pinjaman Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // TYPE SELECTOR: PINJAMAN DANA vs SUPPLIER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "PINJAMAN_DANA",
                        onClick = { type = "PINJAMAN_DANA" },
                        label = { Text("Pinjaman Dana (Cash In)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = type == "SUPPLIER",
                        onClick = { type = "SUPPLIER" },
                        label = { Text("Utang Usaha & Supplier", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // EXPLANATION BANNER
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (type == "PINJAMAN_DANA") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (type == "PINJAMAN_DANA") {
                            "Penerimaan dana pinjaman akan otomatis menambah saldo Kas/Bank & tercatat di Cash Flow (Aktivitas Pendanaan)."
                        } else {
                            "Pencatatan tagihan pembelian/utang supplier tanpa penerimaan uang kas masuk."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (type == "PINJAMAN_DANA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Creditor Name with Suggestions
                ExposedDropdownMenuBox(
                    expanded = expandedCreditorSuggestions && existingCreditorNames.isNotEmpty(),
                    onExpandedChange = { expandedCreditorSuggestions = it }
                ) {
                    OutlinedTextField(
                        value = creditorName,
                        onValueChange = {
                            creditorName = it
                            expandedCreditorSuggestions = it.isNotBlank()
                        },
                        label = { Text("Pemberi Pinjaman / Supplier *") },
                        placeholder = { Text("Contoh: Pak Ginanjar / PT Semen") },
                        trailingIcon = {
                            if (existingCreditorNames.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCreditorSuggestions)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        singleLine = true
                    )
                    if (existingCreditorNames.isNotEmpty()) {
                        val filteredSuggestions = existingCreditorNames.filter {
                            creditorName.isBlank() || it.contains(creditorName, ignoreCase = true)
                        }
                        if (filteredSuggestions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expandedCreditorSuggestions,
                                onDismissRequest = { expandedCreditorSuggestions = false }
                            ) {
                                filteredSuggestions.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            creditorName = name
                                            expandedCreditorSuggestions = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = CurrencyFormatter.formatInput(it) },
                    label = { Text("Nominal Pinjaman / Utang (Rp) *") },
                    placeholder = { Text("Contoh: 15.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (type == "PINJAMAN_DANA") {
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                    ExposedDropdownMenuBox(
                        expanded = expandedAccount,
                        onExpandedChange = { expandedAccount = it }
                    ) {
                        OutlinedTextField(
                            value = currentAccountName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Masuk ke Rekening Kas / Bank *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAccount,
                            onDismissRequest = { expandedAccount = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type})") },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expandedAccount = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = transactionDate,
                        onValueChange = { transactionDate = CurrencyFormatter.formatDateInput(it) },
                        label = { Text("Tgl Pinjaman") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = CurrencyFormatter.formatDateInput(it) },
                        label = { Text("Jatuh Tempo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Pinjaman / Utang") },
                    placeholder = { Text("Contoh: Pinjaman modal bore pile") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val amt = CurrencyFormatter.parseInput(amountStr)
                        if (creditorName.isBlank()) {
                            errorMessage = "Nama pemberi pinjaman/supplier tidak boleh kosong"
                            return@Button
                        }
                        if (amt <= 0) {
                            errorMessage = "Nominal harus lebih dari 0"
                            return@Button
                        }
                        val destAccId = if (type == "PINJAMAN_DANA") selectedAccountId else null
                        onSave(
                            creditorName.trim(),
                            type,
                            description.trim(),
                            amt,
                            dueDate.trim(),
                            destAccId,
                            transactionDate.trim()
                        )
                    }) {
                        Text("Simpan Pinjaman")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceivableModal(
    projectSummaries: List<ProjectFinancialSummary>,
    onDismiss: () -> Unit,
    onSave: (client: String, projName: String, invNum: String, desc: String, amt: Double, due: String, projectId: Long?) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf(projectSummaries.firstOrNull()?.project?.name ?: "") }
    var selectedProjectId by remember { mutableStateOf(projectSummaries.firstOrNull()?.project?.id) }
    var invoiceNumber by remember { mutableStateOf("INV-001") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Catat Piutang Klien Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nama Klien / Sumber Piutang *") },
                    placeholder = { Text("Contoh: PT Bangun Persada") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = CurrencyFormatter.formatInput(it) },
                    label = { Text("Nominal Piutang (Rp) *") },
                    placeholder = { Text("Contoh: 25.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Nomor Invoice / Bukti Piutang") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Piutang") },
                    placeholder = { Text("Contoh: Termin 2 bore pile 10 titik") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Jatuh Tempo (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val amt = CurrencyFormatter.parseInput(amountStr)
                        if (clientName.isBlank()) {
                            errorMessage = "Nama klien tidak boleh kosong"
                            return@Button
                        }
                        if (amt <= 0) {
                            errorMessage = "Nominal harus lebih dari 0"
                            return@Button
                        }
                        onSave(clientName.trim(), projectName.trim(), invoiceNumber.trim(), description.trim(), amt, dueDate.trim(), selectedProjectId)
                    }) {
                        Text("Simpan Piutang")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPaymentModal(
    projectSummary: ProjectFinancialSummary,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (projectId: Long, amount: Double, accountId: Long, date: String, method: String, notes: String) -> Unit
) {
    var amountStr by remember {
        mutableStateOf(CurrencyFormatter.formatInput(projectSummary.remainingContractBalance.toLong().toString()))
    }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var date by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var method by remember { mutableStateOf("TRANSFER") }
    var notes by remember { mutableStateOf("") }
    var expandedAccount by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Penerimaan Termin Proyek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(projectSummary.project.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sisa Piutang Kontrak:", fontSize = 12.sp)
                        Text(formatRupiah(projectSummary.remainingContractBalance), fontWeight = FontWeight.Bold, color = ColorProfit, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = CurrencyFormatter.formatInput(it) },
                    label = { Text("Nominal Diterima (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val currentAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it }
                ) {
                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Masuk ke Rekening Kas / Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${formatRupiah(acc.currentBalance)})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Tanggal Penerimaan (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan Termin") },
                    placeholder = { Text("Contoh: Termin 1 (DP 30%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val amt = CurrencyFormatter.parseInput(amountStr)
                        if (amt > 0) {
                            onSave(projectSummary.project.id, amt, selectedAccountId, date.trim(), method, notes.trim())
                        }
                    }) {
                        Text("Simpan Penerimaan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSettlementModal(
    title: String,
    subtitle: String,
    totalAmount: Double,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, date: String, method: String, notes: String) -> Unit
) {
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var expandedAccount by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var method by remember { mutableStateOf("TRANSFER") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Akumulasi:", fontSize = 12.sp)
                        Text(formatRupiah(totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val currentAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it }
                ) {
                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rekening Kas / Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${formatRupiah(acc.currentBalance)})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Tanggal Transaksi (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan Pelunasan") },
                    placeholder = { Text("Contoh: Pelunasan sekaligus via transfer bank") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(selectedAccountId, date.trim(), method, notes.trim())
                    }) {
                        Text("Konfirmasi & Lunasi")
                    }
                }
            }
        }
    }
}
