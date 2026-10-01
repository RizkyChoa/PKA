package com.example.ui.screens.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.ReceivableEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayablesReceivablesTab(
    projectSummaries: List<ProjectFinancialSummary>,
    receivables: List<ReceivableEntity>,
    payables: List<PayableEntity>,
    accounts: List<AccountEntity>,
    onPayPayablesBatch: (payableIds: List<Long>, sourceAccountId: Long, date: String, method: String, notes: String) -> Unit,
    onPayReceivablesBatch: (receivableIds: List<Long>, destinationAccountId: Long, date: String, method: String, notes: String) -> Unit,
    onCreatePayable: (creditorName: String, type: String, description: String, totalAmount: Double, dueDate: String, destinationAccountId: Long) -> Unit,
    onDeletePayable: (id: Long) -> Unit,
    onCreateReceivable: (clientName: String, projectName: String, invoiceNumber: String, description: String, totalAmount: Double, dueDate: String, projectId: Long?) -> Unit,
    onDeleteReceivable: (id: Long) -> Unit,
    onReceiveProjectPayment: (projectId: Long, amount: Double, destinationAccountId: Long, date: String, method: String, notes: String) -> Unit
) {
    var subTabFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Piutang Saja, 2: Utang Saja

    // Multi-Selection State for Payables
    val selectedPayableIds = remember { mutableStateListOf<Long>() }

    // Multi-Selection State for Receivables
    val selectedReceivableIds = remember { mutableStateListOf<Long>() }

    // Dialog States
    var showAddPayableDialog by remember { mutableStateOf(false) }
    var showAddReceivableDialog by remember { mutableStateOf(false) }
    var showBatchPayModal by remember { mutableStateOf(false) }
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

    val unpaidPayables = remember(payables) {
        payables.filter { it.remainingAmount > 0 }
    }

    val totalAllPayables = remember(unpaidPayables) {
        unpaidPayables.sumOf { it.remainingAmount }
    }

    // Modals
    if (showAddPayableDialog) {
        AddPayableModal(
            accounts = accounts,
            onDismiss = { showAddPayableDialog = false },
            onSave = { name, type, desc, amt, due, accId ->
                onCreatePayable(name, type, desc, amt, due, accId)
                showAddPayableDialog = false
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

    if (showBatchPayModal) {
        val selectedPayablesList = unpaidPayables.filter { selectedPayableIds.contains(it.id) }
        val sumToPay = selectedPayablesList.sumOf { it.remainingAmount }

        BatchSettlementModal(
            title = "Lunasi ${selectedPayablesList.size} Utang Terpilih",
            subtitle = "Pilih rekening pembayaran untuk melunasi:",
            totalAmount = sumToPay,
            accounts = accounts,
            onDismiss = { showBatchPayModal = false },
            onConfirm = { accountId, date, method, notes ->
                onPayPayablesBatch(selectedPayableIds.toList(), accountId, date, method, notes)
                selectedPayableIds.clear()
                showBatchPayModal = false
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
            // FILTER CHIPS & RINGKASAN
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
                    label = { Text("Utang (${formatRupiah(totalAllPayables)})", fontSize = 11.sp) }
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("PIUTANG SISA KONTRAK PROYEK", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("${projectsWithReceivable.size} Proyek belum lunas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(formatRupiah(totalProjectReceivables), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = ColorProfit)
                            }
                        }
                    }

                    if (projectsWithReceivable.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Tidak ada sisa piutang proyek. Semua kontrak proyek aktif telah lunas tercairkan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ps.project.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Sumber/Klien: ${ps.project.clientName} (${ps.project.projectCode})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Lokasi: ${ps.project.location.ifBlank { "-" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = ps.project.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Nilai Total Kontrak:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatRupiah(ps.contractAmount), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }
                                        Column {
                                            Text("Sudah Diterima:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatRupiah(ps.cashReceived), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Sisa Piutang Belum Cair:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatRupiah(ps.remainingContractBalance), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ColorProfit)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action: Cairkan Termin / Lunasi
                                    Button(
                                        onClick = { projectToReceivePayment = ps },
                                        modifier = Modifier.fillMaxWidth().height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Catat Penerimaan Termin / Pelunasan Proyek", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                // 3. UTANG USAHA, SUPPLIER & PINJAMAN MODAL
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
                                Text("UTANG USAHA & SUPPLIER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Text("Total: ${formatRupiah(totalAllPayables)}", style = MaterialTheme.typography.labelSmall, color = ColorExpense, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showAddPayableDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Utang", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (unpaidPayables.isEmpty()) {
                        item {
                            Text("Tidak ada utang usaha/supplier yang belum lunas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(unpaidPayables) { pay ->
                            val isChecked = selectedPayableIds.contains(pay.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedPayableIds.remove(pay.id) else selectedPayableIds.add(pay.id)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChecked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedPayableIds.add(pay.id) else selectedPayableIds.remove(pay.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Sumber/Kreditur: ${pay.creditorName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ColorExpense.copy(alpha = 0.15f)
                                            ) {
                                                Text(pay.type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorExpense, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text(pay.description, style = MaterialTheme.typography.bodySmall)
                                        Text("Jatuh Tempo: ${pay.dueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total: ${formatRupiah(pay.totalAmount)}", style = MaterialTheme.typography.labelSmall)
                                            Text("Sisa Utang: ${formatRupiah(pay.remainingAmount)}", fontWeight = FontWeight.Bold, color = ColorExpense, fontSize = 13.sp)
                                        }
                                    }
                                    IconButton(onClick = { onDeletePayable(pay.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // STICKY BOTTOM ACTION BAR FOR MULTIPLE SELECTION
        if (selectedPayableIds.isNotEmpty()) {
            val sumPay = unpaidPayables.filter { selectedPayableIds.contains(it.id) }.sumOf { it.remainingAmount }
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
                        Text("${selectedPayableIds.size} Utang Terpilih", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Total: ${formatRupiah(sumPay)}", fontWeight = FontWeight.ExtraBold, color = ColorExpense, fontSize = 15.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { selectedPayableIds.clear() }, modifier = Modifier.height(40.dp)) {
                            Text("Batal", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showBatchPayModal = true },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Lunasi Terpilih", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else if (selectedReceivableIds.isNotEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayableModal(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, desc: String, amt: Double, due: String, destinationAccountId: Long) -> Unit
) {
    var creditorName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SUPPLIER") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var expandedAccount by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Catat Utang Usaha / Pinjaman", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Otomatis dicatat sebagai Uang Masuk ke kas/bank & langsung menambah saldo Arus Kas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = creditorName,
                    onValueChange = { creditorName = it },
                    label = { Text("Nama Kreditur / Sumber Utang *") },
                    placeholder = { Text("Contoh: PT Semen Ready Mix / Bank") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = CurrencyFormatter.formatInput(it) },
                    label = { Text("Nominal Utang / Pinjaman (Rp) *") },
                    placeholder = { Text("Contoh: 15.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rekening Kas Penampung
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
                                text = { Text("${acc.name} (${acc.type})") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Utang") },
                    placeholder = { Text("Contoh: Pinjaman modal operasional bore pile") },
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
                        if (creditorName.isBlank()) {
                            errorMessage = "Nama kreditur tidak boleh kosong"
                            return@Button
                        }
                        if (amt <= 0) {
                            errorMessage = "Nominal harus lebih dari 0"
                            return@Button
                        }
                        onSave(creditorName.trim(), type, description.trim(), amt, dueDate.trim(), selectedAccountId)
                    }) {
                        Text("Simpan Utang")
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
    var notes by remember { mutableStateOf("Pembayaran Termin/Pelunasan ${projectSummary.project.name}") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Penerimaan Termin / Pelunasan Proyek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${projectSummary.project.name} (${projectSummary.project.clientName})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Sisa Piutang: ${formatRupiah(projectSummary.remainingContractBalance)}", fontWeight = FontWeight.Bold, color = ColorProfit)
                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = CurrencyFormatter.formatInput(it) },
                    label = { Text("Nominal Pembayaran Diterima (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rekening Penampung
                var expAcc by remember { mutableStateOf(false) }
                val currentAccName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                ExposedDropdownMenuBox(expanded = expAcc, onExpandedChange = { expAcc = it }) {
                    OutlinedTextField(
                        value = currentAccName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rekening Kas/Bank Masuk") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expAcc) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expAcc, onDismissRequest = { expAcc = false }) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.type})") },
                                onClick = { selectedAccountId = acc.id; expAcc = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Tanggal Penerimaan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan Pembayaran") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val amt = CurrencyFormatter.parseInput(amountStr)
                        if (amt <= 0) {
                            errorMessage = "Nominal harus lebih dari 0"
                            return@Button
                        }
                        onSave(projectSummary.project.id, amt, selectedAccountId, date, method, notes)
                    }) {
                        Text("Terima Dana")
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
    var date by remember { mutableStateOf(CurrencyFormatter.todayString()) }
    var method by remember { mutableStateOf("TRANSFER") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Transaksi: ${formatRupiah(totalAmount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                var expAcc by remember { mutableStateOf(false) }
                val currentAccName = accounts.find { it.id == selectedAccountId }?.name ?: "Pilih Rekening"
                ExposedDropdownMenuBox(expanded = expAcc, onExpandedChange = { expAcc = it }) {
                    OutlinedTextField(
                        value = currentAccName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rekening Kas / Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expAcc) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expAcc, onDismissRequest = { expAcc = false }) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.type}) - Saldo: ${formatRupiah(acc.currentBalance)}") },
                                onClick = { selectedAccountId = acc.id; expAcc = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = CurrencyFormatter.formatDateInput(it) },
                    label = { Text("Tanggal Transaksi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan Pelunasan") },
                    placeholder = { Text("Pelunasan batch utang/piutang") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(selectedAccountId, date, method, notes)
                    }) {
                        Text("Konfirmasi & Simpan")
                    }
                }
            }
        }
    }
}
