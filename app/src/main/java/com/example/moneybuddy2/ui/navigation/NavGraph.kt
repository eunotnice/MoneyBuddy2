package com.example.moneybuddy2.ui.navigation


import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.navigation.compose.navigation

@Composable
fun NavGraph (
    navController: NavHostController,
    startDestination: String
){
    NavHost(navController = navController, startDestination = startDestination) {
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

        composable(Routes.HOME){
            HomeScreen(
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
                }
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

        composable(Routes.ADD_EXPENSE) {
            ManualAddExpenseScreen(
                onBack = { navController.popBackStack() }
            )
        }

//        composable(Routes.RECEIPT_PICK) {
//            ReceiptPickScreen(
//                onBack = { navController.popBackStack() },
//                onGoToConfirm = { navController.navigate(Routes.RECEIPT_CONFIRM) }
//            )
//        }
//
//        composable(Routes.RECEIPT_CONFIRM) {
//            ReceiptConfirmScreen(
//                onBack = { navController.popBackStack() },
//                onSaved = {
//                    navController.navigate(Routes.HOME) {
//                        popUpTo(Routes.HOME) { inclusive = false }
//                    }
//                }
//            )
//        }

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

    }
}

