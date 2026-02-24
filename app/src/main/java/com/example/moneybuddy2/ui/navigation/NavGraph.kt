package com.example.moneybuddy2.ui.navigation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.example.moneybuddy2.ui.screens.ocr.ReceiptScanScreen
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.ui.screens.income.AddIncomeScreen


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
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack()}
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

            composable(Routes.ADD_INCOME){
                AddIncomeScreen (
                    onBack = { navController.popBackStack() }
                )
            }

            navigation(
                startDestination = Routes.RECEIPT_PICK,
                route = Routes.RECEIPT_GRAPH
            ) {
                composable(Routes.RECEIPT_SCAN ) { entry ->

                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry(Routes.RECEIPT_GRAPH)
                    }

                    ReceiptScanScreen(
                        parentEntry = parentEntry,
                        onBack = { navController.popBackStack() },
                        onGoToConfirm = { navController.navigate(Routes.RECEIPT_CONFIRM) }
                    )
                }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomBar(navController: NavHostController) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedTab = when (currentRoute) {
        Routes.HOME -> BottomNavItem.Home
        Routes.RECOMMENDATIONS -> BottomNavItem.Tools
        Routes.CHATBOT -> BottomNavItem.Chat
        Routes.RECEIPT_PICK, Routes.RECEIPT_CONFIRM, Routes.RECEIPT_GRAPH -> BottomNavItem.Receipt
        Routes.PROFILE -> BottomNavItem.Profile
        //Routes.SETTINGS -> BottomNavItem.Settings
        else -> null
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tools,
        BottomNavItem.Receipt,
        BottomNavItem.Chat,
        BottomNavItem.Profile
       // BottomNavItem.Settings
    )

    var showReceiptSheet by rememberSaveable{ mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if(showReceiptSheet){
        ModalBottomSheet(
            onDismissRequest = { showReceiptSheet = false},
            sheetState = sheetState
        ){
            ReceiptEntrySheet(
                onIncome = {
                    showReceiptSheet = false
                    navController.navigate(Routes.ADD_INCOME)
                },
                onManual = {
                    showReceiptSheet = false
                    navController.navigate(Routes.ADD_EXPENSE)
                },
                onScanCamera = {
                    showReceiptSheet = false
                    navController.navigate(Routes.RECEIPT_SCAN)
                },
                onUploadGallery = {
                    showReceiptSheet = false
                    navController.navigate(Routes.RECEIPT_PICK)
                }
            )
        }
    }

    NavigationBar {
        items.forEach { item ->
            val selected = selectedTab == item

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if(item == BottomNavItem.Receipt){
                        showReceiptSheet = true
                        return@NavigationBarItem
                    }

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

@Composable
fun ReceiptEntrySheet(
    onIncome: () -> Unit,
    onManual: () -> Unit,
    onScanCamera: () -> Unit,
    onUploadGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add expense", style = MaterialTheme.typography.titleLarge)
        Text("Choose a method", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(8.dp))

        ListItem(
            headlineContent = { Text("Add Income") },
            supportingContent = { Text("Type income amount, category, date") },
            leadingContent = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIncome() }
        )

        ListItem(
            headlineContent = { Text("Enter manually") },
            supportingContent = { Text("Type merchant, amount, category, date") },
            leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onManual() }
        )

        ListItem(
            headlineContent = { Text("Scan receipt (camera)") },
            supportingContent = { Text("Capture a receipt using the camera") },
            leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onScanCamera() }
        )

        ListItem(
            headlineContent = { Text("Upload receipt (gallery)") },
            supportingContent = { Text("Pick an image from your gallery") },
            leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUploadGallery() }
        )

        Spacer(Modifier.height(4.dp))
    }
}

