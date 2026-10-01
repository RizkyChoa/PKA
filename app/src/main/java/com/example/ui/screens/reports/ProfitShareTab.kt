package com.example.ui.screens.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
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
import com.example.data.model.ProfitPartnerEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.TransactionEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.CurrencyFormatter
import com.example.util.PrintHelper

@Composable
fun ProfitShareTab(
    projectSummaries: List<ProjectFinancialSummary>,
    profitPartners: List<ProfitPartnerEntity>,
    allTransactions: List<TransactionEntity>,
    onCreatePartner: (name: String, percentage: Double, phone: String, notes: String) -> Unit,
    onUpdatePartner: (id: Long, name: String, percentage: Double, phone: String, notes: String) -> Unit,
    onDeletePartner: (id: Long) -> Unit
) {
    val context = LocalContext.current
    var showPartnerDialog by remember { mutableStateOf(false) }
    var partnerToEdit by remember { mutableStateOf<ProfitPartnerEntity?>(null) }

    // Total Laba Kotor dari Seluruh Proyek
    val totalGrossProfit = remember(projectSummaries) {
        projectSummaries.sumOf { it.grossProfit }
    }

    val totalPercentage = remember(profitPartners) {
        profitPartners.sumOf { it.sharePercentage }
    }

    val validProfitWithdrawals = remember(allTransactions) {
        allTransactions.filter {
            it.status == "VALID" && it.classification == "PENGAMBILAN_PROFIT"
        }
    }

    val totalWithdrawn = remember(validProfitWithdrawals) {
        validProfitWithdrawals.sumOf { it.amount }
    }

    val netRemainingToDistribute = totalGrossProfit - totalWithdrawn

    if (showPartnerDialog) {
        PartnerFormDialog(
            partnerToEdit = partnerToEdit,
            onDismiss = {
                showPartnerDialog = false
                partnerToEdit = null
            },
            onSave = { name, pct, phone, notes ->
                if (partnerToEdit != null) {
                    onUpdatePartner(partnerToEdit!!.id, name, pct, phone, notes)
                } else {
                    onCreatePartner(name, pct, phone, notes)
                }
                showPartnerDialog = false
                partnerToEdit = null
            }
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. HERO BANNER: TOTAL LABA KOTOR & AKSI CETAK
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TOTAL LABA KOTOR PROYEK",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupiah(totalGrossProfit),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = if (totalGrossProfit >= 0) ColorProfit else ColorExpense
                            )
                        }

                        IconButton(
                            onClick = {
                                val html = PrintHelper.generateProfitShareHtml(
                                    projectSummaries = projectSummaries,
                                    partners = profitPartners,
                                    transactions = allTransactions
                                )
                                PrintHelper.printHtml(context, "Laporan_Pembagian_Profit", html)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Pengambilan Profit:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-${formatRupiah(totalWithdrawn)}", fontWeight = FontWeight.Bold, color = ColorExpense)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sisa Profit Siap Bagi:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupiah(netRemainingToDistribute), fontWeight = FontWeight.Bold, color = ColorProfit)
                        }
                    }
                }
            }
        }

        // 2. RINCIAN LABA KOTOR PER PROYEK
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SUMBER LABA KOTOR PER PROYEK",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (projectSummaries.isEmpty()) {
                        Text("Belum ada proyek tercatat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        projectSummaries.forEach { ps ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ps.project.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${ps.project.clientName} • Kontrak: ${formatRupiah(ps.contractAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = formatRupiah(ps.grossProfit),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (ps.grossProfit >= 0) ColorProfit else ColorExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. DAFTAR & PERSENTASE PENERIMA PROFIT (Bisa Tambah, Edit, Hapus)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PENERIMA PROFIT & PERSENTASE",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total Alokasi: ${totalPercentage.toInt()}%" + if (totalPercentage != 100.0) " (Belum 100%)" else " (Pas 100%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (totalPercentage == 100.0) ColorProfit else ColorExpense,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                partnerToEdit = null
                                showPartnerDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Penerima", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (profitPartners.isEmpty()) {
                        Text(
                            text = "Belum ada mitra/partner penerima profit. Ketuk tombol 'Tambah Penerima' di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        profitPartners.forEach { partner ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(partner.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (partner.notes.isNotBlank()) {
                                                Text(partner.notes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "${partner.sharePercentage.toInt()}%",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                partnerToEdit = partner
                                                showPartnerDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { onDeletePartner(partner.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ColorExpense, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. HASIL PEMBAGIAN PROFIT & POTONGAN PER PARTNER
        item {
            Text(
                text = "HASIL PEMBAGIAN PROFIT & POTONGAN",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (profitPartners.isEmpty()) {
            item {
                Text("Tambahkan mitra di atas untuk melihat rincian pembagian.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(profitPartners) { partner ->
                val partnerGrossShare = totalGrossProfit * (partner.sharePercentage / 100.0)

                // Transaksi penarikan/pengambilan profit oleh partner ini
                val partnerWithdrawals = validProfitWithdrawals.filter {
                    it.profitPartnerId == partner.id || it.profitPartnerName.equals(partner.name, ignoreCase = true)
                }
                val totalPartnerWithdrawal = partnerWithdrawals.sumOf { it.amount }
                val netPartnerProfit = partnerGrossShare - totalPartnerWithdrawal

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = partner.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Porsi Pembagian: ${partner.sharePercentage.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Sisa Bersih Diterima",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatRupiah(netPartnerProfit),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = if (netPartnerProfit >= 0) ColorProfit else ColorExpense
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Rincian per Proyek
                        Text("Rincian Laba Kotor per Proyek:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))

                        projectSummaries.forEach { ps ->
                            val prjGross = ps.grossProfit
                            val prjShare = prjGross * (partner.sharePercentage / 100.0)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${ps.project.name} (${formatRupiah(prjGross)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatRupiah(prjShare),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtotal Jatah Kotor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Jatah Kotor:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(formatRupiah(partnerGrossShare), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        // Potongan Pengambilan Profit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Potongan Pengambilan Profit (${partnerWithdrawals.size}x):",
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorExpense
                            )
                            Text(
                                "-${formatRupiah(totalPartnerWithdrawal)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorExpense
                            )
                        }

                        if (partnerWithdrawals.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            partnerWithdrawals.forEach { w ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${w.date}: ${w.description}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatRupiah(w.amount),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ColorExpense
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (netPartnerProfit >= 0) ColorProfit.copy(alpha = 0.12f) else ColorExpense.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (netPartnerProfit >= 0) "TOTAL PROFIT BERSIH DIBAWAPULANG:" else "STATUS: LEBIH AMBIL (MINUS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (netPartnerProfit >= 0) ColorProfit else ColorExpense
                                )
                                Text(
                                    text = formatRupiah(netPartnerProfit),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (netPartnerProfit >= 0) ColorProfit else ColorExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PartnerFormDialog(
    partnerToEdit: ProfitPartnerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, percentage: Double, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(partnerToEdit?.name ?: "") }
    var percentageStr by remember {
        mutableStateOf(
            partnerToEdit?.sharePercentage?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var phone by remember { mutableStateOf(partnerToEdit?.phone ?: "") }
    var notes by remember { mutableStateOf(partnerToEdit?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (partnerToEdit != null) "Edit Penerima Profit" else "Tambah Penerima Profit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Mitra / Penerima *") },
                    placeholder = { Text("Contoh: Ramlan / Gunawan / Rizky") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = percentageStr,
                    onValueChange = { percentageStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Persentase Pembagian (%) *") },
                    placeholder = { Text("Contoh: 60 atau 20") },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WhatsApp (Opsional)") },
                    placeholder = { Text("08123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan / Jabatan (Opsional)") },
                    placeholder = { Text("Contoh: Partner Utama / Direktur") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama penerima tidak boleh kosong"
                                return@Button
                            }
                            val pct = percentageStr.toDoubleOrNull() ?: 0.0
                            if (pct <= 0.0 || pct > 100.0) {
                                errorMessage = "Persentase harus antara 1% - 100%"
                                return@Button
                            }
                            onSave(name.trim(), pct, phone.trim(), notes.trim())
                        }
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
