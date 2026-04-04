package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.UserProfile
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val profile: UserProfile? = null
)

class ProfileViewModel (
    private val repo: MoneyRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(ProfileUiState(loading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile(){
        val user = FirebaseProvider.auth.currentUser
        if (user == null){
            _uiState.value = ProfileUiState(error = "User not logged in")
            return
        }

        viewModelScope.launch{
            val profile = repo.getUserProfile(user.uid)
            _uiState.value = ProfileUiState(loading = false, profile = profile)
        }
    }

    fun saveProfile(
        displayName: String,
        monthlyIncome: Double,
        monthlyBudget: Double,
        savingGoal: Double,
        currency: String,
        onDone: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null){
            _uiState.value = ProfileUiState(error = "User not logged in")
            return
        }

        val existing = _uiState.value.profile
        _uiState.value = ProfileUiState(loading = true,error=null, saved=false)


        val profile = UserProfile(
            uid = user.uid,
            email = user.email,
            displayName = displayName.trim(),
            monthlyIncome = monthlyIncome,
            monthlyBudget = monthlyBudget,
            savingGoal = savingGoal,
            currency = currency,
            createdAt = existing?.createdAt ?: Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        viewModelScope.launch{
            val ok = repo.saveUserProfile(profile)
            _uiState.value = if (ok) {
                ProfileUiState(loading=false, saved = true, profile = profile)
            } else {
                ProfileUiState(loading = false, error = "Failed to save profile", profile = existing)
            }
            if (ok) onDone()
        }
    }
}