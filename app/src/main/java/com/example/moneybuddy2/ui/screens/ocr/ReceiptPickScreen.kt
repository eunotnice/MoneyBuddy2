package com.example.moneybuddy2.ui.screens.ocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.OcrUiState
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.core.util.DateUtils
import com.example.moneybuddy2.core.util.DateUtils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPickScreen(
    onBack: () -> Unit,
    onGoToConfirm: () -> Unit
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

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        vm.setImage(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt OCR") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { pickImage.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Pick receipt image") }

            if (ui.imageUri != null) {
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selected image", style = MaterialTheme.typography.titleMedium)
                        AsyncImage(
                            model = ui.imageUri,
                            contentDescription = "Receipt image",
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                    }
                }
            }

            Button(
                onClick = { vm.runOcr(context) },
                enabled = ui.imageUri != null && !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (ui.loading) "Running OCR..." else "Run OCR") }

            if (ui.error != null) {
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            }

            if (ui.rawText.isNotBlank()) {
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Extracted text", style = MaterialTheme.typography.titleMedium)
                        Text(ui.rawText, style = MaterialTheme.typography.bodySmall)
                    }
                }

                val parsed = ui.parsed
                if (parsed != null) {
                    Card {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Parsed draft (rule-based)", style = MaterialTheme.typography.titleMedium)
                            Text("Merchant: ${parsed.merchant.ifBlank { "(not found)" }}")
                            Text("Amount: ${parsed.amount?.toString() ?: "(not found)"}")
                            Text(
                                "Date: " + (parsed.dateMillis?.let { formatDate(it) } ?: "(not found)")
                            )

                        }
                    }
                }

                Button(
                    onClick = onGoToConfirm,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Next: Confirm & Save") }
            }
        }
    }
}