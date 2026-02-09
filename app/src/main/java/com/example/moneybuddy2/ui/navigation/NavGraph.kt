package com.example.moneybuddy2.ui.navigation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moneybuddy2.ui.screens.auth.LoginScreen
import com.example.moneybuddy2.ui.screens.auth.SignupScreen
import com.example.moneybuddy2.ui.screens.home.HomeScreen
import com.example.moneybuddy2.ui.screens.settings.SettingsScreen
import com.example.moneybuddy2.ui.screens.expense.ManualAddExpenseScreen
import com.example.moneybuddy2.ui.screens.profile.ProfileScreen
import com.example.moneybuddy2.ui.screens.ocr.ReceiptPickScreen
import com.example.moneybuddy2.ui.screens.ocr.ReceiptConfirmScreen
import com.example.moneybuddy2.ui.screens.chat.ChatScreen
import com.example.moneybuddy2.ui.screens.chat.RecommendationScreen
import com.example.moneybuddy2.ui.screens.chat.BotScreen
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatbotViewModel
import androidx.navigation.compose.navigation
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.screens.chat.ChatbotRoute
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneybuddy2.data.repository.FaqRepository
import com.example.moneybuddy2.ui.navigation.BottomNavItem
import com.example.moneybuddy2.ui.screens.analytics.AnalyticsScreen
import com.example.moneybuddy2.ui.viewmodel.AnalyticsViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatbotViewModelFactory
import com.example.moneybuddy2.ui.viewmodel.HomeViewModel


@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    Scaffold(
        bottomBar = {
            AppBottomBar(navController)
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.LOGIN){
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onGoToSignup = {
                        navController.navigate(Routes.SIGNUP)
                    }
                )
            }

            composable(Routes.SIGNUP){
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onGoToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.HOME) { entry -> // Use 'entry' to scope the ViewModel
                // Create the ViewModel factory
                val context = LocalContext.current
                val app = context.applicationContext as MoneyBuddyApp
                val factory = remember {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return HomeViewModel(app.container.repository) as T
                        }
                    }
                }

                // Instantiate the ViewModel
                val vm: HomeViewModel = viewModel(
                    viewModelStoreOwner = entry,
                    factory = factory
                )

                // Pass the ViewModel to HomeScreen
                HomeScreen(
                    vm = vm, // <-- The fix is here
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onAddExpense = {
                        navController.navigate(Routes.ADD_EXPENSE)
                    },
                    onOpenProfile = {
                        navController.navigate(Routes.PROFILE)
                    },
                    onAddReceipt = {
                        Log.d(TAG, "onAddReceipt clicked")
                        navController.navigate(Routes.RECEIPT_PICK)
                    },
                    onOpenChat = { navController.navigate(Routes.CHAT) },
                    onOpenRecommendations = { navController.navigate(Routes.RECOMMENDATIONS) },
                    onOpenBot = { navController.navigate(Routes.CHATBOT) },
                    onOpenAnalytics = { navController.navigate(Routes.ANALYTICS) }
                )
            }


            composable(Routes.PROFILE){
                ProfileScreen(
                    onDone = {
                        navController.navigate(Routes.HOME){
                            popUpTo(Routes.PROFILE) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack()}
                )
            }

            composable(Routes.SETTINGS){
                SettingsScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.CHATBOT) { entry ->
                val context = LocalContext.current
                val app = context.applicationContext as MoneyBuddyApp

                val factory = remember {
                    ChatbotViewModelFactory(
                        appContext = app.applicationContext,
                        container = app.container
                    )
                }

                val vm: ChatbotViewModel = viewModel(
                    viewModelStoreOwner = entry,
                    factory = factory
                )

                BotScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    whatsappPhoneE164 = "601127275319"
                )
            }

            composable(Routes.RECOMMENDATIONS) {
                val context = LocalContext.current
                val app = context.applicationContext as MoneyBuddyApp
                val vm = app.container.recommendationViewModel

                RecommendationScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_EXPENSE) {
                ManualAddExpenseScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            navigation(
                startDestination = Routes.RECEIPT_PICK,
                route = Routes.RECEIPT_GRAPH
            ) {

                composable(Routes.RECEIPT_PICK) { entry ->

                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry(Routes.RECEIPT_GRAPH)
                    }

                    ReceiptPickScreen(
                        parentEntry = parentEntry,
                        onBack = { navController.popBackStack() },
                        onGoToConfirm = { navController.navigate(Routes.RECEIPT_CONFIRM) }
                    )
                }

                composable(Routes.RECEIPT_CONFIRM) { entry ->

                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry(Routes.RECEIPT_GRAPH)
                    }

                    ReceiptConfirmScreen(
                        parentEntry = parentEntry,
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.RECEIPT_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Routes.ANALYTICS) { entry ->
                val context = LocalContext.current
                val app = context.applicationContext as MoneyBuddyApp

                val factory = remember {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AnalyticsViewModel(app.container.repository) as T
                        }
                    }
                }

                val vm: AnalyticsViewModel = viewModel(
                    viewModelStoreOwner = entry,
                    factory = factory
                )

                AnalyticsScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() }
                )
            }

        }
    }
}

@Composable
fun AppBottomBar(navController: NavHostController) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedTab = when (currentRoute) {
        Routes.HOME -> BottomNavItem.Home
        Routes.CHATBOT -> BottomNavItem.Chat
        Routes.RECEIPT_PICK, Routes.RECEIPT_CONFIRM -> BottomNavItem.Receipt
        Routes.PROFILE -> BottomNavItem.Profile
        Routes.SETTINGS -> BottomNavItem.Settings
        else -> null
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Receipt,
        BottomNavItem.Chat,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )

    NavigationBar {
        items.forEach { item ->
            val selected = selectedTab == item

            NavigationBarItem(
                selected = selected,
                onClick = {
                    Log.d("BottomNav", "Navigate to: ${item.route}")

                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        item.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}



