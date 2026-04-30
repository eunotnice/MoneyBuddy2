package com.example.moneybuddy2.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.remote.FirestorePaths
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.google.firebase.auth.FirebaseAuth
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
                    val u = FirebaseAuth.getInstance().currentUser
                    Log.d("AUTH", "uid=${u?.uid}, email=${u?.email}")
                    Log.d("FS", "userDoc=${FirestorePaths.userDoc(user.uid).path}")


                    val ok = repo.ensureUserProfile(user.uid, user.email)
                    _uiState.value = if (ok) AuthUiState() else AuthUiState(error = "Failed to sign in")
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

    fun forgotPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email address")
            return
        }
        _uiState.value = AuthUiState(loading = true)
        FirebaseProvider.auth
            .sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                Log.d("AUTH", "Reset email sent successfully to $email")
                _uiState.value = AuthUiState()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("AUTH", "Reset email failed: ${e.message}")
                _uiState.value = AuthUiState(error = e.message ?: "Failed to send reset email")
                onError(e.message ?: "Failed to send reset email")
            }
    }
    }

