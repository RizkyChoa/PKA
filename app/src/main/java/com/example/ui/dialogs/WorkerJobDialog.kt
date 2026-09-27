package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorkerJobItemEntity
import com.example.ui.theme.ColorProfit
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WorkerJobDialog(
    invoiceId: Long,
    initialJob: WorkerJobItemEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        jobId: Long?,
        invoiceId: Long,
        jobName: String,
        pointCount: Int,
        depthMeters: Double,
        unitPricePerMeter: Double
    ) -> Unit
) {
    var jobName by remember { mutableStateOf(initialJob?.jobName ?: "") }
    var pointCountStr by remember { mutableStateOf(initialJob?.pointCount?.toString() ?: "") }
    var depthMetersStr by remember { mutableStateOf(initialJob?.depthMeters?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var unitPriceStr by remember { mutableStateOf(initialJob?.unitPricePerMeter?.toLong()?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditMode = initialJob != null

    val pointCount = pointCountStr.toIntOrNull() ?: 0
    val depthMeters = depthMetersStr.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceStr.toDoubleOrNull() ?: 0.0

    val calculatedVolume = pointCount * depthMeters
    val calculatedSubtotal = calculatedVolume * unitPrice

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val sampleSuggestions = listOf(
        "Bore pile diameter 40cm",
        "Bore pile diameter 50cm",
        "Bore pile diameter 60cm",
        "Bobok pile cap tiang",
        "Pemasangan tulangan besi & cor"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Engineering else Icons.Default.Construction,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEditMode) "Edit Item Pekerjaan" else "Tambah Item Pekerjaan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Item rincian bore pile / borongan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Template / Suggestion Chips
                Text(
                    text = "Pilih Cepat / Saran Uraian:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleSuggestions.take(2).forEach { sample ->
                        FilterChip(
                            selected = jobName == sample,
                            onClick = { jobName = sample },
                            label = { Text(sample, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nama Pekerjaan Field
                OutlinedTextField(
                    value = jobName,
                    onValueChange = { jobName = it; errorMessage = null },
                    label = { Text("Nama / Uraian Pekerjaan *") },
                    placeholder = { Text("contoh: Bore pile diameter 40cm") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Row: Jumlah Titik & Kedalaman Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pointCountStr,
                        onValueChange = { pointCountStr = it.filter { ch -> ch.isDigit() }; errorMessage = null },
                        label = { Text("Jumlah Titik *") },
                        placeholder = { Text("50") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("job_point_count_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = depthMetersStr,
                        onValueChange = { depthMetersStr = it.filter { ch -> ch.isDigit() || ch == '.' }; errorMessage = null },
                        label = { Text("Kedalaman (m) *") },
                        placeholder = { Text("24.0") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("job_depth_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Harga Satuan per Meter
                OutlinedTextField(
                    value = unitPriceStr,
                    onValueChange = { unitPriceStr = it.filter { ch -> ch.isDigit() }; errorMessage = null },
                    label = { Text("Harga Satuan per Meter (Rp) *") },
                    placeholder = { Text("40000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_unit_price_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Calculation Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kalkulasi Otomatis Volume & Borongan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Volume ($pointCount ttk × $depthMeters m):",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$calculatedVolume m'",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtotal Penghasilan:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currencyFormat.format(calculatedSubtotal),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = ColorProfit
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (jobName.isBlank()) {
                                errorMessage = "Nama / Uraian pekerjaan wajib diisi"
                                return@Button
                            }
                            if (pointCount <= 0) {
                                errorMessage = "Jumlah titik harus lebih besar dari 0"
                                return@Button
                            }
                            if (depthMeters <= 0.0) {
                                errorMessage = "Kedalaman meter harus lebih besar dari 0"
                                return@Button
                            }
                            if (unitPrice <= 0.0) {
                                errorMessage = "Harga satuan per meter harus lebih dari 0"
                                return@Button
                            }

                            onSave(
                                initialJob?.id,
                                invoiceId,
                                jobName.trim(),
                                pointCount,
                                depthMeters,
                                unitPrice
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_job_btn")
                    ) {
                        Text(if (isEditMode) "Simpan Perubahan" else "Tambah Pekerjaan")
                    }
                }
            }
        }
    }
}
