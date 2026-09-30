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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WorkerLoanItemEntity
import com.example.ui.theme.ColorExpense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkerLoanDialog(
    invoiceId: Long,
    initialLoan: WorkerLoanItemEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        loanId: Long?,
        date: String,
        description: String,
        trxType: String,
        amount: Double,
        deductionDescription: String,
        deductionAmount: Double
    ) -> Unit
) {
    val isEditMode = initialLoan != null
    val defaultDate = remember {
        initialLoan?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    var date by remember { mutableStateOf(defaultDate) }
    var description by remember { mutableStateOf(initialLoan?.description ?: "") }
    var trxType by remember { mutableStateOf(initialLoan?.trxType ?: "TRANSFER") }
    var amountStr by remember { mutableStateOf(if (initialLoan != null && initialLoan.amount > 0) initialLoan.amount.toLong().toString() else "") }
    var deductionDesc by remember { mutableStateOf(initialLoan?.deductionDescription ?: "") }
    var deductionAmountStr by remember { mutableStateOf(if (initialLoan != null && initialLoan.deductionAmount > 0) initialLoan.deductionAmount.toLong().toString() else "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
    val deductionDouble = deductionAmountStr.toDoubleOrNull() ?: 0.0
    val netAmount = (amountDouble - deductionDouble).coerceAtLeast(0.0)

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
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
                    Column {
                        Text(
                            text = if (isEditMode) "Edit Kasbon / Pinjaman" else "+ Tambah Kasbon / Pinjaman",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEditMode) "Perbarui data pinjaman mandor/pekerja" else "Catat kasbon baru yang bertambah seiring waktu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tanggal
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal Kasbon (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_input_date"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Keterangan Kasbon
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Kasbon *") },
                    placeholder = { Text("Misal: DP naik anggota, uang makan, solar dll.") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_input_description"),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Jenis Transaksi
                Text(
                    text = "Jenis Transaksi Pembayaran:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = trxType == "TRANSFER",
                        onClick = { trxType = "TRANSFER" },
                        label = { Text("Transfer Bank") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = trxType == "CASH",
                        onClick = { trxType = "CASH" },
                        label = { Text("Tunai / Cash") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nominal Kasbon
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Nominal Kasbon / Pinjaman (Rp) *") },
                    placeholder = { Text("0") },
                    leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ColorExpense) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_input_amount"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Potongan Opsional Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Potongan Khusus (Opsional):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = deductionDesc,
                            onValueChange = { deductionDesc = it },
                            label = { Text("Keterangan Potongan") },
                            placeholder = { Text("Misal: Ongkos keberangkatan tiket") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("loan_input_deduction_desc"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = deductionAmountStr,
                            onValueChange = { deductionAmountStr = it.filter { char -> char.isDigit() } },
                            label = { Text("Nominal Potongan (Rp)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("loan_input_deduction_amount"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ringkasan Bersih Kasbon
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Kasbon Bersih", style = MaterialTheme.typography.labelSmall)
                            if (deductionDouble > 0) {
                                Text(
                                    "(${rupiahFormat.format(amountDouble)} - ${rupiahFormat.format(deductionDouble)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = rupiahFormat.format(netAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorExpense
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                errorMessage = "Keterangan kasbon wajib diisi."
                                return@Button
                            }
                            if (amountDouble <= 0) {
                                errorMessage = "Nominal kasbon harus lebih dari 0."
                                return@Button
                            }
                            onSave(
                                initialLoan?.id,
                                date.trim(),
                                description.trim(),
                                trxType,
                                amountDouble,
                                deductionDesc.trim(),
                                deductionDouble
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("loan_save_button")
                    ) {
                        Text(if (isEditMode) "Simpan Perubahan" else "Tambahkan Kasbon")
                    }
                }
            }
        }
    }
}
