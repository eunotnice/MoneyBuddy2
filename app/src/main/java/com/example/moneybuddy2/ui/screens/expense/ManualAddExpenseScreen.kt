package com.example.moneybuddy2.ui.screens.expense

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
import com.example.moneybuddy2.core.Constants
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.theme.AppColors
import com.example.moneybuddy2.ui.viewmodel.ExpenseUiState
import com.example.moneybuddy2.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddExpenseScreen(
    onBack: () -> Unit,
    existingExpense: Expense? = null
) {

    val repo = remember { AppContainer().repository }
    val vm: ExpenseViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(repo) as T
            }
        }
    )

    val uiState by vm.uiState.collectAsState(initial = ExpenseUiState())

    val isEditMode = existingExpense != null

    var merchant by remember(existingExpense?.id) {
        mutableStateOf(existingExpense?.merchant ?: "")
    }
    var amountText by remember(existingExpense?.id) {
        mutableStateOf(
            existingExpense?.amount?.takeIf { it > 0.0 }?.toString() ?: ""
        )
    }
    var category by remember(existingExpense?.id) {
        mutableStateOf(existingExpense?.category ?: "Food")
    }
    var description by remember(existingExpense?.id) {
        mutableStateOf(existingExpense?.description ?: "")
    }

    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember(existingExpense?.id) {
        mutableStateOf(existingExpense?.dateMillis ?: cal.timeInMillis)
    }
    val dateLabel  = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    // Validation state
    val amountValid  = amountText.toDoubleOrNull()?.let { it > 0 } ?: true
    val canSave      = merchant.isNotBlank() &&
            (amountText.toDoubleOrNull()?.let { it > 0 } == true) &&
            !uiState.loading

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Expense" else "Add Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.TextPrimary
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

                // ── Category quick-select grid ────────────────────────────
                FormCard {
                    FormSectionLabel(
                        icon  = Icons.Outlined.GridView,
                        label = "Category"
                    )
                    CategoryGrid(
                        selected  = category,
                        onSelect  = { category = it }
                    )
                }

                // ── Merchant & Amount ────────────────────────────────────
                FormCard {
                    FormSectionLabel(icon = Icons.Outlined.Receipt, label = "Details")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value         = merchant,
                            onValueChange = { merchant = it },
                            label         = "Merchant / Store",
                            placeholder   = "e.g. MyDin, Shell, KFC",
                            icon          = Icons.Outlined.Store,
                            isError       = merchant.isEmpty() && uiState.error != null
                        )

                        // Amount field with RM prefix
                        OutlinedTextField(
                            value         = amountText,
                            onValueChange = { amountText = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Amount", fontSize = 13.sp) },
                            placeholder   = { Text("0.00", color = AppColors.TextSecondary, fontSize = 13.sp) },
                            prefix        = {
                                Text(
                                    "RM ",
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon   = {
                                Icon(
                                    Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape   = RoundedCornerShape(12.dp),
                            isError = amountText.isNotBlank() && !amountValid,
                            supportingText = if (amountText.isNotBlank() && !amountValid) {
                                { Text("Enter a valid amount", color = AppColors.Error, fontSize = 11.sp) }
                            } else null,
                            colors  = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Divider,
                                errorBorderColor     = AppColors.Error
                            )
                        )

                        StyledTextField(
                            value         = description,
                            onValueChange = { description = it },
                            label         = "Notes (optional)",
                            placeholder   = "e.g. Lunch with team",
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
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.PrimaryDark),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary)
                    ) {
                        Icon(
                            Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            dateLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // ── Error banner ─────────────────────────────────────────
                if (uiState.error != null) {
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
                        Text(uiState.error!!, color = AppColors.Error, fontSize = 13.sp)
                    }
                }

                // ── Save button ──────────────────────────────────────────
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: return@Button

                        if (isEditMode) {
                            vm.updateManualExpense(
                                existingExpense = existingExpense!!,
                                merchant = merchant,
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
                            vm.addManualExpense(
                                merchant = merchant,
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
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = Color.White,
                        disabledContainerColor = AppColors.Primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (isEditMode) "Saving changes…" else "Saving…",
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEditMode) "Save Changes" else "Save Expense",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (isEditMode) {
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                    ) {
                        Text("Delete Expense", fontWeight = FontWeight.SemiBold)
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete expense?") },
                            text = { Text("This action cannot be undone.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                        vm.deleteExpense(existingExpense!!.id) {
                                            vm.clearStatus()
                                            onBack()
                                        }
                                    }
                                ) {
                                    Text("Delete", color = AppColors.Error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Category quick-pick grid ─────────────────────────────────────────────────
private data class CategoryMeta(val name: String, val emoji: String, val color: Color)

private val categoryMetas = listOf(
    CategoryMeta("Food",         "🍔", Color(0xFFFF6B6B)),
    CategoryMeta("Transport",    "🚗", Color(0xFF4A90E2)),
    CategoryMeta("Groceries",    "🛒", Color(0xFF00C896)),
    CategoryMeta("Shopping",     "🛍️", Color(0xFFFF9F43)),
    CategoryMeta("Health",       "💊", Color(0xFFEE5A24)),
    CategoryMeta("Entertainment","🎬", Color(0xFF9B59B6)),
    CategoryMeta("Bills",        "📄", Color(0xFF2ECC71)),
    CategoryMeta("Education",    "📚", Color(0xFF1ABC9C)),
    CategoryMeta("Travel",       "✈️", Color(0xFF3498DB)),
    CategoryMeta("Other",        "💸", Color(0xFF95A5A6)),
)

@Composable
private fun CategoryGrid(selected: String, onSelect: (String) -> Unit) {
    // Pull Constants.DEFAULT_CATEGORIES but fall back to our meta list
    val rows = categoryMetas.chunked(4)
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
                            .background(if (isSelected) meta.color.copy(alpha = 0.15f) else AppColors.Surface)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) meta.color else AppColors.Divider,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(meta.name) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(meta.emoji, fontSize = 20.sp)
                        Text(
                            meta.name,
                            fontSize   = 9.sp,
                            color      = if (isSelected) meta.color else AppColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines   = 1
                        )
                    }
                }
                // Fill remaining slots in last row
                repeat(4 - row.size) {
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
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
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
                .background(AppColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.PrimaryDark, modifier = Modifier.size(16.dp))
        }
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
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
        placeholder   = { Text(placeholder, color = AppColors.TextSecondary, fontSize = 13.sp) },
        leadingIcon   = {
            Icon(icon, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
        },
        shape    = RoundedCornerShape(12.dp),
        isError  = isError,
        minLines = minLines,
        colors   = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AppColors.Primary,
            unfocusedBorderColor = AppColors.Divider,
            errorBorderColor     = AppColors.Error
        )
    )
}