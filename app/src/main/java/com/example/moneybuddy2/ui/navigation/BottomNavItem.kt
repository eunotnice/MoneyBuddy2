package com.example.moneybuddy2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Icons.Default.Home, "Home")
    object Tools: BottomNavItem(Icons.Default.Dashboard, "Tools")
    object Chat : BottomNavItem(Icons.Default.Chat, "Chat")
    object Receipt : BottomNavItem(Icons.Default.AddBox, "Receipt")
    object Game : BottomNavItem(Icons.Default.SportsEsports, "Game")
    //object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}
