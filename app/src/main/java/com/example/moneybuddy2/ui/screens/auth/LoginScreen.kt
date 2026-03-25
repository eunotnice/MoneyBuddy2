package com.example.moneybuddy2.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.AuthUiState
import com.example.moneybuddy2.ui.viewmodel.AuthViewModel

// ─── Colour tokens ────────────────────────────────────────────────────────────
private val GreenMint     = Color(0xFF00C896)
private val GreenDark     = Color(0xFF009E78)
private val GreenLight    = Color(0xFFE6FBF5)
private val SurfaceGray   = Color(0xFFF7F8FA)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val RedSoft       = Color(0xFFE53935)
private val RedLight      = Color(0xFFFFECEC)

// ─────────────────────────────────────────────────────────────────────────────
// LOGIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToSignup: () -> Unit
) {
    val repo = remember { AppContainer().repository }
    val vm: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repo) as T
            }
        }
    )

    val uiState by vm.uiState.collectAsState(initial = AuthUiState())
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthScaffold {
        AuthHero(
            title    = "Welcome back",
            subtitle = "Sign in to your MoneyBuddy account"
        )

        Spacer(Modifier.height(32.dp))

        AuthCard {
            AuthTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "Email address",
                placeholder   = "you@example.com",
                icon          = Icons.Outlined.Email,
                keyboardType  = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "Password",
                placeholder          = "Enter your password",
                icon                 = Icons.Outlined.Lock,
                keyboardType         = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password",
                            tint     = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        AuthErrorBanner(uiState.error)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = { vm.login(email, password, onLoginSuccess) },
            enabled  = !uiState.loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = GreenMint,
                contentColor           = Color.White,
                disabledContainerColor = GreenMint.copy(alpha = 0.4f),
                disabledContentColor   = Color.White.copy(alpha = 0.6f)
            )
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Signing in…", fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Outlined.Login, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", fontSize = 14.sp, color = TextSecondary)
            TextButton(onClick = onGoToSignup) {
                Text("Sign up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenDark)
            }
        }
    }
}
@Composable
private fun AuthScaffold(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
    ) {
        // Soft teal gradient wash at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(GreenMint.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            content = content
        )
    }
}

@Composable
private fun AuthHero(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(GreenMint, GreenDark))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize   = 26.sp,
            color      = TextPrimary,
            textAlign  = TextAlign.Center
        )
        Text(
            subtitle,
            fontSize   = 14.sp,
            color      = TextSecondary,
            textAlign  = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        modifier             = Modifier.fillMaxWidth(),
        label                = { Text(label, fontSize = 13.sp) },
        placeholder          = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon          = { Icon(icon, contentDescription = null, tint = GreenMint, modifier = Modifier.size(20.dp)) },
        trailingIcon         = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions      = KeyboardOptions(keyboardType = keyboardType),
        shape                = RoundedCornerShape(12.dp),
        singleLine           = true,
        colors               = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GreenMint,
            unfocusedBorderColor = DividerColor
        )
    )
}

@Composable
private fun AuthErrorBanner(error: String?) {
    if (error != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RedLight)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = RedSoft)
            Text(error, color = RedSoft, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}



