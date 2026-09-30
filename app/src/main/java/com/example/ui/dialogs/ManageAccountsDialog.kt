package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.formatRupiah

@Composable
fun ManageAccountsDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onCreateAccount: (name: String, type: String, accountNumber: String, initialBalance: Double) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onDeleteAccount: (Long) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("BANK") } // CASH, BANK, EWALLET
    var accountNumber by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        showForm = false
        accountToEdit = null
        name = ""
        selectedType = "BANK"
        accountNumber = ""
        initialBalanceText = ""
        formError = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
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
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Buku Kas & Rekening",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Kelola rekening bank dan kas tunai perusahaan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol / Form Tambah / Edit Rekening
                if (!showForm) {
                    Button(
                        onClick = {
                            resetForm()
                            showForm = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Rekening / Buku Kas Baru")
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (accountToEdit != null) "Edit Rekening" else "Tambah Rekening Baru",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (formError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Rekening / Kas *") },
                                placeholder = { Text("Contoh: Bank BCA PT Bore Pile") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Pilih Tipe
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedType == "BANK",
                                    onClick = { selectedType = "BANK" },
                                    label = { Text("Bank") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                FilterChip(
                                    selected = selectedType == "CASH",
                                    onClick = { selectedType = "CASH" },
                                    label = { Text("Kas Tunai") },
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                FilterChip(
                                    selected = selectedType == "EWALLET",
                                    onClick = { selectedType = "EWALLET" },
                                    label = { Text("E-Wallet") }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = { Text("Nomor Rekening (Opsional)") },
                                placeholder = { Text("Contoh: 123-456-7890") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = initialBalanceText,
                                onValueChange = { initialBalanceText = it },
                                label = { Text("Saldo Awal (Rp)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { resetForm() }) {
                                    Text("Batal")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (name.isBlank()) {
                                            formError = "Nama rekening tidak boleh kosong"
                                            return@Button
                                        }
                                        val initialBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                                        if (accountToEdit != null) {
                                            val current = accountToEdit!!
                                            val balanceDiff = initialBal - current.initialBalance
                                            onUpdateAccount(
                                                current.copy(
                                                    name = name.trim(),
                                                    type = selectedType,
                                                    accountNumber = accountNumber.trim(),
                                                    initialBalance = initialBal,
                                                    currentBalance = current.currentBalance + balanceDiff
                                                )
                                            )
                                        } else {
                                            onCreateAccount(name.trim(), selectedType, accountNumber.trim(), initialBal)
                                        }
                                        resetForm()
                                    }
                                ) {
                                    Text(if (accountToEdit != null) "Simpan Edit" else "Tambah")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // List Rekening
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (accounts.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada rekening/buku kas.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(accounts, key = { it.id }) { acc ->
                            AccountItemCard(
                                account = acc,
                                onEdit = {
                                    accountToEdit = acc
                                    name = acc.name
                                    selectedType = acc.type
                                    accountNumber = acc.accountNumber
                                    initialBalanceText = if (acc.initialBalance > 0) acc.initialBalance.toLong().toString() else "0"
                                    showForm = true
                                },
                                onDelete = {
                                    accountToDelete = acc
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (accountToDelete != null) {
        val target = accountToDelete!!
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Rekening Kas?") },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus rekening '${target.name}'?")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saldo saat ini: ${formatRupiah(target.currentBalance)}. Pastikan tidak ada transaksi aktif yang bergantung pada rekening ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccount(target.id)
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun AccountItemCard(
    account: AccountEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (account.type) {
                        "BANK" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        "CASH" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = when (account.type) {
                            "BANK" -> Icons.Default.AccountBalance
                            "CASH" -> Icons.Default.Payments
                            else -> Icons.Default.AccountBalanceWallet
                        },
                        contentDescription = null,
                        tint = when (account.type) {
                            "BANK" -> MaterialTheme.colorScheme.primary
                            "CASH" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = account.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (account.accountNumber.isNotBlank() && account.accountNumber != "-")
                                "${account.type} • ${account.accountNumber}"
                            else
                                account.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatRupiah(account.currentBalance),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Rekening",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus Rekening",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
