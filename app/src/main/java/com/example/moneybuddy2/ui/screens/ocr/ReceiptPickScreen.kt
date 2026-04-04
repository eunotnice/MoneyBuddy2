package com.example.moneybuddy2.ui.screens.ocr

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.core.copy
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.ui.theme.AppColors
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.OcrViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPickScreen(
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

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d("ReceiptDebug", "Picker returned uri = $uri")
        vm.setImage(uri)
    }

    LaunchedEffect(ui.parsed) {
        if (ui.parsed != null) onGoToConfirm()
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Upload Receipt", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (ui.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.Primary, trackColor = AppColors.PrimaryLight)
            }

            // ── Error ────────────────────────────────────────────────────
            if (ui.error != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.ErrorLight)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = AppColors.Error)
                    Text(ui.error!!, color = AppColors.Error, fontSize = 13.sp)
                }
            }

            // ── Image preview or empty state ─────────────────────────────
            if (ui.imageUri != null) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Image, null, tint = AppColors.PrimaryDark, modifier = Modifier.size(16.dp))
                            }
                            Text("Selected receipt", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        }
                        AsyncImage(
                            model              = ui.imageUri,
                            contentDescription = "Receipt image",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            } else {
                // Empty drop zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AppColors.PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Image, null, tint = AppColors.Primary, modifier = Modifier.size(30.dp))
                        }
                        Text("No image selected", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.TextPrimary)
                        Text("Tap the button below to pick from gallery", fontSize = 13.sp, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Action buttons ───────────────────────────────────────────
            Button(
                onClick  = { pickImage.launch("image/*") },
                enabled  = !ui.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (ui.imageUri == null) AppColors.Primary else Color(0xFF4A90E2),
                    contentColor   = Color.White
                )
            ) {
                Icon(Icons.Outlined.PhotoLibrary, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (ui.imageUri == null) "Pick from Gallery" else "Choose Different Image",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp
                )
            }

            if (ui.imageUri != null && !ui.loading) {
                Button(
                    onClick  = { vm.runOcr(context) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.DocumentScanner, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Receipt", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            if (ui.loading) {
                OcrProcessingBanner()
            }
        }
    }
}

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