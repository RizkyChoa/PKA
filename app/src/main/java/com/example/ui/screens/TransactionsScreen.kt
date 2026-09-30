package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.ProjectEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.formatRupiah
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.ui.theme.ColorTransfer
import com.example.util.PrintHelper

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    allProjects: List<ProjectEntity>,
    onOpenAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onRequestVoidTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, MONEY_IN, MONEY_OUT, TRANSFER
    var selectedProjectFilter by remember { mutableStateOf<Long?>(null) } // null = Semua
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedTypeFilter,
        selectedProjectFilter
    ) {
        transactions.filter { trx ->
            val matchSearch = searchQuery.isBlank() ||
                    trx.description.contains(searchQuery, ignoreCase = true) ||
                    trx.trxNumber.contains(searchQuery, ignoreCase = true) ||
                    (trx.projectName?.contains(searchQuery, ignoreCase = true) == true)

            val matchType = when (selectedTypeFilter) {
                "MONEY_IN" -> trx.type == "MONEY_IN"
                "MONEY_OUT" -> trx.type == "MONEY_OUT"
                "TRANSFER" -> trx.type == "TRANSFER"
                else -> true
            }

            val matchProject = selectedProjectFilter == null || trx.projectId == selectedProjectFilter

            matchSearch && matchType && matchProject
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search & Add Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nota, deskripsi, proyek...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("trx_search_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    PrintHelper.printHtml(
                        context = context,
                        jobName = "LaporanTransaksi-BorePile",
                        htmlContent = PrintHelper.generateTransactionsHtml(filteredTransactions)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("trx_print_button")
            ) {
                Icon(Icons.Default.Print, contentDescription = "Cetak PDF", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Print", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenAddTransaction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("trx_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Input")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == "ALL",
                    onClick = { selectedTypeFilter = "ALL" },
                    label = { Text("Semua (${transactions.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedTypeFilter == "MONEY_IN",
                    onClick = { selectedTypeFilter = "MONEY_IN" },
                    label = { Text("Uang Masuk") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorProfit.copy(alpha = 0.2f),
                        selectedLabelColor = ColorProfit
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedTypeFilter == "MONEY_OUT",
                    onClick = { selectedTypeFilter = "MONEY_OUT" },
                    label = { Text("Uang Keluar") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorExpense.copy(alpha = 0.2f),
                        selectedLabelColor = ColorExpense
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedTypeFilter == "TRANSFER",
                    onClick = { selectedTypeFilter = "TRANSFER" },
                    label = { Text("Mutasi Kas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorTransfer.copy(alpha = 0.2f),
                        selectedLabelColor = ColorTransfer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Project Filter Chips (Optional filtering by project)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedProjectFilter == null,
                    onClick = { selectedProjectFilter = null },
                    label = { Text("Semua Proyek") }
                )
            }
            items(allProjects) { proj ->
                FilterChip(
                    selected = selectedProjectFilter == proj.id,
                    onClick = { selectedProjectFilter = proj.id },
                    label = { Text(proj.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada transaksi yang cocok dengan filter.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { trx ->
                    TransactionItemCard(
                        transaction = trx,
                        onEditClick = { onEditTransaction(trx) },
                        onDeleteClick = { transactionToDelete = trx },
                        onRequestVoid = { onRequestVoidTransaction(trx) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Transaction Dialog
    if (transactionToDelete != null) {
        val t = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Transaksi?") },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus transaksi '${t.trxNumber}'?")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deskripsi: ${t.description}\nNominal: ${formatRupiah(t.amount)}\nSaldo rekening kas dan laporan akan otomatis disesuaikan kembali.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(t.id)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRequestVoid: () -> Unit
) {
    val isVoid = transaction.status == "VOID"
    val isIn = transaction.type == "MONEY_IN"
    val isTransfer = transaction.type == "TRANSFER"

    val iconColor = when {
        isVoid -> Color.Gray
        isTransfer -> ColorTransfer
        isIn -> ColorProfit
        else -> ColorExpense
    }

    val icon = when {
        isTransfer -> Icons.Default.SwapHoriz
        isIn -> Icons.Default.ArrowUpward
        else -> Icons.Default.ArrowDownward
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trx_card_${transaction.trxNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isVoid) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: ID, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = transaction.trxNumber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isVoid) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = transaction.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    val prefix = if (isTransfer) "" else if (isIn) "+" else "-"
                    Text(
                        text = "$prefix${formatRupiah(transaction.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = iconColor
                    )
                    if (isVoid) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "VOID (DIBATALKAN)",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = transaction.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = transaction.description,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )

            if (transaction.voidReason.isNotBlank() && isVoid) {
                Text(
                    text = "Alasan batal: ${transaction.voidReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meta tags (Proyek, Rekening, Kategori)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (transaction.projectName != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "Proyek: ${transaction.projectName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    val accountLabel = if (isTransfer && transaction.destinationAccountName != null) {
                        "${transaction.sourceAccountName} -> ${transaction.destinationAccountName}"
                    } else {
                        transaction.sourceAccountName
                    }
                    Text(
                        text = "Akun: $accountLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Transaksi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Transaksi",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Void Action Button (if not already voided)
                    if (!isVoid) {
                        IconButton(
                            onClick = onRequestVoid,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = "Batalkan Transaksi (VOID)",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
