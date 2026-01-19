package com.example.moneybuddy2.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Home (Dashboard placeholder)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Button(onClick = onAddExpense, modifier = Modifier.fillMaxWidth()) {
            Text("Add Expense (Manual)")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Open Settings")
        }
    }
}
