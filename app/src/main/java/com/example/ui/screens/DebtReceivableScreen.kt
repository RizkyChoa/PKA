package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DebtReceivableEntity
import com.example.ui.components.Formatters
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600

@Composable
fun DebtReceivableScreen(
    items: List<DebtReceivableEntity>,
    onSettleClick: (DebtReceivableEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Piutang (Receivables), 1: Utang (Payables)

    val currentType = if (selectedTab == 0) "RECEIVABLE" else "DEBT"
    val filteredList = items.filter { it.type == currentType }

    val totalAmount = filteredList.sumOf { it.totalAmount }
    val totalPaid = filteredList.sumOf { it.paidAmount }
    val totalRemaining = filteredList.sumOf { it.totalAmount - it.paidAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("debt_receivable_screen")
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Piutang Klien (Tagihan)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Utang Usaha & Bank", fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (selectedTab == 0) "Total Piutang Belum Tertagih" else "Total Kewajiban Utang Belum Lunas",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = Formatters.rupiah(totalRemaining),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Amber600
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Kontrak/Kewajiban: ${Formatters.rupiah(totalAmount)}", style = MaterialTheme.typography.bodySmall)
                            Text("Sudah Dibayar: ${Formatters.rupiah(totalPaid)}", style = MaterialTheme.typography.bodySmall, color = Emerald600)
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tidak ada catatan saat ini.",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    val remaining = item.totalAmount - item.paidAmount
                    val progress = if (item.totalAmount > 0) (item.paidAmount / item.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
                    val isPaid = item.status == "PAID" || remaining <= 0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.partyName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isPaid) Emerald600 else if (item.paidAmount > 0) Amber600 else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (isPaid) "LUNAS" else if (item.paidAmount > 0) "SEBAGIAN" else "BELUM BAYAR",
                                        color = if (isPaid || item.paidAmount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (item.dueDate.isNotEmpty()) {
                                Text(
                                    text = "Jatuh Tempo: ${Formatters.simpleDate(item.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Rose600
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Tagihan / Pokok:")
                                Text(Formatters.rupiah(item.totalAmount), fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sudah Dibayar:")
                                Text(Formatters.rupiah(item.paidAmount), color = Emerald600, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sisa Kewajiban:", fontWeight = FontWeight.Bold)
                                Text(Formatters.rupiah(remaining), fontWeight = FontWeight.Bold, color = if (isPaid) Emerald600 else Rose600)
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = Emerald600,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            if (!isPaid) {
                                Button(
                                    onClick = { onSettleClick(item) },
                                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (item.type == "RECEIVABLE") "Catat Penerimaan" else "Catat Pembayaran")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
