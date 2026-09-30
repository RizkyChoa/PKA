package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WorkerInvoiceEntity

@Composable
fun EditWorkerInvoiceHeaderDialog(
    invoice: WorkerInvoiceEntity,
    onDismiss: () -> Unit,
    onSave: (WorkerInvoiceEntity) -> Unit
) {
    var workerRole by remember { mutableStateOf(invoice.workerRole) }
    var workerLeaderName by remember { mutableStateOf(invoice.workerLeaderName) }
    var date by remember { mutableStateOf(invoice.date) }
    var status by remember { mutableStateOf(invoice.status) }
    var notes by remember { mutableStateOf(invoice.notes) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .navigationBarsPadding()
                .imePadding()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ubah Data Invoice",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${invoice.invoiceNumber} • ${invoice.projectName}",
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

                // Kategori Penerima
                Text(
                    text = "Kategori Penerima Upah:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = workerRole == "MANDOR",
                        onClick = { workerRole = "MANDOR" },
                        label = { Text("Upah Mandor", fontWeight = if (workerRole == "MANDOR") FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = workerRole == "PEKERJA",
                        onClick = { workerRole = "PEKERJA" },
                        label = { Text("Upah Pekerja", fontWeight = if (workerRole == "PEKERJA") FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nama Mandor / Pekerja
                OutlinedTextField(
                    value = workerLeaderName,
                    onValueChange = { workerLeaderName = it; errorMessage = null },
                    label = { Text(if (workerRole == "MANDOR") "Nama Mandor Lapangan *" else "Nama Tim Pekerja *") },
                    placeholder = { Text("contoh: Pak Bambang (Mandor 1)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_mandor_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tanggal Invoice (YYYY-MM-DD)
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it; errorMessage = null },
                    label = { Text("Tanggal Invoice (YYYY-MM-DD) *") },
                    placeholder = { Text("2026-09-27") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_invoice_date_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Invoice (LUNAS / PROSES)
                Text(
                    text = "Status Pembayaran:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == "LUNAS",
                        onClick = { status = "LUNAS" },
                        label = { Text("LUNAS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = status == "PROSES",
                        onClick = { status = "PROSES" },
                        label = { Text("PROSES / BERJALAN", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Catatan / Keterangan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan Tambahan") },
                    placeholder = { Text("contoh: Sisa borongan dibayarkan via transfer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_invoice_notes_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

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
                            if (workerLeaderName.isBlank()) {
                                errorMessage = "Nama mandor/pekerja wajib diisi"
                                return@Button
                            }
                            if (date.isBlank()) {
                                errorMessage = "Tanggal invoice wajib diisi"
                                return@Button
                            }

                            onSave(
                                invoice.copy(
                                    workerLeaderName = workerLeaderName.trim(),
                                    workerRole = workerRole,
                                    date = date.trim(),
                                    status = status,
                                    notes = notes.trim()
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_edit_invoice_header_btn")
                    ) {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}
