package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IncomeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
class IncomeViewModel (
    private val repo: MoneyRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState

    fun addIncome(
        amount: Double,
        category: String,
        description: String,
        dateMillis: Long,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if(user == null){
            _uiState.value = IncomeUiState(error = "User not logged in")
            return
        }
        _uiState.value = IncomeUiState(loading = true)

        val income = Income(
            amount = amount,
            category = category,
            description = description,
            dateMillis = dateMillis
        )

        viewModelScope.launch{
            val ok = repo.addIncome(user.uid, income)
            _uiState.value = if (ok) IncomeUiState(success = true) else IncomeUiState(error = "Failed to add income")
            if (ok) onSuccess()
        }
    }

    fun updateIncome(
        existingIncome: Income,
        amount: Double,
        category: String,
        description: String,
        dateMillis: Long,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _uiState.value = IncomeUiState(error = "User not logged in")
            return
        }

        _uiState.value = IncomeUiState(loading = true)

        val updatedIncome = existingIncome.copy(
            amount = amount,
            category = category,
            description = description,
            dateMillis = dateMillis
        )

        viewModelScope.launch {
            val ok = repo.updateIncome(user.uid, updatedIncome)
            _uiState.value = if (ok) {
                IncomeUiState(success = true)
            } else {
                IncomeUiState(error = "Failed to update income")
            }
            if (ok) onSuccess()
        }
    }

    fun deleteIncome(
        incomeId: String,
        onSuccess: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _uiState.value = IncomeUiState(error = "User not logged in")
            return
        }

        _uiState.value = IncomeUiState(loading = true)

        viewModelScope.launch {
            val ok = repo.deleteIncome(user.uid, incomeId)
            _uiState.value = if (ok) {
                IncomeUiState(success = true)
            } else {
                IncomeUiState(error = "Failed to delete income")
            }
            if (ok) onSuccess()
        }
    }


    fun clearStatus(){
        _uiState.value = IncomeUiState()
    }
}
