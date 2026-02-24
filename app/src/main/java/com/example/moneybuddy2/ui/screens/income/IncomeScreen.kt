package com.example.moneybuddy2.ui.screens.income

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.core.Constants
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.ExpenseUiState
import com.example.moneybuddy2.ui.viewmodel.ExpenseViewModel
import com.example.moneybuddy2.ui.viewmodel.IncomeUiState
import com.example.moneybuddy2.ui.viewmodel.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit
) {
    val repo = remember { AppContainer().repository }

    val vm: IncomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IncomeViewModel(repo) as T
            }
        }
    )

    val uiState by vm.uiState.collectAsState(initial = IncomeUiState())

    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salary") }
    var description by remember { mutableStateOf("")}

    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember { mutableStateOf(cal.timeInMillis) }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Income") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
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
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (MYR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                val incomeCategories = listOf(
                    "Salary",
                    "Bonus",
                    "Freelance",
                    "Gift",
                    "Other"
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    incomeCategories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Date picker button
            OutlinedButton(
                onClick = {
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH)
                    val day = cal.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val picked = Calendar.getInstance()
                            picked.set(Calendar.YEAR, y)
                            picked.set(Calendar.MONTH, m)
                            picked.set(Calendar.DAY_OF_MONTH, d)
                            picked.set(Calendar.HOUR_OF_DAY, 12)
                            picked.set(Calendar.MINUTE, 0)
                            picked.set(Calendar.SECOND, 0)
                            picked.set(Calendar.MILLISECOND, 0)

                            dateMillis = picked.timeInMillis
                        },
                        year, month, day
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Income Date: $dateLabel")
            }

            Button(
                onClick = {
                    val numAmount = amount.toDoubleOrNull()
                    if (numAmount == null || numAmount <= 0) return@Button

                    vm.addIncome(
                        amount = numAmount,
                        category = category,
                        description = description,
                        dateMillis = dateMillis,
                        onSuccess = {
                            vm.clearStatus()
                            onBack()
                        }
                    )
                },
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.loading) "Saving..." else "Save Income")
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
