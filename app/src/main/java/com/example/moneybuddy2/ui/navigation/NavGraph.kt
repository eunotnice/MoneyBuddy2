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
import com.example.moneybuddy2.ui.screens.expense.ManualAddExpenseScreen
import com.example.moneybuddy2.ui.screens.profile.ProfileScreen
import com.example.moneybuddy2.ui.screens.ocr.ReceiptScanScreen
import com.example.moneybuddy2.ui.screens.ocr.ReceiptPickScreen
import com.example.moneybuddy2.ui.screens.ocr.ReceiptConfirmScreen
import com.example.moneybuddy2.ui.screens.chat.ChatScreen
import com.example.moneybuddy2.ui.screens.chat.BotScreen
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
import com.example.moneybuddy2.game.screens.ResultScreen
import com.example.moneybuddy2.game.screens.SimulatorScreen
import com.example.moneybuddy2.game.SimulatorViewModel
import com.example.moneybuddy2.ui.screens.chat.AiRecommendationScreen
import com.example.moneybuddy2.ui.screens.expense.EditExpenseRoute
import com.example.moneybuddy2.ui.screens.income.AddIncomeScreen
import com.example.moneybuddy2.ui.screens.income.EditIncomeRoute
import com.example.moneybuddy2.ui.viewmodel.AiRecommendationViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatViewModelFactory
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.OcrViewModelFactory
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.game.screens.IntroScreen
import com.example.moneybuddy2.ui.theme.AppColors

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
                    vm = vm,
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onAddExpense = {
                        navController.navigate(Routes.ADD_EXPENSE)
                    },
                    onOpenProfile = {
                        navController.navigate(Routes.PROFILE){
                            launchSingleTop = true
                        }
                    },
                    onAddReceipt = {
                        Log.d(TAG, "onAddReceipt clicked")
                        navController.navigate(Routes.RECEIPT_PICK)
                    },
                    onOpenChat = { navController.navigate(Routes.CHAT) },
                    onOpenRecommendations = { navController.navigate(Routes.RECOMMENDATIONS) },
                    onOpenBot = { navController.navigate(Routes.CHATBOT) },
                    onOpenAnalytics = { navController.navigate(Routes.ANALYTICS) },
                    onOpenGame = { navController.navigate(Routes.GAME) },
                    onEditExpense = { id ->
                        navController.navigate(Routes.editExpense(id))
                    },
                    onEditIncome = { id ->
                        navController.navigate(Routes.editIncome(id))
                    }

                )
            }



            navigation(
                startDestination = Routes.GAME_INTRO,
                route = Routes.GAME_GRAPH
            ) {
                composable(Routes.GAME_INTRO) {
                    IntroScreen(
                        onStart = { navController.navigate(Routes.GAME) }
                    )
                }

                composable(Routes.GAME) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Routes.GAME_GRAPH)
                    }
                    val gameViewModel: SimulatorViewModel = viewModel(parentEntry)

                    SimulatorScreen(
                        viewModel = gameViewModel,
                        onSimulate = {
                            navController.navigate(Routes.RESULT)
                        }
                    )
                }

                composable(Routes.RESULT) { backStackEntry ->

                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Routes.GAME_GRAPH)
                    }

                    val gameViewModel: SimulatorViewModel = viewModel(parentEntry)

                    ResultScreen(
                        viewModel = gameViewModel,
                        onReset = {
                            navController.popBackStack()
                        }
                    )
                }
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
                    onBack = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PROFILE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.CHAT) {
                val vm: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory()
                )

                ChatScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() }
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


