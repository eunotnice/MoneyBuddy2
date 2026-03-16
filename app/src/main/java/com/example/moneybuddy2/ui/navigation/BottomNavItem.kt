package com.example.moneybuddy2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Tools: BottomNavItem("recommendations", Icons.Default.Dashboard, "Tools")
    object Chat : BottomNavItem("chat", Icons.Default.Chat, "Chat")
    object Receipt : BottomNavItem("receipt_graph", Icons.Default.AddBox, "Receipt")
    object Profile : BottomNavItem("profile", Icons.Default.Settings, "Settings")
    //object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}
