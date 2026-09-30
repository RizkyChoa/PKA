package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.BorePileJobItem
import com.example.data.model.ProjectEntity
import com.example.data.model.formatRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class JobItemState(
    val id: String = UUID.randomUUID().toString(),
    initialName: String = "Bore Pile D-60",
    initialPoints: String = "",
    initialDepth: String = "",
    initialPrice: String = ""
) {
    var jobName by mutableStateOf(initialName)
    var pointCountText by mutableStateOf(initialPoints)
    var depthMetersText by mutableStateOf(initialDepth)
    var pricePerMeterText by mutableStateOf(initialPrice)

    val pointCount: Int get() = pointCountText.toIntOrNull() ?: 0
    val depthMeters: Double get() = depthMetersText.toDoubleOrNull() ?: 0.0
    val pricePerMeter: Double get() = pricePerMeterText.toDoubleOrNull() ?: 0.0
    val totalMeters: Double get() = pointCount * depthMeters
    val subtotal: Double get() = totalMeters * pricePerMeter
}

@Composable
fun AddProjectDialog(
    projectToEdit: ProjectEntity? = null,
    onDismiss: () -> Unit,
    onSaveProject: (
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
    ) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var name by remember { mutableStateOf(projectToEdit?.name ?: "") }
    var client by remember { mutableStateOf(projectToEdit?.clientName ?: "") }
    var location by remember { mutableStateOf(projectToEdit?.location ?: "") }
    var startDate by remember { mutableStateOf(projectToEdit?.startDate ?: todayStr) }
    var targetDate by remember { mutableStateOf(projectToEdit?.targetDate ?: "") }

    // Multiple Job Items State List
    val jobItems = remember {
        mutableStateListOf<JobItemState>().apply {
            val existing = projectToEdit?.getJobItemsList().orEmpty()
            if (existing.isNotEmpty()) {
                existing.forEach {
                    add(
                        JobItemState(
                            id = it.id,
                            initialName = it.jobName,
                            initialPoints = if (it.pointCount > 0) it.pointCount.toString() else "",
                            initialDepth = if (it.depthMeters > 0.0) it.depthMeters.toString() else "",
                            initialPrice = if (it.pricePerMeter > 0.0) it.pricePerMeter.toLong().toString() else ""
                        )
                    )
                }
            } else {
                add(JobItemState(initialName = "Bore Pile D-60"))
            }
        }
    }

    // Mobilisasi / Demobilisasi
    var mobiUnitsText by remember {
        mutableStateOf(if ((projectToEdit?.mobiUnits ?: 0) > 0) projectToEdit!!.mobiUnits.toString() else "")
    }
    var mobiPricePerUnitText by remember {
        mutableStateOf(if ((projectToEdit?.mobiPricePerUnit ?: 0.0) > 0.0) projectToEdit!!.mobiPricePerUnit.toLong().toString() else "")
    }

    val mobiUnits by remember { derivedStateOf { mobiUnitsText.toIntOrNull() ?: 0 } }
    val mobiPricePerUnit by remember { derivedStateOf { mobiPricePerUnitText.toDoubleOrNull() ?: 0.0 } }
    val subtotalMobi by remember { derivedStateOf { mobiUnits * mobiPricePerUnit } }

    // Hitungan Total Semua Pekerjaan Bor
    val totalBoringSubtotal by remember {
        derivedStateOf { jobItems.sumOf { it.subtotal } }
    }
    val totalBoringPoints by remember {
        derivedStateOf { jobItems.sumOf { it.pointCount } }
    }
    val totalBoringMeters by remember {
        derivedStateOf { jobItems.sumOf { it.totalMeters } }
    }

    val systematicTotal by remember {
        derivedStateOf { totalBoringSubtotal + subtotalMobi }
    }

    var manualContractAmountText by remember {
        mutableStateOf(
            if (projectToEdit != null && projectToEdit.contractAmount > 0) {
                projectToEdit.contractAmount.toLong().toString()
            } else ""
        )
    }

    val effectiveContractAmount by remember {
        derivedStateOf {
            val manualVal = manualContractAmountText.toDoubleOrNull()
            if (manualVal != null && manualVal > 0) {
                manualVal
            } else {
                systematicTotal
            }
        }
    }

    var notes by remember { mutableStateOf(projectToEdit?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun doValidateAndSubmit() {
        if (name.isBlank()) {
            errorMessage = "Nama proyek tidak boleh kosong"
            return
        }
        if (client.isBlank()) {
            errorMessage = "Nama klien / pemilik proyek wajib diisi"
            return
        }
        if (effectiveContractAmount <= 0) {
            errorMessage = "Nilai kontrak belum terhitung. Isi rincian pekerjaan bor atau nilai kontrak proyek."
            return
        }

        val compiledJobs = jobItems.map {
            BorePileJobItem(
                id = it.id,
                jobName = it.jobName.trim().ifBlank { "Bore Pile" },
                pointCount = it.pointCount,
                depthMeters = it.depthMeters,
                pricePerMeter = it.pricePerMeter
            )
        }

        onSaveProject(
            name.trim(),
            client.trim(),
            location.trim(),
            startDate.trim(),
            targetDate.trim(),
            effectiveContractAmount,
            notes.trim(),
            compiledJobs,
            mobiUnits,
            mobiPricePerUnit
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. STICKY TOP HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (projectToEdit != null) "Edit Proyek: ${projectToEdit.projectCode}" else "Buat Proyek Bore Pile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Rincian sistematis pekerjaan bor, volume & kontrak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Selesai button on header
                        Button(
                            onClick = { doValidateAndSubmit() },
                            modifier = Modifier
                                .testTag("top_submit_project_button")
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selesai", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_project_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 2. SCROLLABLE FORM CONTENT
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Field Identitas Proyek
                    Text(
                        text = "Informasi Utama Proyek",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Proyek *") },
                        placeholder = { Text("Contoh: Bore Pile Gedung Bertingkat") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = client,
                            onValueChange = { client = it },
                            label = { Text("Klien / Pemilik *") },
                            placeholder = { Text("PT atau Perorangan") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("project_client_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Lokasi Pekerjaan") },
                            placeholder = { Text("Kota / Daerah") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("project_location_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Mulai (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = targetDate,
                            onValueChange = { targetDate = it },
                            label = { Text("Target Selesai") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: DAFTAR PEKERJAAN BORE PILE (BISA MULTI PEKERJAAN / MULTI DIAMETER)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Construction,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Pekerjaan Pengeboran (${jobItems.size} Jenis)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Button Tambah Pekerjaan Bor
                        OutlinedButton(
                            onClick = {
                                val nextNum = jobItems.size + 1
                                jobItems.add(JobItemState(initialName = "Bore Pile D-${if (nextNum == 2) "80" else "40"}"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Pekerjaan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Card tiap item pekerjaan
                    jobItems.forEachIndexed { index, item ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Item #${index + 1}: ${item.jobName.ifBlank { "Pekerjaan Bor" }}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (jobItems.size > 1) {
                                        IconButton(
                                            onClick = { jobItems.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Hapus Item",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = item.jobName,
                                    onValueChange = { item.jobName = it },
                                    label = { Text("Jenis / Diameter Pekerjaan") },
                                    placeholder = { Text("Contoh: Bore Pile D-60, D-80, Strauss Pile") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.pointCountText,
                                        onValueChange = { item.pointCountText = it },
                                        label = { Text("Titik") },
                                        placeholder = { Text("50") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.depthMetersText,
                                        onValueChange = { item.depthMetersText = it },
                                        label = { Text("Kedalaman(m)") },
                                        placeholder = { Text("20") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.pricePerMeterText,
                                        onValueChange = { item.pricePerMeterText = it },
                                        label = { Text("Harga/m (Rp)") },
                                        placeholder = { Text("150000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.5f),
                                        singleLine = true
                                    )
                                }

                                if (item.pointCount > 0 && item.depthMeters > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Volume: ${item.totalMeters} m' (${item.pointCount} titik × ${item.depthMeters}m)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Subtotal: ${formatRupiah(item.subtotal)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ringkasan Subtotal Seluruh Pekerjaan Bor
                    if (totalBoringPoints > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Bor: $totalBoringPoints Titik • $totalBoringMeters m'",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Subtotal Bor: ${formatRupiah(totalBoringSubtotal)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 2: MOBILISASI ALAT
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Mobilisasi & Demobilisasi Alat Berat",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = mobiUnitsText,
                                    onValueChange = { mobiUnitsText = it },
                                    label = { Text("Jumlah Unit") },
                                    placeholder = { Text("1") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = mobiPricePerUnitText,
                                    onValueChange = { mobiPricePerUnitText = it },
                                    label = { Text("Biaya per Unit (Rp)") },
                                    placeholder = { Text("15000000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.8f),
                                    singleLine = true
                                )
                            }

                            if (mobiUnits > 0 && mobiPricePerUnit > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Subtotal Mob/Demob: ${formatRupiah(subtotalMobi)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 3: RINGKASAN NILAI KONTRAK
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Hasil Hitungan Kontrak Sistematis",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatRupiah(effectiveContractAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (systematicTotal > 0 && manualContractAmountText.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = { manualContractAmountText = "" }
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pakai Hitungan", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = manualContractAmountText,
                                onValueChange = { manualContractAmountText = it },
                                label = { Text("Sesuaikan Manual Nilai Kontrak (Opsional)") },
                                placeholder = { Text(if (systematicTotal > 0) systematicTotal.toLong().toString() else "Rp Kontrak Total") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan / Spesifikasi Teknis") },
                        placeholder = { Text("Contoh: Menggunakan bentonite, casing 6m, uji PDA...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                    // Extra breathing space at bottom of scrollable area so all content can scroll comfortably
                    Spacer(modifier = Modifier.height(36.dp))
                }

                // 3. STICKY BOTTOM ACTION BAR (SELALU TERLIHAT & TIDAK TERTUTUP NAVIGASI HP!)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total Kontrak Proyek",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatRupiah(effectiveContractAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Batal")
                            }

                            Button(
                                onClick = { doValidateAndSubmit() },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("save_project_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (projectToEdit != null) "Selesai & Simpan" else "Selesai / Submit",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