//            composable(Routes.RECOMMENDATIONS) {
//                val context = LocalContext.current
//                val app = context.applicationContext as MoneyBuddyApp
//                val vm = app.container.recommendationViewModel
//
//                RecommendationScreen(
//                    vm = vm,
//                    onBack = { navController.popBackStack() }
//                )
//            }

            composable(Routes.RECOMMENDATIONS) {
                val vm: AiRecommendationViewModel = viewModel(
                    factory = RecommendationViewModelFactory()
                )

                AiRecommendationScreen(
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

            composable(Routes.EDIT_EXPENSE_ROUTE) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()

                EditExpenseRoute(
                    expenseId = expenseId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.EDIT_INCOME_ROUTE) { backStackEntry ->
                val incomeId = backStackEntry.arguments?.getString("incomeId").orEmpty()

                EditIncomeRoute(
                    incomeId = incomeId,
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

                    val context = LocalContext.current
                    val app = context.applicationContext as MoneyBuddyApp
                    val repo = app.container.repository

                    val vm: OcrViewModel = viewModel(
                        viewModelStoreOwner = parentEntry,
                        factory = remember(repo) { OcrViewModelFactory(repo) }
                    )

                    ReceiptConfirmScreen(
                        parentEntry = parentEntry,
                        onBack = {
                            vm.clearParsedResult()
                            navController.popBackStack()
                        },
                        onSaved = {
                            vm.reset()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.RECEIPT_GRAPH) { inclusive = true }
                                launchSingleTop = true
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
    val noBottomBarScreens = listOf("login", "signup")


    val selectedTab = when {
        currentRoute == Routes.HOME -> BottomNavItem.Home
        currentRoute == Routes.RECOMMENDATIONS -> BottomNavItem.Tools
        currentRoute == Routes.CHAT -> BottomNavItem.Chat
        currentRoute in setOf(Routes.RECEIPT_PICK, Routes.RECEIPT_CONFIRM, Routes.RECEIPT_GRAPH) -> BottomNavItem.Receipt
        currentRoute in setOf(Routes.GAME_GRAPH, Routes.GAME_INTRO, Routes.GAME, Routes.RESULT) -> BottomNavItem.Game
        else -> null
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tools,
        BottomNavItem.Receipt,
        BottomNavItem.Chat,
        BottomNavItem.Game
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


    if(currentRoute !in noBottomBarScreens){
        NavigationBar (
            containerColor = AppColors.Surface
        ){
            items.forEach { item ->
                val selected = selectedTab == item

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (item == BottomNavItem.Receipt) {
                            showReceiptSheet = true
                            return@NavigationBarItem
                        }

                        when (item) {
                            BottomNavItem.Game -> {
                                navController.navigate(Routes.GAME_GRAPH) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = false        // always restart game from intro
                                }
                            }
                            BottomNavItem.Home -> {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                            BottomNavItem.Tools -> {
                                navController.navigate(Routes.RECOMMENDATIONS) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true         // keep state
                                }
                            }
                            BottomNavItem.Chat -> {
                                navController.navigate(Routes.CHAT) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true         // keep state
                                }
                            }
                            else -> {}
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // This ensures the "pill" behind the icon is also correct
                        indicatorColor = Color.Transparent,
                        selectedIconColor = AppColors.Primary,
                        unselectedIconColor = AppColors.TextSecondary,
                        selectedTextColor = AppColors.Primary,
                        unselectedTextColor = AppColors.TextSecondary
                    ),
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
}


// ─── Colour tokens ──────────────────────────────────────────────────────────
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
            .background(AppColors.Surface) // Ensure the sheet itself is pure white
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Drag handle ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp, bottom = 20.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.Divider)
        )

        // ── Header ───────────────────────────────────────────────────────
        Text(
            "Add Transaction",
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            color      = AppColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose how you'd like to add",
            fontSize = 14.sp,
            color    = AppColors.TextSecondary
        )

        Spacer(Modifier.height(20.dp))

        // ── Action tiles ─────────────────────────────────────────────────

        // Income: Using the brand Yellow (#FFC915)
        EntryOption(
            icon        = Icons.Outlined.TrendingUp,
            iconBg      = Color(0xFFFFF9E6), // Very light yellow tint
            iconTint    = Color(0xFFFFC915), // Brand Amber
            title       = "Add Income",
            subtitle    = "Record salary, bonus, freelance & more",
            onClick     = onIncome
        )

        OptionDivider()

        // Manual: Using a neutral Gray or Purple tint
        EntryOption(
            icon        = Icons.Outlined.Edit,
            iconBg      = AppColors.PrimaryLight,        // Your new light purple tint (#F3EBF7)
            iconTint    = AppColors.Primary,         // Your brand purple (#652F80)
            title       = "Enter Manually",
            subtitle    = "Type merchant, amount, category & date",
            onClick     = onManual
        )

        OptionDivider()

        // Scan: Highlighting this with Brand Purple
        EntryOption(
            icon        = Icons.Outlined.PhotoCamera,
            iconBg      = AppColors.PrimaryLight,        // Your new light purple tint
            iconTint    = AppColors.Primary,         // Your brand purple
            title       = "Scan Receipt",
            subtitle    = "Capture a receipt using your camera",
            onClick     = onScanCamera
        )

        OptionDivider()

        // Gallery: Using a softer variation
        EntryOption(
            icon        = Icons.Outlined.PhotoLibrary,
            iconBg      = Color(0xFFF7F8FA), // AppColors.Background
            iconTint    = AppColors.TextSecondary,     // Muted gray for secondary action
            title       = "Upload from Gallery",
            subtitle    = "Pick an existing receipt image",
            onClick     = onUploadGallery
        )
    }
}

// ─── Single option row ────────────────────────────────────────────────────────
@Composable
private fun EntryOption(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = AppColors.TextPrimary
            )
            Text(
                subtitle,
                fontSize   = 13.sp,
                color      = AppColors.TextSecondary,
                lineHeight = 18.sp
            )
        }

        // Chevron
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint     = AppColors.Divider,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun OptionDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 60.dp),
        color     = AppColors.Divider,
        thickness = 0.8.dp
    )
}

