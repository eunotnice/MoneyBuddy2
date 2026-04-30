package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditIncomeLoaderUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val income: Income? = null
)

class EditIncomeLoaderViewModel(
    private val repo: MoneyRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EditIncomeLoaderUiState())
    val ui: StateFlow<EditIncomeLoaderUiState> = _ui

    fun loadIncome(incomeId: String) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _ui.value = EditIncomeLoaderUiState(error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _ui.value = EditIncomeLoaderUiState(loading = true)

            val income = repo.getIncomeById(user.uid, incomeId)

            _ui.value = if (income != null) {
                EditIncomeLoaderUiState(income = income)
            } else {
                EditIncomeLoaderUiState(error = "Income not found")
            }
        }
    }
}

class EditIncomeLoaderViewModelFactory(
    private val repo: MoneyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditIncomeLoaderViewModel::class.java)) {
            return EditIncomeLoaderViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}