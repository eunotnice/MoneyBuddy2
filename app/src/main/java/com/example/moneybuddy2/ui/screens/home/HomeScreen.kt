package com.example.moneybuddy2.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.HomeUiState
import com.example.moneybuddy2.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val repo = remember { AppContainer().repository }

    val vm: HomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repo) as T
            }
        }
    )

    val ui by vm.uiState.collectAsState(initial = HomeUiState(loading = true))

    LaunchedEffect(Unit) { vm.loadHome() }

    // Auto-redirect if profile incomplete
    LaunchedEffect(ui.needsProfileSetup) {
        if (ui.needsProfileSetup) onOpenProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyBuddy") },
                actions = {
                    TextButton(onClick = onOpenProfile) { Text("Profile") }
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (ui.error != null) Text(ui.error!!, color = MaterialTheme.colorScheme.error)

            // Budget card (with progress)
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This month spending", style = MaterialTheme.typography.titleMedium)
                    Text("Total: MYR %.2f".format(ui.monthTotal), style = MaterialTheme.typography.headlineSmall)

                    val budget = ui.profile?.monthlyBudget ?: 0.0
                    if (budget > 0.0) {
                        Text("Budget: MYR %.2f".format(budget))
                        LinearProgressIndicator(
                            progress = { ui.budgetUsedRatio.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (ui.showBudgetWarning) {
                            Text(
                                "Budget alert: ${(ui.budgetUsedRatio * 100).toInt()}% used",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text("Budget not set yet. Please complete your profile.")
                    }
                }
            }

            // Category breakdown card (this month)
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Category breakdown (this month)", style = MaterialTheme.typography.titleMedium)
                    if (ui.monthCategoryTotals.isEmpty()) {
                        Text("No expenses in this month yet.")
                    } else {
                        ui.monthCategoryTotals.forEach { (cat, total) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat)
                                Text("MYR %.2f".format(total))
                            }
                        }
                    }
                }
            }

            Text("Latest expenses", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.latestExpenses, key = { it.id }) { e ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(e.merchant.ifBlank { "(No merchant)" }, style = MaterialTheme.typography.titleMedium)
                                Text("MYR %.2f • %s".format(e.amount, e.category))
                                if (e.description.isNotBlank()) Text(e.description, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { vm.deleteExpense(e.id) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
