package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BookPeriodEntity
import com.example.data.model.GlobalFinancialSummary
import com.example.data.model.TransactionEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.PrintHelper

@Composable
fun ArchiveExplorerDialog(
    allPeriods: List<BookPeriodEntity>,
    activePeriod: BookPeriodEntity?,
    currentSelectedPeriodId: Long?,
    allTransactions: List<TransactionEntity>,
    globalSummary: GlobalFinancialSummary,
    onDismiss: () -> Unit,
    onSelectPeriod: (Long?) -> Unit,
    onOpenCloseBookModal: () -> Unit,
    onRenamePeriod: (Long, String) -> Unit,
    onUnlockPeriod: (Long) -> Unit,
    onLockPeriod: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for renaming an archive
    var periodToRename by remember { mutableStateOf<BookPeriodEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Dialog state for unlocking an archive
    var periodToUnlock by remember { mutableStateOf<BookPeriodEntity?>(null) }

    // Collapsed/expanded state of year folders
    val expandedYears = remember { mutableStateMapOf<Int, Boolean>() }

    // Group periods by Year
    val groupedByYear = remember(allPeriods, searchQuery) {
        val filtered = allPeriods.filter { p ->
            if (searchQuery.isBlank()) true else {
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.year.toString().contains(searchQuery) ||
                p.startDate.contains(searchQuery) ||
                p.endDate.contains(searchQuery)
            }
        }
        filtered.groupBy { it.year }.toSortedMap(Comparator.reverseOrder())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // TOP HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "📁 ARSIP & PERIODE BUKU",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Struktur Pembukuan & Histori Data PKA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CURRENT ACTIVE PERIOD CARD
                if (activePeriod != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ColorProfit
                                    ) {
                                        Text(
                                            "PERIODE AKTIF",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        activePeriod.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                if (currentSelectedPeriodId != null && currentSelectedPeriodId != activePeriod.id) {
                                    OutlinedButton(
                                        onClick = {
                                            onSelectPeriod(null) // return to active
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Buka Aktif", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rentang: ${activePeriod.startDate} s/d ${activePeriod.endDate} • Saldo Awal: ${formatRupiah(activePeriod.openingCashBalance)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenCloseBookModal()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tutup Buku & Arsipkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SEARCH BAR FOR ARCHIVES
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama arsip / tahun / rentang...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // FOLDER TREE STRUCTURE GROUPED BY YEAR
                Text(
                    "STRUKTUR FOLDER ARSIP:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (groupedByYear.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Tidak ada arsip yang cocok dengan pencarian.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        groupedByYear.forEach { (year, periodsInYear) ->
                            val isExpanded = expandedYears[year] ?: true

                            item(key = "year_$year") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedYears[year] = !isExpanded }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Tahun $year",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "(${periodsInYear.size} Periode)",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (isExpanded) {
                                items(periodsInYear, key = { "period_${it.id}" }) { period ->
                                    val isCurrentSelected = (currentSelectedPeriodId == period.id) ||
                                        (currentSelectedPeriodId == null && period.status == "ACTIVE")
                                    val isArchived = period.isArchived
                                    val isLastInYear = periodsInYear.lastOrNull()?.id == period.id
                                    val treeBranch = if (isLastInYear) "└── " else "├── "

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrentSelected) {
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isCurrentSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
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
                                                        text = treeBranch,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        Icons.Default.Archive,
                                                        contentDescription = null,
                                                        tint = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else ColorProfit,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        period.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isArchived) MaterialTheme.colorScheme.surfaceVariant else ColorProfit.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        if (isArchived) "ARSIP (READ-ONLY)" else "AKTIF",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else ColorProfit,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Rentang: ${period.startDate} s/d ${period.endDate}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (isArchived) {
                                                Text(
                                                    "Saldo Akhir Kas: ${formatRupiah(period.closingCashBalance)} • Hutang: ${formatRupiah(period.closingPayableBalance)}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // ACTIONS ON ARCHIVE ITEM
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    // Open Archive Button
                                                    Button(
                                                        onClick = {
                                                            onSelectPeriod(period.id)
                                                            onDismiss()
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.height(28.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isCurrentSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                                        )
                                                    ) {
                                                        Text(if (isCurrentSelected) "Sedang Dibuka" else "Buka Arsip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    // Rename Archive Button
                                                    OutlinedButton(
                                                        onClick = {
                                                            periodToRename = period
                                                            renameInput = period.name
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.height(28.dp),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama", modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("Ubah Nama", fontSize = 10.sp)
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    // Print / Export PDF button
                                                    IconButton(
                                                        onClick = {
                                                            val periodTrxs = allTransactions.filter { it.date >= period.startDate && it.date <= period.endDate }
                                                            PrintHelper.printHtml(
                                                                context = context,
                                                                jobName = "Arsip-${period.periodCode}",
                                                                htmlContent = PrintHelper.generatePeriodArchiveHtml(period, globalSummary, periodTrxs)
                                                            )
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Print, contentDescription = "Cetak PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                                    }

                                                    // Lock / Unlock button
                                                    if (isArchived) {
                                                        IconButton(
                                                            onClick = { periodToUnlock = period },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.LockOpen, contentDescription = "Buka Kunci", tint = ColorExpense, modifier = Modifier.size(15.dp))
                                                        }
                                                    } else {
                                                        IconButton(
                                                            onClick = { onLockPeriod(period.id) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Lock, contentDescription = "Kunci", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
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
                }
            }
        }
    }

    // MODAL EDIT NAMA ARSIP
    if (periodToRename != null) {
        AlertDialog(
            onDismissRequest = { periodToRename = null },
            title = { Text("Ubah Nama Arsip", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column {
                    Text(
                        "Perubahan nama arsip tidak akan mengubah tanggal, transaksi, atau catatan finansial di dalamnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Nama Arsip") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenamePeriod(periodToRename!!.id, renameInput.trim())
                            periodToRename = null
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { periodToRename = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // MODAL KONFIRMASI BUKA KUNCI ARSIP
    if (periodToUnlock != null) {
        AlertDialog(
            onDismissRequest = { periodToUnlock = null },
            title = { Text("Buka Kunci Arsip?", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Text(
                    "Arsip '${periodToUnlock!!.name}' akan dibuka statusnya sehingga dapat diedit sementara untuk keperluan koreksi data. Pastikan Anda mengunci kembali periode ini setelah selesai.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnlockPeriod(periodToUnlock!!.id)
                        periodToUnlock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Buka Kunci")
                }
            },
            dismissButton = {
                TextButton(onClick = { periodToUnlock = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
