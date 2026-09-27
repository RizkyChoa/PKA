package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.Formatters
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600
import com.example.ui.viewmodel.FinancialSummary

@Composable
fun ProfitLossScreen(
    summary: FinancialSummary
) {
    val grossProfit = summary.totalRevenue - summary.totalProjectCost
    val grossMargin = if (summary.totalRevenue > 0) (grossProfit / summary.totalRevenue) * 100.0 else 0.0
    val netMargin = if (summary.totalRevenue > 0) (summary.netProfit / summary.totalRevenue) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profit_loss_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Net Profit Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.netProfit >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LABA / RUGI BERSIH (NET PROFIT)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = Formatters.rupiah(summary.netProfit),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.netProfit >= 0) Emerald600 else Rose600
                    )
                    Text(
                        text = "Net Profit Margin: ${String.format("%.1f", netMargin)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Laporan Laba Rugi Komprehensif
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Laporan Laba Rugi Komprehensif", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    // SECTION 1: PENDAPATAN
                    Text("1. PENDAPATAN USAHA (REVENUE)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    PlRowItem("Pendapatan & Termin Proyek Bore Pile", summary.totalRevenue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL PENDAPATAN", fontWeight = FontWeight.Bold)
                        Text(Formatters.rupiah(summary.totalRevenue), fontWeight = FontWeight.Bold, color = Emerald600)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // SECTION 2: BEBAN POKOK PROYEK
                    Text("2. BEBAN POKOK PROYEK (COST OF REVENUE)", fontWeight = FontWeight.Bold, color = Rose600)
                    PlRowItem("Biaya Lapangan (Material, Upah, Mobilisasi, Solar)", summary.totalProjectCost)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL BEBAN POKOK PROYEK", fontWeight = FontWeight.Bold)
                        Text(Formatters.rupiah(summary.totalProjectCost), fontWeight = FontWeight.Bold, color = Rose600)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // GROSS PROFIT
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LABA KOTOR (GROSS PROFIT):", fontWeight = FontWeight.Bold)
                            Text(
                                Formatters.rupiah(grossProfit),
                                fontWeight = FontWeight.Bold,
                                color = if (grossProfit >= 0) Emerald600 else Rose600
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // SECTION 3: BEBAN OPERASIONAL
                    Text("3. BEBAN OPERASIONAL & UMUM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    PlRowItem("Sewa Kantor, Utilitas, Staf & Administrasi", summary.totalOperationalCost)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL BEBAN OPERASIONAL", fontWeight = FontWeight.Bold)
                        Text(Formatters.rupiah(summary.totalOperationalCost), fontWeight = FontWeight.Bold, color = Rose600)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // FINAL NET PROFIT
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LABA BERSIH (NET PROFIT):", fontWeight = FontWeight.ExtraBold)
                            Text(
                                Formatters.rupiah(summary.netProfit),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (summary.netProfit >= 0) Emerald600 else Rose600
                            )
                        }
                    }
                }
            }
        }

        // Edukasi / Konfirmasi Aturan Akuntansi
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Prinsip Akuntansi Terintegrasi:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text(
                            "• Setoran Modal Pemilik dan Pencairan Pinjaman Utang TIDAK dihitung sebagai Pendapatan di Laba Rugi ini.\n" +
                            "• Pembayaran Pokok Utang dan Pembelian Aset Tetap TIDAK dimasukkan sebagai Beban di Laba Rugi ini.\n" +
                            "• Semuanya tetap tercatat akurat dan lengkap pada Arus Kas & Saldo Rekening.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlRowItem(title: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(Formatters.rupiah(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
