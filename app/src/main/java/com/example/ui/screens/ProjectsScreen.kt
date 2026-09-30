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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BorePileJobItem
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFinancialSummary
import com.example.data.model.formatRupiah
import com.example.ui.dialogs.AddProjectDialog
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorProfit
import com.example.ui.theme.ColorWarning

@Composable
fun ProjectsScreen(
    projectSummaries: List<ProjectFinancialSummary>,
    onOpenAddProject: () -> Unit,
    onCompleteProject: (Long) -> Unit,
    onReopenProject: (Long) -> Unit,
    onEditProject: (
        id: Long,
        name: String,
        client: String,
        location: String,
        startDate: String,
        targetDate: String,
        contractAmount: Double,
        notes: String,
        jobItems: List<BorePileJobItem>,
        mobiUnits: Int,
        mobiPricePerUnit: Double
    ) -> Unit,
    onDeleteProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Proyek Berjalan (Aktif), 1: Proyek Selesai (Completed)

    var projectToComplete by remember { mutableStateOf<ProjectFinancialSummary?>(null) }
    var projectToReopen by remember { mutableStateOf<ProjectFinancialSummary?>(null) }
    var projectToEdit by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectFinancialSummary?>(null) }

    val activeSummaries = remember(projectSummaries) {
        projectSummaries.filter { it.project.status != "COMPLETED" && it.project.status != "CANCELLED" }
    }

    val completedSummaries = remember(projectSummaries) {
        projectSummaries.filter { it.project.status == "COMPLETED" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Header & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Aktif (${activeSummaries.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Selesai (${completedSummaries.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenAddProject,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Proyek", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Important Information Banner regarding project closing
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedTab == 0)
                        "Proyek aktif dapat dipilih saat mencatat biaya/pengeluaran baru."
                    else
                        "Proyek selesai dikunci dari transaksi baru, namun semua histori biaya & laba rugi tetap tersimpan aman.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = if (selectedTab == 0) activeSummaries else completedSummaries

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Belum ada proyek bore pile aktif." else "Belum ada proyek yang diselesaikan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(currentList, key = { it.project.id }) { ps ->
                    ProjectCard(
                        summary = ps,
                        onEditClick = { projectToEdit = ps.project },
                        onDeleteClick = { projectToDelete = ps },
                        onCompleteClick = { projectToComplete = ps },
                        onReopenClick = { projectToReopen = ps }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Edit Project Dialog
    if (projectToEdit != null) {
        val currentP = projectToEdit!!
        AddProjectDialog(
            projectToEdit = currentP,
            onDismiss = { projectToEdit = null },
            onSaveProject = { name, client, location, startDate, targetDate, contractAmount, notes, jobItems, mobiUnits, mobiPricePerUnit ->
                onEditProject(
                    currentP.id,
                    name,
                    client,
                    location,
                    startDate,
                    targetDate,
                    contractAmount,
                    notes,
                    jobItems,
                    mobiUnits,
                    mobiPricePerUnit
                )
                projectToEdit = null
            }
        )
    }

    // Delete Project Confirmation Dialog
    if (projectToDelete != null) {
        val p = projectToDelete!!.project
        val costs = projectToDelete!!.totalCost
        val revenue = projectToDelete!!.cashReceived
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Proyek Ini?") },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus proyek '${p.name}' (${p.projectCode})?",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (costs > 0 || revenue > 0) {
                        Text(
                            text = "Peringatan: Proyek ini tercatat memiliki penerimaan ${formatRupiah(revenue)} dan biaya ${formatRupiah(costs)}. Menghapus proyek akan menghapusnya dari daftar proyek aktif.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Data proyek ini akan dihapus secara permanen dari daftar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(p.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_project_button")
                ) {
                    Text("Ya, Hapus Proyek")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Complete Project Dialog
    if (projectToComplete != null) {
        val p = projectToComplete!!.project
        AlertDialog(
            onDismissRequest = { projectToComplete = null },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorProfit) },
            title = { Text("Tandai Proyek Selesai?") },
            text = {
                Text(
                    "Setelah proyek '${p.name}' diselesaikan, proyek ini TIDAK AKAN MUNCUL lagi sebagai pilihan pengeluaran/transaksi baru, tetapi seluruh histori keuangan dan laba rugi tetap dapat diakses di laporan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCompleteProject(p.id)
                        projectToComplete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorProfit)
                ) {
                    Text("Ya, Selesaikan Proyek")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToComplete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Reopen Project Dialog
    if (projectToReopen != null) {
        val p = projectToReopen!!.project
        AlertDialog(
            onDismissRequest = { projectToReopen = null },
            icon = { Icon(Icons.Default.Replay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Buka Kembali Proyek?") },
            text = {
                Text("Proyek '${p.name}' akan kembali berstatus AKTIF dan dapat dipilih kembali pada form transaksi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReopenProject(p.id)
                        projectToReopen = null
                    }
                ) {
                    Text("Buka Kembali")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToReopen = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProjectCard(
    summary: ProjectFinancialSummary,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onReopenClick: () -> Unit
) {
    val project = summary.project
    val isCompleted = project.status == "COMPLETED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Code, Title, Status Badge, Edit & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = project.projectCode,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) ColorProfit.copy(alpha = 0.15f) else ColorWarning.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isCompleted) "SELESAI" else "BERJALAN",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) ColorProfit else ColorWarning,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_edit_project_${project.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Proyek",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_project_${project.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Proyek",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(project.clientName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(project.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (project.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Spesifikasi: ${project.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val jobList = remember(project) { project.getJobItemsList() }
            if (jobList.isNotEmpty() || project.mobiUnits > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        jobList.forEachIndexed { idx, j ->
                            Text(
                                text = "• ${j.jobName.ifBlank { "Bor #${idx+1}" }}: ${j.pointCount} Titik × ${j.depthMeters}m (${j.totalMeters.toInt()}m') @ ${formatRupiah(j.pricePerMeter)}/m = ${formatRupiah(j.subtotal)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (project.mobiUnits > 0) {
                            Text(
                                text = "• Mobilisasi: ${project.mobiUnits} Unit @ ${formatRupiah(project.mobiPricePerUnit)} = ${formatRupiah(project.mobiUnits * project.mobiPricePerUnit)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Financial Metrics 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nilai Kontrak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(summary.contractAmount), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Penerimaan (Cash In)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(summary.cashReceived), fontWeight = FontWeight.Bold, color = ColorProfit, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Biaya HPP Bor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(summary.totalCost), fontWeight = FontWeight.Bold, color = ColorExpense, style = MaterialTheme.typography.bodyMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Laba Kotor / Margin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${formatRupiah(summary.grossProfit)} (${String.format(java.util.Locale.US, "%.1f%%", summary.marginPercent)})",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.grossProfit >= 0) ColorProfit else ColorExpense,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            if (!isCompleted) {
                Button(
                    onClick = onCompleteClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Selesaikan Proyek (Complete Project)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = onReopenClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Kembali Proyek (Reopen)", fontSize = 12.sp)
                }
            }
        }
    }
}
