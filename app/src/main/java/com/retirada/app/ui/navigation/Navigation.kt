package com.retirada.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.retirada.app.ui.screens.HistoryScreen
import com.retirada.app.ui.screens.HomeScreen
import com.retirada.app.ui.screens.ReportsScreen
import com.retirada.app.ui.screens.SettingsScreen
import com.retirada.app.ui.screens.StockScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Início", Icons.Filled.Home),
    History("history", "Histórico", Icons.Filled.History),
    Stock("stock", "Estoque", Icons.Filled.Inventory2),
    Reports("reports", "Relatórios", Icons.Filled.BarChart),
    Settings("settings", "Config.", Icons.Filled.Settings)
}

@Composable
fun RetiradaApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = Tab.Home.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Home.route) { HomeScreen() }
            composable(Tab.History.route) { HistoryScreen() }
            composable(Tab.Stock.route) { StockScreen() }
            composable(Tab.Reports.route) { ReportsScreen() }
            composable(Tab.Settings.route) { SettingsScreen() }
        }
    }
}
