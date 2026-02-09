package com.example.moneybuddy2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Chat : BottomNavItem("chatbot", Icons.Default.Chat, "Chat")
    object Receipt : BottomNavItem("receipt_graph", Icons.Default.Receipt, "Receipt")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}
