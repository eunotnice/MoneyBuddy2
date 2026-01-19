package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel (
    private val repo: MoneyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _uiState.value = AuthUiState(loading = true)
        FirebaseProvider.auth
            .signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                val user = FirebaseProvider.auth.currentUser
                if (user == null){
                    _uiState.value = AuthUiState(error = "User is null")
                    return@addOnSuccessListener
                }
                viewModelScope.launch{
                    val ok = repo.ensureUserProfile(user.uid, user.email)
                    _uiState.value = if (ok) AuthUiState() else AuthUiState(error = "Failed to create user profile in Firestore")
                    if (ok) onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(error = e.message ?: "Login Failed")
            }
         }

    fun signup(email: String, password: String, onSuccess: () -> Unit) {
        _uiState.value = AuthUiState(loading = true)
        FirebaseProvider.auth
            .createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                val user = FirebaseProvider.auth.currentUser
                if (user == null) {
                    _uiState.value = AuthUiState(error = "Signup succeeded but user is null")
                    return@addOnSuccessListener
                }
                viewModelScope.launch {
                    val ok = repo.ensureUserProfile(user.uid, user.email)
                    _uiState.value = if (ok) AuthUiState() else AuthUiState(error = "Failed to create user profile in Firestore")
                    if (ok) onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = AuthUiState(error = e.message ?: "Signup failed")
            }
    }
    }