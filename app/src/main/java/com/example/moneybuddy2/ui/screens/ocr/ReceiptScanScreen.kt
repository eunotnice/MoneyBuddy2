package com.example.moneybuddy2.ui.screens.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.OcrViewModelFactory
import createReceiptImageUri
import androidx.compose.ui.platform.LocalContext
import com.example.moneybuddy2.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    parentEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onGoToConfirm: () -> Unit
) {
    val context = LocalContext.current
    val app     = context.applicationContext as MoneyBuddyApp
    val repo    = app.container.repository

    val vm: OcrViewModel = viewModel(
        viewModelStoreOwner = parentEntry,
        factory             = remember(repo) { OcrViewModelFactory(repo) }
    )
    val ui by vm.ui.collectAsStateWithLifecycle()

    var pendingUri      by remember { mutableStateOf<Uri?>(null) }
    var launchAttempted by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) { vm.setImage(uri); vm.runOcr(context) }
        else onBack()
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createReceiptImageUri(context); pendingUri = uri; takePicture.launch(uri)
        } else onBack()
    }

    fun openCamera() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { onBack(); return }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createReceiptImageUri(context); pendingUri = uri; takePicture.launch(uri)
        } else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) { if (!launchAttempted) { launchAttempted = true; openCamera() } }
    LaunchedEffect(ui.parsed) { if (ui.parsed != null) onGoToConfirm() }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Scan Receipt", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !ui.loading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                ui.loading -> {
                    // ── Processing state ─────────────────────────────────
                    OcrProcessingBanner()
                }
                ui.error != null -> {
                    // ── Error state ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AppColors.ErrorLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = AppColors.Error, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Scan failed", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        ui.error!!,
                        fontSize   = 14.sp,
                        color      = AppColors.TextSecondary,
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick  = { openCamera() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Primary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Try Again", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
                else -> {
                    // ── Opening camera state ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = AppColors.Primary, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Opening camera…", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Point your camera at a receipt and take a photo.",
                        fontSize   = 14.sp,
                        color      = AppColors.TextSecondary,
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// ─── Shared OCR processing banner ────────────────────────────────────────────
@Composable
private fun OcrProcessingBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue   = 0.4f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label          = "alpha"
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.DocumentScanner,
                null,
                tint     = AppColors.Primary.copy(alpha = alpha),
                modifier = Modifier.size(34.dp)
            )
        }
        Text("Reading your receipt…", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.TextPrimary)
        Text("This usually takes a few seconds.", fontSize = 13.sp, color = AppColors.TextSecondary)
        LinearProgressIndicator(
            modifier   = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
            color      = AppColors.Primary,
            trackColor = AppColors.PrimaryLight
        )
    }
}