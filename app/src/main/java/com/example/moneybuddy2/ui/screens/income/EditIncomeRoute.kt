package com.example.moneybuddy2.ui.screens.income

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.ui.viewmodel.EditIncomeLoaderViewModel
import com.example.moneybuddy2.ui.viewmodel.EditIncomeLoaderViewModelFactory

@Composable
fun EditIncomeRoute(
    incomeId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MoneyBuddyApp
    val repo = app.container.repository

    val vm: EditIncomeLoaderViewModel = viewModel(
        factory = remember(repo) { EditIncomeLoaderViewModelFactory(repo) }
    )

    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(incomeId) {
        vm.loadIncome(incomeId)
    }

    when {
        ui.loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ui.income != null -> {
            AddIncomeScreen(
                onBack = onBack,
                existingIncome = ui.income
            )
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(ui.error ?: "Income not found")
            }
        }
    }
}