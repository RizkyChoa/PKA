package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    TRANSACTIONS("Transaksi", Icons.Default.ReceiptLong),
    CASH_FLOW("Arus Kas", Icons.Default.AccountBalanceWallet),
    PROJECTS("Proyek", Icons.Default.Construction),
    WORKER_INVOICES("Pekerja", Icons.Default.Engineering),
    REPORTS("Laporan", Icons.Default.Assessment)
}

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        AppScreen.values().forEach { screen ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        screen.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
            )
        }
    }
}
