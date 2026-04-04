package com.example.moneybuddy2.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ADD_INCOME = "add_income"
    const val EDIT_INCOME = "edit_income"
    const val EDIT_INCOME_ROUTE = "edit_income/{incomeId}"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense"
    const val EDIT_EXPENSE_ROUTE = "edit_expense/{expenseId}"

    fun editExpense(expenseId: String) = "$EDIT_EXPENSE/$expenseId"
    fun editIncome(incomeId: String) = "$EDIT_INCOME/$incomeId"

    const val RECEIPT_SCAN = "receipt_scan"
    const val RECEIPT_PICK = "receipt_pick"
    const val RECEIPT_CONFIRM = "receipt_confirm"
    const val PROFILE = "profile"
    const val CHAT = "chat"
    const val RECEIPT_MODE = "receipt_mode"
    const val RECEIPT_GRAPH = "receipt_graph"
    const val RECOMMENDATIONS = "recommendations"
    const val CHATBOT = "chatbot"
    const val CARBON = "carbon"
    const val ANALYTICS = "analytics"
    const val GAME_GRAPH = "game_graph"
    const val GAME = "game"
    const val GAME_INTRO = "intro"
    const val RESULT = "result"
}
