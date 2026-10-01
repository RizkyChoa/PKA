package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AppScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopAppBarBorePile
import com.example.ui.dialogs.AddProjectDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.AddWorkerInvoiceDialog
import com.example.ui.dialogs.ManageAccountsDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.SyncAndBackupDialog
import com.example.ui.dialogs.VoidTransactionDialog
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WorkerInvoicesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BorePileFinanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BorePileFinanceApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Dialog States
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddWorkerInvoiceDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showManageAccountsDialog by remember { mutableStateOf(false) }
    var transactionToVoid by remember { mutableStateOf<TransactionEntity?>(null) }

    // Back button handling: return to dashboard if on sub-screens
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    // Reactive State Collections
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    // STRICT ACTIVE PROJECTS for transaction entry (Completed projects are excluded!)
    val activeProjects by viewModel.activeProjects.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val globalSummary by viewModel.globalSummary.collectAsStateWithLifecycle()
    val projectSummaries by viewModel.projectSummaries.collectAsStateWithLifecycle()
    val dailySummaries by viewModel.dailySummaries.collectAsStateWithLifecycle()
    val receivables by viewModel.receivables.collectAsStateWithLifecycle()
    val payables by viewModel.payables.collectAsStateWithLifecycle()
    val profitPartners by viewModel.profitPartners.collectAsStateWithLifecycle()
    val workerInvoicesWithDetails by viewModel.workerInvoicesWithDetails.collectAsStateWithLifecycle()
    val selectedDashboardProjectId by viewModel.selectedDashboardProjectId.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncStateMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarBorePile(
                onOpenSync = { showSyncDialog = true },
                onOpenSettings = { showSettingsDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    summary = globalSummary,
                    accounts = accounts,
                    projectSummaries = projectSummaries,
                    selectedProjectId = selectedDashboardProjectId,
                    recentTransactions = allTransactions.take(5),
                    onSelectProject = { viewModel.selectDashboardProject(it) },
                    onOpenAddTransaction = {
                        transactionToEdit = null
                        showAddTransactionDialog = true
                    },
                    onNavigateToProjects = { currentScreen = AppScreen.PROJECTS },
                    modifier = screenModifier
                )
            }

            AppScreen.TRANSACTIONS -> {
                TransactionsScreen(
                    transactions = allTransactions,
                    allProjects = allProjects,
                    onOpenAddTransaction = {
                        transactionToEdit = null
                        showAddTransactionDialog = true
                    },
                    onEditTransaction = { trx ->
                        transactionToEdit = trx
                        showAddTransactionDialog = true
                    },
                    onDeleteTransaction = { trxId ->
                        viewModel.deleteTransaction(trxId)
                    },
                    onRequestVoidTransaction = { transactionToVoid = it },
                    modifier = screenModifier
                )
            }

            AppScreen.CASH_FLOW -> {
                CashFlowScreen(
                    summary = globalSummary,
                    accounts = accounts,
                    dailySummaries = dailySummaries,
                    onOpenAddTransaction = {
                        transactionToEdit = null
                        showAddTransactionDialog = true
                    },
                    onOpenManageAccounts = { showManageAccountsDialog = true },
                    modifier = screenModifier
                )
            }

            AppScreen.PROJECTS -> {
                ProjectsScreen(
                    projectSummaries = projectSummaries,
                    onOpenAddProject = { showAddProjectDialog = true },
                    onCompleteProject = { viewModel.completeProject(it) },
                    onReopenProject = { viewModel.reopenProject(it) },
                    onEditProject = { id, name, client, location, startDate, targetDate, contractAmount, notes, jobItems, mobiUnits, mobiPricePerUnit ->
                        viewModel.updateProject(
                            id = id,
                            name = name,
                            client = client,
                            location = location,
                            startDate = startDate,
                            targetDate = targetDate,
                            contractAmount = contractAmount,
                            notes = notes,
                            jobItems = jobItems,
                            mobiUnits = mobiUnits,
                            mobiPricePerUnit = mobiPricePerUnit
                        )
                    },
                    onDeleteProject = { viewModel.deleteProject(it) },
                    modifier = screenModifier
                )
            }

            AppScreen.WORKER_INVOICES -> {
                WorkerInvoicesScreen(
                    invoices = workerInvoicesWithDetails,
                    onOpenAddInvoice = { showAddWorkerInvoiceDialog = true },
                    onDeleteInvoice = { viewModel.deleteWorkerInvoice(it) },
                    // Pekerjaan callbacks
                    onAddJobItem = { invoiceId, jobName, pointCount, depthMeters, unitPricePerMeter ->
                        viewModel.addWorkerJobItem(invoiceId, jobName, pointCount, depthMeters, unitPricePerMeter)
                    },
                    onUpdateJobItem = { id, invoiceId, jobName, pointCount, depthMeters, unitPricePerMeter ->
                        viewModel.updateWorkerJobItem(id, invoiceId, jobName, pointCount, depthMeters, unitPricePerMeter)
                    },
                    onDeleteJobItem = { jobItemId ->
                        viewModel.deleteWorkerJobItem(jobItemId)
                    },
                    // Kasbon callbacks
                    onAddLoanItem = { invoiceId, date, description, trxType, amount, deductionDesc, deductionAmount ->
                        viewModel.addWorkerLoanItem(
                            invoiceId = invoiceId,
                            date = date,
                            description = description,
                            trxType = trxType,
                            amount = amount,
                            deductionDescription = deductionDesc,
                            deductionAmount = deductionAmount
                        )
                    },
                    onUpdateLoanItem = { id, invoiceId, date, description, trxType, amount, deductionDesc, deductionAmount ->
                        viewModel.updateWorkerLoanItem(
                            id = id,
                            invoiceId = invoiceId,
                            date = date,
                            description = description,
                            trxType = trxType,
                            amount = amount,
                            deductionDescription = deductionDesc,
                            deductionAmount = deductionAmount
                        )
                    },
                    onDeleteLoanItem = { loanId ->
                        viewModel.deleteWorkerLoanItem(loanId)
                    },
                    // Invoice Header callback
                    onUpdateInvoiceHeader = { updatedInvoice ->
                        viewModel.updateWorkerInvoice(updatedInvoice)
                    },
                    modifier = screenModifier
                )
            }

            AppScreen.REPORTS -> {
                ReportsScreen(
                    summary = globalSummary,
                    projectSummaries = projectSummaries,
                    receivables = receivables,
                    payables = payables,
                    profitPartners = profitPartners,
                    allTransactions = allTransactions,
                    accounts = accounts,
                    onCreatePartner = { name, pct, phone, notes ->
                        viewModel.createProfitPartner(name, pct, phone, notes)
                    },
                    onUpdatePartner = { id, name, pct, phone, notes ->
                        viewModel.updateProfitPartner(id, name, pct, phone, notes)
                    },
                    onDeletePartner = { id ->
                        viewModel.deleteProfitPartner(id)
                    },
                    onPayPayablesBatch = { pIds, accId, date, method, notes ->
                        viewModel.payPayablesBatch(pIds, accId, date, method, notes)
                    },
                    onPayReceivablesBatch = { rIds, accId, date, method, notes ->
                        viewModel.payReceivablesBatch(rIds, accId, date, method, notes)
                    },
                    onCreatePayable = { name, type, desc, amt, due, accId ->
                        viewModel.createPayable(name, type, desc, amt, due, accId)
                    },
                    onDeletePayable = { id ->
                        viewModel.deletePayable(id)
                    },
                    onCreateReceivable = { client, proj, inv, desc, amt, due, pId ->
                        viewModel.createReceivable(client, proj, inv, desc, amt, due, pId)
                    },
                    onDeleteReceivable = { id ->
                        viewModel.deleteReceivable(id)
                    },
                    onReceiveProjectPayment = { pId, amt, accId, date, method, notes ->
                        viewModel.receiveProjectPayment(pId, amt, accId, date, method, notes)
                    },
                    modifier = screenModifier
                )
            }
        }
    }

    // Modal Dialog: Add / Edit Transaction
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            transactionToEdit = transactionToEdit,
            accounts = accounts,
            activeProjects = activeProjects,
            categories = categories,
            profitPartners = profitPartners,
            payables = payables,
            receivables = receivables,
            onDismiss = {
                showAddTransactionDialog = false
                transactionToEdit = null
            },
            onSaveTransaction = { date, type, amount, desc, method, sourceId, destId, catId, classification, projId, costGrp, recId, payId, partnerId, partnerName ->
                if (transactionToEdit != null) {
                    viewModel.updateTransaction(
                        transactionToEdit!!.copy(
                            date = date,
                            type = type,
                            amount = amount,
                            description = desc,
                            paymentMethod = method,
                            sourceAccountId = sourceId,
                            destinationAccountId = destId,
                            categoryId = catId,
                            classification = classification,
                            projectId = projId,
                            costGroup = costGrp,
                            receivableId = recId,
                            payableId = payId,
                            profitPartnerId = partnerId,
                            profitPartnerName = partnerName
                        )
                    )
                } else {
                    viewModel.createTransaction(
                        date = date,
                        type = type,
                        amount = amount,
                        description = desc,
                        paymentMethod = method,
                        sourceAccountId = sourceId,
                        destinationAccountId = destId,
                        categoryId = catId,
                        classification = classification,
                        projectId = projId,
                        costGroup = costGrp,
                        receivableId = recId,
                        payableId = payId,
                        profitPartnerId = partnerId,
                        profitPartnerName = partnerName
                    )
                }
                showAddTransactionDialog = false
                transactionToEdit = null
            }
        )
    }

    // Modal Dialog: Add Project
    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSaveProject = { name, client, location, start, target, contract, notes, jobItems, mobiUnits, mobiPricePerUnit ->
                viewModel.createProject(
                    name = name,
                    client = client,
                    location = location,
                    startDate = start,
                    targetDate = target,
                    contractAmount = contract,
                    notes = notes,
                    jobItems = jobItems,
                    mobiUnits = mobiUnits,
                    mobiPricePerUnit = mobiPricePerUnit
                )
                showAddProjectDialog = false
            }
        )
    }

    // Modal Dialog: Add Worker Invoice
    if (showAddWorkerInvoiceDialog) {
        AddWorkerInvoiceDialog(
            projects = allProjects,
            onDismiss = { showAddWorkerInvoiceDialog = false },
            onSaveInvoice = { projId, projName, leader, role, date, notes, jobs, loans ->
                viewModel.createWorkerInvoice(
                    projectId = projId,
                    projectName = projName,
                    workerLeaderName = leader,
                    workerRole = role,
                    date = date,
                    notes = notes,
                    jobItems = jobs,
                    loanItems = loans
                )
                showAddWorkerInvoiceDialog = false
            }
        )
    }

    // Modal Dialog: Void Transaction
    if (transactionToVoid != null) {
        VoidTransactionDialog(
            transaction = transactionToVoid!!,
            onDismiss = { transactionToVoid = null },
            onConfirmVoid = { reason ->
                viewModel.voidTransaction(transactionToVoid!!.id, reason)
                transactionToVoid = null
            }
        )
    }

    // Modal Dialog: Sync and Backup Antar HP
    if (showSyncDialog) {
        SyncAndBackupDialog(
            projectCount = allProjects.size,
            transactionCount = allTransactions.size,
            invoiceCount = workerInvoicesWithDetails.size,
            syncMessage = syncMessage,
            onDismiss = { showSyncDialog = false },
            onExportBackup = { callback ->
                viewModel.exportBackup(context, callback)
            },
            onImportBackup = { uri, callback ->
                viewModel.importBackup(context, uri, callback)
            },
            onClearMessage = { viewModel.clearSyncMessage() }
        )
    }

    // Modal Dialog: Settings
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onOpenManageAccounts = { showManageAccountsDialog = true },
            onOpenBackupSync = { showSyncDialog = true },
            onResetAllData = { viewModel.resetAllData() }
        )
    }

    // Modal Dialog: Manage Accounts (Buku Kas & Rekening)
    if (showManageAccountsDialog) {
        ManageAccountsDialog(
            accounts = accounts,
            onDismiss = { showManageAccountsDialog = false },
            onCreateAccount = { name, type, accNumber, initialBal ->
                viewModel.createAccount(name, type, accNumber, initialBal)
            },
            onUpdateAccount = { acc ->
                viewModel.updateAccount(acc)
            },
            onDeleteAccount = { accId ->
                viewModel.deleteAccount(accId)
            }
        )
    }
}
