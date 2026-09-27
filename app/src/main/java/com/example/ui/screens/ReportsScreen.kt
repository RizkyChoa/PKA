package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlobalFinancialSummary
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.ReceivableEntity
import com.example.data.model.formatRupiah
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.util.PrintHelper

@Composable
fun ReportsScreen(
    summary: GlobalFinancialSummary,
    projectSummaries: List<ProjectFinancialSummary>,
    receivables: List<ReceivableEntity>,
    payables: List<PayableEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Laba Rugi Perusahaan, 1: Laba Rugi Per Proyek, 2: Piutang & Utang

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("P&L Perusahaan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Laba Rugi Proyek", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Utang / Piutang", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> CompanyPnLTab(summary)
            1 -> ProjectPnLTab(projectSummaries)
            2 -> PayablesReceivablesTab(receivables, payables)
        }
    }
}

@Composable
fun CompanyPnLTab(summary: GlobalFinancialSummary) {
    val isProfit = summary.netProfit >= 0
    val netMargin = if (summary.totalRevenue > 0) (summary.netProfit / summary.totalRevenue) * 100 else 0.0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            // Net Profit Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isProfit) ColorProfit.copy(alpha = 0.12f) else ColorExpense.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProfit) "LABA BERSIH PERUSAHAAN (NET PROFIT)" else "RUGI BERSIH PERUSAHAAN",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isProfit) ColorProfit else ColorExpense
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isProfit) ColorProfit else ColorExpense
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "Margin: %.1f%%", netMargin),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formatRupiah(summary.netProfit),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isProfit) ColorProfit else ColorExpense
                    )
                    Text(
                        text = "Dihitung dari Total Pendapatan Proyek dikurangi Biaya HPP & Operasional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. PENDAPATAN (REVENUE)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PnLRow("Pendapatan Proyek Bore Pile (DP, Termin & Pelunasan)", summary.totalRevenue, ColorProfit)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    PnLRow("TOTAL PENDAPATAN", summary.totalRevenue, ColorProfit, isBold = true)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "2. BIAYA POKOK PROYEK / HPP BORE PILE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PnLRow("Biaya Langsung Lapangan Proyek", summary.totalProjectCost, ColorExpense)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    val grossProfit = summary.totalRevenue - summary.totalProjectCost
                    PnLRow("LABA KOTOR (GROSS PROFIT)", grossProfit, if (grossProfit >= 0) ColorProfit else ColorExpense, isBold = true)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "3. BIAYA OPERASIONAL & UMUM",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PnLRow("Beban Operasional Kantor / Workshop", summary.totalOperationalCost, ColorExpense)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    PnLRow("TOTAL BIAYA & BEBAN", summary.totalExpense, ColorExpense, isBold = true)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    PnLRow("LABA / RUGI BERSIH BERJALAN", summary.netProfit, if (isProfit) ColorProfit else ColorExpense, isBold = true, isLarge = true)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProjectPnLTab(projectSummaries: List<ProjectFinancialSummary>) {
    val context = LocalContext.current
    var selectedProjectId by remember { mutableStateOf<Long?>(projectSummaries.firstOrNull()?.project?.id) }
    var expandedSelector by remember { mutableStateOf(false) }

    val activeSummary = remember(projectSummaries, selectedProjectId) {
        projectSummaries.find { it.project.id == selectedProjectId } ?: projectSummaries.firstOrNull()
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            // Project Selector & Print Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expandedSelector = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pilih Proyek Bore Pile:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = activeSummary?.project?.name ?: "Pilih Proyek",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expandedSelector,
                        onDismissRequest = { expandedSelector = false }
                    ) {
                        projectSummaries.forEach { ps ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(ps.project.name, fontWeight = FontWeight.Bold)
                                        Text("${ps.project.clientName} (${ps.project.status})", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedProjectId = ps.project.id
                                    expandedSelector = false
                                }
                            )
                        }
                    }
                }

                if (activeSummary != null) {
                    Button(
                        onClick = {
                            PrintHelper.printHtml(
                                context = context,
                                jobName = "LabaRugi-${activeSummary.project.projectCode}",
                                htmlContent = PrintHelper.generateProjectPnLHtml(activeSummary)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak PDF", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (activeSummary != null) {
            val ps = activeSummary
            val isProfit = ps.grossProfit >= 0

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ps.project.projectCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                if (ps.project.status == "COMPLETED") "SELESAI" else "AKTIF",
                                fontWeight = FontWeight.Bold,
                                color = if (ps.project.status == "COMPLETED") ColorProfit else ColorExpense
                            )
                        }
                        Text(ps.project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${ps.project.clientName} • ${ps.project.location}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        PnLRow("Nilai Kontrak", ps.contractAmount, MaterialTheme.colorScheme.onSurface)
                        PnLRow("Pendapatan Diterima (Cash In)", ps.cashReceived, ColorProfit, isBold = true)
                        PnLRow("Sisa Piutang / Belum Cair", ps.remainingContractBalance, ColorExpense)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("RINCIAN BIAYA HPP BORE PILE:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        PnLRow("• Material Ready Mix & Besi", ps.costBreakdown.material, ColorExpense)
                        PnLRow("• Upah Operator, Mandor & Kenek", ps.costBreakdown.labor, ColorExpense)
                        PnLRow("• Mobilisasi & Demobilisasi Rig", ps.costBreakdown.mobilization, ColorExpense)
                        PnLRow("• BBM Solar & Pelumas", ps.costBreakdown.fuel, ColorExpense)
                        PnLRow("• Sewa Alat, Genset & Casing", ps.costBreakdown.equipment, ColorExpense)
                        PnLRow("• Perawatan & Mata Bor", ps.costBreakdown.maintenance, ColorExpense)
                        PnLRow("• Koordinasi & Lainnya", ps.costBreakdown.other, ColorExpense)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        PnLRow("TOTAL BIAYA HPP", ps.totalCost, ColorExpense, isBold = true)

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        PnLRow(
                            "LABA KOTOR PROYEK (GROSS PROFIT)",
                            ps.grossProfit,
                            if (isProfit) ColorProfit else ColorExpense,
                            isBold = true,
                            isLarge = true
                        )
                        PnLRow(
                            "MARGIN PROYEK",
                            0.0,
                            if (isProfit) ColorProfit else ColorExpense,
                            customValue = String.format(java.util.Locale.US, "%.1f%%", ps.marginPercent),
                            isBold = true
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PayablesReceivablesTab(
    receivables: List<ReceivableEntity>,
    payables: List<PayableEntity>
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("PIUTANG TERMIN KLIENT BORE PILE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        if (receivables.isEmpty()) {
            item {
                Text("Tidak ada piutang klien tercatat.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(receivables.size) { idx ->
                val rec = receivables[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(rec.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(rec.status, fontWeight = FontWeight.Bold, color = if (rec.status == "PAID") ColorProfit else ColorExpense)
                        }
                        Text("${rec.clientName} • ${rec.projectName}", style = MaterialTheme.typography.bodySmall)
                        Text(rec.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total: ${formatRupiah(rec.totalAmount)}", style = MaterialTheme.typography.bodySmall)
                            Text("Sisa: ${formatRupiah(rec.remainingAmount)}", fontWeight = FontWeight.Bold, color = ColorProfit)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("UTANG USAHA & SUPPLIER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        if (payables.isEmpty()) {
            item {
                Text("Tidak ada utang usaha tercatat.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(payables.size) { idx ->
                val pay = payables[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(pay.creditorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(pay.status, fontWeight = FontWeight.Bold, color = if (pay.status == "PAID") ColorProfit else ColorExpense)
                        }
                        Text(pay.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total: ${formatRupiah(pay.totalAmount)}", style = MaterialTheme.typography.bodySmall)
                            Text("Sisa: ${formatRupiah(pay.remainingAmount)}", fontWeight = FontWeight.Bold, color = ColorExpense)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PnLRow(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    isBold: Boolean = false,
    isLarge: Boolean = false,
    customValue: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isLarge) 15.sp else 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = customValue ?: formatRupiah(amount),
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = color,
            fontSize = if (isLarge) 16.sp else 13.sp
        )
    }
}
