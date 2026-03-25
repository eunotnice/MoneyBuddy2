package com.example.moneybuddy2.ui.screens.ocr

import android.util.Log
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.OcrUiState
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.core.carbon.CarbonEstimator
import com.example.moneybuddy2.core.util.DateUtils
import com.example.moneybuddy2.core.util.DateUtils.formatDate
import com.example.moneybuddy2.data.model.CarbonFactors
import com.example.moneybuddy2.ui.navigation.Routes
import com.example.moneybuddy2.ui.viewmodel.OcrViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPickScreen(
    parentEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onGoToConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val app = (context.applicationContext as MoneyBuddyApp)
    val repo = app.container.repository

    val vm: OcrViewModel = viewModel(
        viewModelStoreOwner = parentEntry,
        factory = remember(repo) { OcrViewModelFactory(repo) }
    )

    val ui by vm.ui.collectAsStateWithLifecycle()

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d("ReceiptDebug", "Picker returned uri = $uri")
        vm.setImage(uri)
    }

    // Automatically move to confirm screen once parsing is ready
    LaunchedEffect(ui.parsed) {
        if (ui.parsed != null) {
            onGoToConfirm()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt OCR") },
                navigationIcon = {
                    TextButton(onClick = onBack, enabled = !ui.loading) {
                        Text("Back")
                    }
                }
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
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick receipt image")
            }

            if (ui.imageUri != null) {
                Card {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Selected image", style = MaterialTheme.typography.titleMedium)
                        AsyncImage(
                            model = ui.imageUri,
                            contentDescription = "Receipt image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    }
                }
            }

            Button(
                onClick = { vm.runOcr(context) },
                enabled = ui.imageUri != null && !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (ui.loading) "Running OCR..." else "Run OCR")
            }

            if (ui.error != null) {
                Text(
                    text = ui.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (ui.loading) {
                Text("Processing receipt...")
            }
        }
    }
}