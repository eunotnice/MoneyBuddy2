package com.example.moneybuddy2.ui.screens.ocr

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
import com.example.moneybuddy2.ui.viewmodel.OcrUiState
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.moneybuddy2.MoneyBuddyApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptConfirmScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MoneyBuddyApp
    val vm = remember { app.container.ocrViewModel }
    val ui by vm.ui.collectAsState()


//    val repo = remember { AppContainer().repository }
//    val vm: OcrViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return OcrViewModel(repo) as T
//            }
//        }
//    )

    // Seed fields from parsed result
    val parsed = ui.parsed
    var didSeed by remember { mutableStateOf(false) }
    var merchant by remember { mutableStateOf(parsed?.merchant ?: "") }
    var amountText by remember { mutableStateOf(parsed?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf("Other") }
    var description by remember { mutableStateOf("Receipt OCR") }

    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember {
        mutableStateOf(parsed?.dateMillis ?: System.currentTimeMillis())
    }

    // If parsed changes (first time only), refresh initial values
    LaunchedEffect(parsed) {
        if (!didSeed && parsed != null) {
            merchant = parsed.merchant
            amountText = parsed.amount?.toString() ?: ""
            dateMillis = parsed.dateMillis ?: System.currentTimeMillis()
            didSeed = true
        }
    }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm receipt expense") },
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
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (MYR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Category dropdown (reuse your default categories)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Constants.DEFAULT_CATEGORIES.forEach { item ->
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
                label = { Text("Description / Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

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
            ) { Text("Purchase Date: $dateLabel") }

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (merchant.isBlank()) return@Button
                    if (amt == null || amt <= 0.0) return@Button

                    vm.saveConfirmedExpense(
                        merchant = merchant,
                        amount = amt,
                        category = category,
                        description = description,
                        dateMillis = dateMillis,
                        onSaved = {
                            vm.reset()
                            onSaved()
                        }
                    )
                },
                enabled = !ui.loading && ui.rawText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (ui.loading) "Saving..." else "Save Expense")
            }
        }
    }
}