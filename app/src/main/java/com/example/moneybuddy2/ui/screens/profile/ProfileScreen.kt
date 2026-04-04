package com.example.moneybuddy2.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.theme.AppColors
import com.example.moneybuddy2.ui.viewmodel.ProfileUiState
import com.example.moneybuddy2.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
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
    val incomeText = ""
    val budgetText = ""
    val goalText   = ""
    var currency by remember { mutableStateOf("MYR") }

    LaunchedEffect(ui.profile) {
        val p = ui.profile ?: return@LaunchedEffect
        name     = p.displayName
        currency = p.currency.ifBlank { "MYR" }
    }

    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val initials  = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifBlank { "?" }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile & Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = AppColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Loading / Error ──────────────────────────────────────
                if (ui.loading) {
                    LinearProgressIndicator(
                        modifier   = Modifier.fillMaxWidth(),
                        color      = AppColors.Primary,
                        trackColor = AppColors.PrimaryLight
                    )
                }
                if (ui.error != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.ErrorLight)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = AppColors.Error)
                        Text(ui.error!!, color = AppColors.Error, fontSize = 13.sp)
                    }
                }

                // ── Avatar hero ──────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Initials circle
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(AppColors.Primary, AppColors.PrimaryDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials,
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                        if (name.isNotBlank()) {
                            Text(
                                name,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = AppColors.TextPrimary
                            )
                        }
                        if (userEmail.isNotBlank()) {
                            Text(userEmail, fontSize = 13.sp, color = AppColors.TextSecondary)
                        }
                    }
                }

                // ── Name field ───────────────────────────────────────────
                ProfileFormCard(title = "Account", icon = Icons.Outlined.Person) {
                    StyledTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = "Display name",
                        placeholder   = "Enter your name",
                        icon          = Icons.Default.Person,
                        isError       = name.isBlank() && ui.error != null
                    )
                }

                // ── Save button ──────────────────────────────────────────
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        vm.saveProfile(
                            displayName   = name,
                            monthlyIncome = 0.0,
                            monthlyBudget = 0.0,
                            savingGoal    = 0.0,
                            currency      = currency.ifBlank { "MYR" },
                            onDone        = onDone
                        )
                    },
                    enabled  = !ui.loading && name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = AppColors.Primary,
                        contentColor           = Color.White,
                        disabledContainerColor = AppColors.Primary.copy(alpha = 0.4f),
                        disabledContentColor   = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (ui.loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Saving…", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Profile", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                // ── Danger zone ──────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.ErrorLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint     = AppColors.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                "Account Actions",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = AppColors.TextPrimary
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick  = {
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error)
                        ) {
                            Icon(
                                Icons.Outlined.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Shared composables ───────────────────────────────────────────────────────
@Composable
private fun ProfileFormCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    Icon(icon, contentDescription = null, tint = AppColors.PrimaryDark, modifier = Modifier.size(16.dp))
                }
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
            }
            content()
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isError: Boolean = false
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = AppColors.TextSecondary, fontSize = 13.sp) },
        leadingIcon   = {
            Icon(icon, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
        },
        shape   = RoundedCornerShape(12.dp),
        isError = isError,
        colors  = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AppColors.Primary,
            unfocusedBorderColor = AppColors.Divider,
            errorBorderColor     = AppColors.Error
        )
    )
}