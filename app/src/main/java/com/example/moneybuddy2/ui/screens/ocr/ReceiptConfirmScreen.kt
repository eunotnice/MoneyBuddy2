package com.example.moneybuddy2.ui.screens.ocr

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import com.example.moneybuddy2.ui.theme.AppColors
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptConfirmScreen(
    parentEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val vm: OcrViewModel = viewModel(parentEntry)
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    val parsed = ui.parsed
    var didSeed    by remember { mutableStateOf(false) }
    var merchant   by remember { mutableStateOf(parsed?.merchant ?: "") }
    var amountText by remember { mutableStateOf(parsed?.amount?.toString() ?: "") }
    var category   by remember { mutableStateOf("Other") }
    var description by remember { mutableStateOf("Receipt OCR") }

    val cal = remember { Calendar.getInstance() }
    var dateMillis by remember { mutableStateOf(parsed?.dateMillis ?: System.currentTimeMillis()) }

    LaunchedEffect(parsed) {
        if (!didSeed && parsed != null) {
            merchant    = parsed.merchant
            category    = parsed.category ?: "Other"
            amountText  = parsed.amount?.toString() ?: ""
            dateMillis  = parsed.dateMillis ?: System.currentTimeMillis()
            didSeed     = true
        }
    }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    val amountValid = amountText.toDoubleOrNull()?.let { it > 0 } ?: true
    val canSave     = !ui.loading &&
            ui.rawText.isNotBlank() &&
            merchant.isNotBlank() &&
            (amountText.toDoubleOrNull()?.let { it > 0 } == true)

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Confirm Expense",
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
        ) {
            if (ui.loading) {
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth(),
                    color      = AppColors.Primary,
                    trackColor = AppColors.PrimaryLight
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── OCR success banner ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.PrimaryLight)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = AppColors.PrimaryDark)
                    Column {
                        Text(
                            "Receipt scanned successfully",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 13.sp,
                            color      = AppColors.PrimaryDark
                        )
                        Text(
                            "Review and adjust the details below before saving.",
                            fontSize = 12.sp,
                            color    = AppColors.PrimaryDark.copy(alpha = 0.7f)
                        )
                    }
                }

                // ── Error ────────────────────────────────────────────────
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
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = AppColors.Error)
                        Text(ui.error!!, color = AppColors.Error, fontSize = 13.sp)
                    }
                }

                // ── Category grid ────────────────────────────────────────
                ConfirmFormCard(title = "Category", icon = Icons.Outlined.GridView) {
                    OcrCategoryGrid(selected = category, onSelect = { category = it })
                }

                // ── Details ──────────────────────────────────────────────
                ConfirmFormCard(title = "Details", icon = Icons.Outlined.Receipt) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfirmTextField(
                            value         = merchant,
                            onValueChange = { merchant = it },
                            label         = "Merchant / Store",
                            placeholder   = "e.g. MyDin, Shell, KFC",
                            icon          = Icons.Outlined.Store
                        )
                        // Amount
                        OutlinedTextField(
                            value         = amountText,
                            onValueChange = { amountText = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Amount", fontSize = 13.sp) },
                            placeholder   = { Text("0.00", color = AppColors.TextSecondary, fontSize = 13.sp) },
                            prefix        = {
                                Text("RM ", fontWeight = FontWeight.Bold, color = AppColors.PrimaryDark, fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.AttachMoney, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape           = RoundedCornerShape(12.dp),
                            isError         = amountText.isNotBlank() && !amountValid,
                            supportingText  = if (amountText.isNotBlank() && !amountValid) {
                                { Text("Enter a valid amount", color = AppColors.Error, fontSize = 11.sp) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Divider,
                                errorBorderColor     = AppColors.Error
                            )
                        )
                        ConfirmTextField(
                            value         = description,
                            onValueChange = { description = it },
                            label         = "Notes",
                            placeholder   = "e.g. Lunch receipt",
                            icon          = Icons.Outlined.Notes,
                            minLines      = 2
                        )
                    }
                }

                // ── Date ─────────────────────────────────────────────────
                ConfirmFormCard(title = "Date", icon = Icons.Outlined.CalendarMonth) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val picked = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y); set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                        set(Calendar.HOUR_OF_DAY, 12)
                                        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
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
                        Icon(Icons.Outlined.Event, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                // Show local image preview before saving
                ui.imageUri?.let { uri ->
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    Icon(
                                        Icons.Outlined.Receipt, null,
                                        tint     = AppColors.PrimaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    "Scanned Receipt",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp,
                                    color      = AppColors.TextPrimary
                                )
                            }

                            AsyncImage(
                                model              = uri,          // ← local Uri, no upload needed
                                contentDescription = "Receipt image",
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 320.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale       = ContentScale.Fit
                            )
                        }
                    }
                }



                // ── Save button ──────────────────────────────────────────
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: return@Button
                        vm.saveConfirmedExpense(
                            context     = context,
                            merchant    = merchant,
                            amount      = amt,
                            category    = category,
                            description = description,
                            dateMillis  = dateMillis,
                            onSaved     = { vm.reset(); onSaved() }
                        )
                    },
                    enabled  = canSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = AppColors.Primary,
                        contentColor           = Color.White,
                        disabledContainerColor = AppColors.Primary.copy(alpha = 0.4f),
                        disabledContentColor   = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (ui.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Saving…", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Outlined.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Expense", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Category grid (reuses the same pattern as ManualAddExpenseScreen) ────────
private data class OcrCategoryMeta(val name: String, val emoji: String, val color: Color)

private val ocrCategories = listOf(
    OcrCategoryMeta("Food",         "🍔", Color(0xFFFF6B6B)),
    OcrCategoryMeta("Transport",    "🚗", Color(0xFF4A90E2)),
    OcrCategoryMeta("Groceries",    "🛒", Color(0xFF00C896)),
    OcrCategoryMeta("Shopping",     "🛍️", Color(0xFFFF9F43)),
    OcrCategoryMeta("Health",       "💊", Color(0xFFEE5A24)),
    OcrCategoryMeta("Entertainment","🎬", Color(0xFF9B59B6)),
    OcrCategoryMeta("Bills",        "📄", Color(0xFF2ECC71)),
    OcrCategoryMeta("Education",    "📚", Color(0xFF1ABC9C)),
    OcrCategoryMeta("Travel",       "✈️", Color(0xFF3498DB)),
    OcrCategoryMeta("Other",        "💸", Color(0xFF95A5A6)),
)

@Composable
private fun OcrCategoryGrid(selected: String, onSelect: (String) -> Unit) {
    val rows = ocrCategories.chunked(4)
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
                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ConfirmFormCard(
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
                    Icon(icon, null, tint = AppColors.PrimaryDark, modifier = Modifier.size(16.dp))
                }
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
            }
            content()
        }
    }
}

@Composable
private fun ConfirmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = AppColors.TextSecondary, fontSize = 13.sp) },
        leadingIcon   = { Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp)) },
        shape         = RoundedCornerShape(12.dp),
        minLines      = minLines,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AppColors.Primary,
            unfocusedBorderColor = AppColors.Divider
        )
    )
}

