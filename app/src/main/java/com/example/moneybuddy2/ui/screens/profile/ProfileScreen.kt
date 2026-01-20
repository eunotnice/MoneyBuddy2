package com.example.moneybuddy2.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.ProfileUiState
import com.example.moneybuddy2.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen (
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val repo = remember { AppContainer().repository }

    val vm: ProfileViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repo) as T
            }
        }
    )

    val ui by vm.uiState.collectAsState(initial = ProfileUiState(loading = true))

    LaunchedEffect(Unit) { vm.loadProfile() }

    var name by remember { mutableStateOf("") }
    var incomeText by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var goalText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("MYR") }

    // Pre-fill from existing profile once loaded
    LaunchedEffect(ui.profile) {
        val p = ui.profile ?: return@LaunchedEffect
        name = p.displayName
        incomeText = if (p.monthlyIncome > 0) p.monthlyIncome.toString() else ""
        budgetText = if (p.monthlyBudget > 0) p.monthlyBudget.toString() else ""
        goalText = if (p.savingGoal > 0) p.savingGoal.toString() else ""
        currency = p.currency.ifBlank { "MYR" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Setup") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
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

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it },
                label = { Text("Monthly income (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Monthly budget (required for alerts)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                label = { Text("Saving goal (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Currency (MVP: fixed, but kept as a field)
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val income = incomeText.toDoubleOrNull() ?: 0.0
                    val budget = budgetText.toDoubleOrNull() ?: 0.0
                    val goal = goalText.toDoubleOrNull() ?: 0.0

                    if (name.isBlank()) return@Button
                    if (budget <= 0.0) return@Button

                    vm.saveProfile(
                        displayName = name,
                        monthlyIncome = income,
                        monthlyBudget = budget,
                        savingGoal = goal,
                        currency = currency.ifBlank { "MYR" },
                        onDone = onDone
                    )
                },
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (ui.loading) "Saving..." else "Save Profile")
            }

            Text(
                "Tip: Set a monthly budget so MoneyBuddy can warn you at 90% usage.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}