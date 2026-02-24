package com.example.moneybuddy2.ui.screens.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.core.carbon.CarbonEstimator
import com.example.moneybuddy2.data.model.CarbonFactors
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.OcrViewModelFactory
import createReceiptImageUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    parentEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onGoToConfirm: () -> Unit
) {
    val app = (LocalContext.current.applicationContext as MoneyBuddyApp)
    val repo = app.container.repository
    val vm: OcrViewModel = viewModel(
        viewModelStoreOwner = parentEntry,
        factory = remember(repo) { OcrViewModelFactory(repo) }
    )
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current


    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            vm.setImage(uri)
            vm.runOcr(context)
        } else {
            // user cancelled; no-op or set an error if you want
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createReceiptImageUri(context)
            pendingUri = uri
            takePicture.launch(uri)
        } else {
            // You can set vm error or show snackbar
            // vm.setError("Camera permission denied")
        }
    }

    fun openCamera() {
        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCamera) {
            // fallback to gallery in ReceiptPickScreen, or show error
            return
        }

        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (granted) {
            val uri = createReceiptImageUri(context)
            pendingUri = uri
            takePicture.launch(uri)
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    // Navigate automatically when parsed is ready
    LaunchedEffect(ui.parsed) {
        if (ui.parsed != null) onGoToConfirm()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan receipt") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !ui.loading) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Button(
                onClick = { openCamera() },
                enabled = !ui.loading
            ) {
                Text(if (ui.loading) "Scanning..." else "Open camera")
            }

            if (ui.error != null) {
                Text(
                    text = ui.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Optional: debug
            // Text("Image: ${ui.imageUri}")
            // Text("Raw len: ${ui.rawText.length}")
        }
    }
}