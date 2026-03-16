package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class ExpenseViewModel (
    private val repo: MoneyRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState

    fun addManualExpense(
        merchant: String,
        amount: Double,
        category: String,
        description: String,
        dateMillis: Long,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if(user == null){
            _uiState.value = ExpenseUiState(error = "User not logged in")
            return
        }
        _uiState.value = ExpenseUiState(loading = true)

        val expense = Expense (
            merchant = merchant.trim(),
            amount = amount,
            category = category,
            description = description,
            dateMillis = dateMillis,
            source = "manual"
        )

        viewModelScope.launch{
            val ok = repo.addExpense(user.uid, expense)
            _uiState.value = if (ok) ExpenseUiState(success = true) else ExpenseUiState(error = "Failed to add expense")
            if (ok) onSuccess()
        }
    }

    fun updateExpense(uid: String, expense: Expense, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repo.updateExpense(uid, expense)
            onResult(success)
        }
    }

    fun clearStatus(){
        _uiState.value = ExpenseUiState()
    }
}