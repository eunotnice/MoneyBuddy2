package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditExpenseLoaderUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val expense: Expense? = null
)

class EditExpenseLoaderViewModel(
    private val repo: MoneyRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EditExpenseLoaderUiState())
    val ui: StateFlow<EditExpenseLoaderUiState> = _ui

    fun loadExpense(expenseId: String) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _ui.value = EditExpenseLoaderUiState(error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _ui.value = EditExpenseLoaderUiState(loading = true)

            val expense = repo.getExpenseById(user.uid, expenseId)

            _ui.value = if (expense != null) {
                EditExpenseLoaderUiState(expense = expense)
            } else {
                EditExpenseLoaderUiState(error = "Expense not found")
            }
        }
    }
}

class EditExpenseLoaderViewModelFactory(
    private val repo: MoneyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditExpenseLoaderViewModel::class.java)) {
            return EditExpenseLoaderViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}