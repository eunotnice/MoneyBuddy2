package com.example.moneybuddy2.ui.screens.income

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.IncomeUiState
import com.example.moneybuddy2.ui.viewmodel.IncomeViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─── Colour tokens (same as the rest of the app) ─────────────────────────────
private val GreenMint     = Color(0xFF00C896)
private val GreenDark     = Color(0xFF009E78)
private val GreenLight    = Color(0xFFE6FBF5)
private val SurfaceGray   = Color(0xFFF7F8FA)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val RedSoft       = Color(0xFFE53935)
private val RedLight      = Color(0xFFFFECEC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit,
    existingIncome: Income? = null
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

    val isEditMode = existingIncome != null

    var amountText by remember(existingIncome?.id) {
        mutableStateOf(
            existingIncome?.amount?.takeIf { it > 0.0 }?.toString() ?: ""
        )
    }
    var category by remember(existingIncome?.id) {
        mutableStateOf(existingIncome?.category ?: "Salary")
    }
    var description by remember(existingIncome?.id) {
        mutableStateOf(existingIncome?.description ?: "")
    }

    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember(existingIncome?.id) {
        mutableStateOf(existingIncome?.dateMillis ?: cal.timeInMillis)
    }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    val amountValid = amountText.toDoubleOrNull()?.let { it > 0 } ?: true
    val canSave = (amountText.toDoubleOrNull()?.let { it > 0 } == true) && !uiState.loading

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Income" else "Add Income")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
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

                // ── Category quick-select grid ────────────────────────────
                FormCard {
                    FormSectionLabel(icon = Icons.Outlined.GridView, label = "Source")
                    IncomeCategoryGrid(selected = category, onSelect = { category = it })
                }

                // ── Amount & Notes ───────────────────────────────────────
                FormCard {
                    FormSectionLabel(icon = Icons.Outlined.Payments, label = "Details")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Amount with RM prefix
                        OutlinedTextField(
                            value         = amountText,
                            onValueChange = { amountText = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Amount", fontSize = 13.sp) },
                            placeholder   = { Text("0.00", color = TextSecondary, fontSize = 13.sp) },
                            prefix        = {
                                Text(
                                    "RM ",
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDark,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = GreenMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape           = RoundedCornerShape(12.dp),
                            isError         = amountText.isNotBlank() && !amountValid,
                            supportingText  = if (amountText.isNotBlank() && !amountValid) {
                                { Text("Enter a valid amount", color = RedSoft, fontSize = 11.sp) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = GreenMint,
                                unfocusedBorderColor = DividerColor,
                                errorBorderColor     = RedSoft
                            )
                        )

                        StyledTextField(
                            value         = description,
                            onValueChange = { description = it },
                            label         = "Notes (optional)",
                            placeholder   = "e.g. March salary, project bonus",
                            icon          = Icons.Outlined.Notes,
                            minLines      = 2
                        )
                    }
                }

                // ── Date picker ──────────────────────────────────────────
                FormCard {
                    FormSectionLabel(icon = Icons.Outlined.CalendarMonth, label = "Date")
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val picked = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                        set(Calendar.HOUR_OF_DAY, 12)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    dateMillis = picked.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, GreenMint)
                    ) {
                        Icon(
                            Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(dateLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                // ── Error banner ─────────────────────────────────────────
                if (uiState.error != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedLight)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = RedSoft)
                        Text(uiState.error!!, color = RedSoft, fontSize = 13.sp)
                    }
                }

                // ── Save button ──────────────────────────────────────────
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: return@Button

                        if (isEditMode) {
                            vm.updateIncome(
                                existingIncome = existingIncome!!,
                                amount = amount,
                                category = category,
                                description = description,
                                dateMillis = dateMillis,
                                onSuccess = {
                                    vm.clearStatus()
                                    onBack()
                                }
                            )
                        } else {
                            vm.addIncome(
                                amount = amount,
                                category = category,
                                description = description,
                                dateMillis = dateMillis,
                                onSuccess = {
                                    vm.clearStatus()
                                    onBack()
                                }
                            )
                        }
                    },
                    enabled  = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = GreenMint,
                        contentColor           = Color.White,
                        disabledContainerColor = GreenMint.copy(alpha = 0.4f),
                        disabledContentColor   = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (uiState.loading) {
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
                        Text("Save Income", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            if (isEditMode) {
                OutlinedButton(
                    onClick = {
                        vm.deleteIncome(existingIncome!!.id) {
                            vm.clearStatus()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Income")
                }
            }
        }
    }
}

// ─── Income category grid ─────────────────────────────────────────────────────
private data class IncomeMeta(val name: String, val emoji: String, val color: Color)

private val incomeMetas = listOf(
    IncomeMeta("Salary",    "💼", Color(0xFF5A2D82)),
    IncomeMeta("Bonus",     "🎉", Color(0xFFFFB300)),
    IncomeMeta("Freelance", "💻", Color(0xFF4A90E2)),
    IncomeMeta("Gift",      "🎁", Color(0xFFFF6B6B)),
    IncomeMeta("Interest",  "📈", Color(0xFF00C896)),
    IncomeMeta("Other",     "💰", Color(0xFF95A5A6)),
)

@Composable
private fun IncomeCategoryGrid(selected: String, onSelect: (String) -> Unit) {
    val rows = incomeMetas.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { meta ->
                    val isSelected = selected == meta.name
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) meta.color.copy(alpha = 0.15f) else CardWhite)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) meta.color else DividerColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(meta.name) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(meta.emoji, fontSize = 22.sp)
                        Text(
                            meta.name,
                            fontSize   = 11.sp,
                            color      = if (isSelected) meta.color else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines   = 1
                        )
                    }
                }
                // Fill remaining columns
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Shared small composables ─────────────────────────────────────────────────
@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun FormSectionLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GreenDark, modifier = Modifier.size(16.dp))
        }
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isError: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon   = {
            Icon(icon, contentDescription = null, tint = GreenMint, modifier = Modifier.size(20.dp))
        },
        shape    = RoundedCornerShape(12.dp),
        isError  = isError,
        minLines = minLines,
        colors   = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GreenMint,
            unfocusedBorderColor = DividerColor,
            errorBorderColor     = RedSoft
        )
    )
}