package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val exportedAt: String,
    val projects: List<ProjectEntity>,
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val receivables: List<ReceivableEntity>,
    val payables: List<PayableEntity>,
    val workerInvoices: List<WorkerInvoiceEntity>,
    val workerJobItems: List<WorkerJobItemEntity>,
    val workerLoanItems: List<WorkerLoanItemEntity>
)

object BackupManager {

    fun exportToJson(
        projects: List<ProjectEntity>,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        receivables: List<ReceivableEntity>,
        payables: List<PayableEntity>,
        workerInvoices: List<WorkerInvoiceEntity>,
        workerJobItems: List<WorkerJobItemEntity>,
        workerLoanItems: List<WorkerLoanItemEntity>
    ): String {
        val root = JSONObject()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        root.put("app", "BorePileFinance")
        root.put("version", 1)
        root.put("exportedAt", timestamp)

        // Projects
        val projArray = JSONArray()
        projects.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("projectCode", p.projectCode)
                put("name", p.name)
                put("clientName", p.clientName)
                put("location", p.location)
                put("startDate", p.startDate)
                put("targetDate", p.targetDate)
                put("contractAmount", p.contractAmount)
                put("status", p.status)
                put("notes", p.notes)
                put("createdAt", p.createdAt)
            }
            projArray.put(obj)
        }
        root.put("projects", projArray)

        // Accounts
        val accArray = JSONArray()
        accounts.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("type", a.type)
                put("accountNumber", a.accountNumber)
                put("initialBalance", a.initialBalance)
                put("currentBalance", a.currentBalance)
                put("isActive", a.isActive)
            }
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        // Categories
        val catArray = JSONArray()
        categories.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("type", c.type)
                put("classification", c.classification)
                put("costGroup", c.costGroup)
            }
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Transactions
        val trxArray = JSONArray()
        transactions.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("trxNumber", t.trxNumber)
                put("date", t.date)
                put("type", t.type)
                put("amount", t.amount)
                put("description", t.description)
                put("paymentMethod", t.paymentMethod)
                put("sourceAccountId", t.sourceAccountId)
                put("sourceAccountName", t.sourceAccountName)
                put("destinationAccountId", t.destinationAccountId ?: JSONObject.NULL)
                put("destinationAccountName", t.destinationAccountName ?: JSONObject.NULL)
                put("categoryId", t.categoryId)
                put("categoryName", t.categoryName)
                put("classification", t.classification)
                put("projectId", t.projectId ?: JSONObject.NULL)
                put("projectName", t.projectName ?: JSONObject.NULL)
                put("costGroup", t.costGroup)
                put("receivableId", t.receivableId ?: JSONObject.NULL)
                put("payableId", t.payableId ?: JSONObject.NULL)
                put("status", t.status)
                put("voidReason", t.voidReason)
                put("createdBy", t.createdBy)
                put("createdAt", t.createdAt)
            }
            trxArray.put(obj)
        }
        root.put("transactions", trxArray)

        // Receivables
        val recArray = JSONArray()
        receivables.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("projectId", r.projectId ?: JSONObject.NULL)
                put("projectName", r.projectName)
                put("clientName", r.clientName)
                put("invoiceNumber", r.invoiceNumber)
                put("description", r.description)
                put("totalAmount", r.totalAmount)
                put("paidAmount", r.paidAmount)
                put("dueDate", r.dueDate)
                put("status", r.status)
                put("createdAt", r.createdAt)
            }
            recArray.put(obj)
        }
        root.put("receivables", recArray)

        // Payables
        val payArray = JSONArray()
        payables.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("creditorName", p.creditorName)
                put("type", p.type)
                put("description", p.description)
                put("totalAmount", p.totalAmount)
                put("paidAmount", p.paidAmount)
                put("dueDate", p.dueDate)
                put("status", p.status)
                put("createdAt", p.createdAt)
            }
            payArray.put(obj)
        }
        root.put("payables", payArray)

        // Worker Invoices
        val invArray = JSONArray()
        workerInvoices.forEach { w ->
            val obj = JSONObject().apply {
                put("id", w.id)
                put("invoiceNumber", w.invoiceNumber)
                put("projectId", w.projectId)
                put("projectName", w.projectName)
                put("workerLeaderName", w.workerLeaderName)
                put("workerRole", w.workerRole)
                put("date", w.date)
                put("status", w.status)
                put("notes", w.notes)
                put("createdAt", w.createdAt)
            }
            invArray.put(obj)
        }
        root.put("workerInvoices", invArray)

        // Worker Job Items
        val jobArray = JSONArray()
        workerJobItems.forEach { j ->
            val obj = JSONObject().apply {
                put("id", j.id)
                put("invoiceId", j.invoiceId)
                put("jobName", j.jobName)
                put("pointCount", j.pointCount)
                put("depthMeters", j.depthMeters)
                put("volumeMeters", j.volumeMeters)
                put("unitPricePerMeter", j.unitPricePerMeter)
                put("subtotal", j.subtotal)
            }
            jobArray.put(obj)
        }
        root.put("workerJobItems", jobArray)

        // Worker Loan Items
        val loanArray = JSONArray()
        workerLoanItems.forEach { l ->
            val obj = JSONObject().apply {
                put("id", l.id)
                put("invoiceId", l.invoiceId)
                put("date", l.date)
                put("description", l.description)
                put("trxType", l.trxType)
                put("amount", l.amount)
                put("deductionDescription", l.deductionDescription)
                put("deductionAmount", l.deductionAmount)
            }
            loanArray.put(obj)
        }
        root.put("workerLoanItems", loanArray)

        return root.toString(2)
    }

    fun parseFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val exportedAt = root.optString("exportedAt", "Tidak diketahui")

        // Projects
        val projects = mutableListOf<ProjectEntity>()
        val projArray = root.optJSONArray("projects") ?: JSONArray()
        for (i in 0 until projArray.length()) {
            val obj = projArray.getJSONObject(i)
            projects.add(
                ProjectEntity(
                    id = obj.optLong("id", 0L),
                    projectCode = obj.optString("projectCode", "BP-${System.currentTimeMillis()}"),
                    name = obj.optString("name", "Proyek"),
                    clientName = obj.optString("clientName", "-"),
                    location = obj.optString("location", "-"),
                    startDate = obj.optString("startDate", ""),
                    targetDate = obj.optString("targetDate", ""),
                    contractAmount = obj.optDouble("contractAmount", 0.0),
                    status = obj.optString("status", "ACTIVE"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Accounts
        val accounts = mutableListOf<AccountEntity>()
        val accArray = root.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accArray.length()) {
            val obj = accArray.getJSONObject(i)
            accounts.add(
                AccountEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", "Akun"),
                    type = obj.optString("type", "CASH"),
                    accountNumber = obj.optString("accountNumber", ""),
                    initialBalance = obj.optDouble("initialBalance", 0.0),
                    currentBalance = obj.optDouble("currentBalance", 0.0),
                    isActive = obj.optBoolean("isActive", true)
                )
            )
        }

        // Categories
        val categories = mutableListOf<CategoryEntity>()
        val catArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catArray.length()) {
            val obj = catArray.getJSONObject(i)
            categories.add(
                CategoryEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", "Kategori"),
                    type = obj.optString("type", "EXPENSE"),
                    classification = obj.optString("classification", "OPERATIONAL_EXPENSE"),
                    costGroup = obj.optString("costGroup", "")
                )
            )
        }

        // Transactions
        val transactions = mutableListOf<TransactionEntity>()
        val trxArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until trxArray.length()) {
            val obj = trxArray.getJSONObject(i)
            transactions.add(
                TransactionEntity(
                    id = obj.optLong("id", 0L),
                    trxNumber = obj.optString("trxNumber", "TRX-${System.currentTimeMillis()}"),
                    date = obj.optString("date", ""),
                    type = obj.optString("type", "MONEY_OUT"),
                    amount = obj.optDouble("amount", 0.0),
                    description = obj.optString("description", ""),
                    paymentMethod = obj.optString("paymentMethod", "TRANSFER"),
                    sourceAccountId = obj.optLong("sourceAccountId", 1L),
                    sourceAccountName = obj.optString("sourceAccountName", ""),
                    destinationAccountId = if (obj.isNull("destinationAccountId")) null else obj.optLong("destinationAccountId"),
                    destinationAccountName = if (obj.isNull("destinationAccountName")) null else obj.optString("destinationAccountName"),
                    categoryId = obj.optLong("categoryId", 1L),
                    categoryName = obj.optString("categoryName", ""),
                    classification = obj.optString("classification", "OPERATIONAL"),
                    projectId = if (obj.isNull("projectId")) null else obj.optLong("projectId"),
                    projectName = if (obj.isNull("projectName")) null else obj.optString("projectName"),
                    costGroup = obj.optString("costGroup", ""),
                    receivableId = if (obj.isNull("receivableId")) null else obj.optLong("receivableId"),
                    payableId = if (obj.isNull("payableId")) null else obj.optLong("payableId"),
                    status = obj.optString("status", "VALID"),
                    voidReason = obj.optString("voidReason", ""),
                    createdBy = obj.optString("createdBy", "Admin"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Receivables
        val receivables = mutableListOf<ReceivableEntity>()
        val recArray = root.optJSONArray("receivables") ?: JSONArray()
        for (i in 0 until recArray.length()) {
            val obj = recArray.getJSONObject(i)
            receivables.add(
                ReceivableEntity(
                    id = obj.optLong("id", 0L),
                    projectId = if (obj.isNull("projectId")) null else obj.optLong("projectId"),
                    projectName = obj.optString("projectName", ""),
                    clientName = obj.optString("clientName", ""),
                    invoiceNumber = obj.optString("invoiceNumber", ""),
                    description = obj.optString("description", ""),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    dueDate = obj.optString("dueDate", ""),
                    status = obj.optString("status", "UNPAID"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Payables
        val payables = mutableListOf<PayableEntity>()
        val payArray = root.optJSONArray("payables") ?: JSONArray()
        for (i in 0 until payArray.length()) {
            val obj = payArray.getJSONObject(i)
            payables.add(
                PayableEntity(
                    id = obj.optLong("id", 0L),
                    creditorName = obj.optString("creditorName", ""),
                    type = obj.optString("type", "SUPPLIER"),
                    description = obj.optString("description", ""),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    paidAmount = obj.optDouble("paidAmount", 0.0),
                    dueDate = obj.optString("dueDate", ""),
                    status = obj.optString("status", "UNPAID"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Worker Invoices
        val workerInvoices = mutableListOf<WorkerInvoiceEntity>()
        val invArray = root.optJSONArray("workerInvoices") ?: JSONArray()
        for (i in 0 until invArray.length()) {
            val obj = invArray.getJSONObject(i)
            workerInvoices.add(
                WorkerInvoiceEntity(
                    id = obj.optLong("id", 0L),
                    invoiceNumber = obj.optString("invoiceNumber", ""),
                    projectId = obj.optLong("projectId", 0L),
                    projectName = obj.optString("projectName", ""),
                    workerLeaderName = obj.optString("workerLeaderName", ""),
                    workerRole = obj.optString("workerRole", "PEKERJA"),
                    date = obj.optString("date", ""),
                    status = obj.optString("status", "LUNAS"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Worker Job Items
        val workerJobItems = mutableListOf<WorkerJobItemEntity>()
        val jobArray = root.optJSONArray("workerJobItems") ?: JSONArray()
        for (i in 0 until jobArray.length()) {
            val obj = jobArray.getJSONObject(i)
            workerJobItems.add(
                WorkerJobItemEntity(
                    id = obj.optLong("id", 0L),
                    invoiceId = obj.optLong("invoiceId", 0L),
                    jobName = obj.optString("jobName", ""),
                    pointCount = obj.optInt("pointCount", 1),
                    depthMeters = obj.optDouble("depthMeters", 0.0),
                    volumeMeters = obj.optDouble("volumeMeters", 0.0),
                    unitPricePerMeter = obj.optDouble("unitPricePerMeter", 0.0),
                    subtotal = obj.optDouble("subtotal", 0.0)
                )
            )
        }

        // Worker Loan Items
        val workerLoanItems = mutableListOf<WorkerLoanItemEntity>()
        val loanArray = root.optJSONArray("workerLoanItems") ?: JSONArray()
        for (i in 0 until loanArray.length()) {
            val obj = loanArray.getJSONObject(i)
            workerLoanItems.add(
                WorkerLoanItemEntity(
                    id = obj.optLong("id", 0L),
                    invoiceId = obj.optLong("invoiceId", 0L),
                    date = obj.optString("date", ""),
                    description = obj.optString("description", ""),
                    trxType = obj.optString("trxType", "TRANSFER"),
                    amount = obj.optDouble("amount", 0.0),
                    deductionDescription = obj.optString("deductionDescription", ""),
                    deductionAmount = obj.optDouble("deductionAmount", 0.0)
                )
            )
        }

        return BackupData(
            exportedAt = exportedAt,
            projects = projects,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            receivables = receivables,
            payables = payables,
            workerInvoices = workerInvoices,
            workerJobItems = workerJobItems,
            workerLoanItems = workerLoanItems
        )
    }

    fun saveBackupToCache(context: Context, jsonString: String): File {
        val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(backupDir, "BorePile_Data_$dateStamp.json")
        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun shareBackupFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Cadangan Data BorePile Finance - ${file.name}")
            putExtra(Intent.EXTRA_TEXT, "File cadangan data BorePile Finance. Impor file ini di aplikasi BorePile Finance di perangkat lain untuk menyinkronkan data.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Kirim / Bagikan Cadangan Data")
        context.startActivity(chooser)
    }

    fun readJsonFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("Tidak dapat membaca file cadangan")
    }
}
