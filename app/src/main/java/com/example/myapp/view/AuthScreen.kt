package com.example.myapp.view

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.viewmodel.UserViewModel
import com.example.myapp.viewmodel.UserViewModelFactory
import com.example.myapp.R
import com.example.myapp.model.getCurrencySymbol
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

val AppBg      = Color(0xFF120D09)
val AppCard    = Color(0xFF1E160F)
val AppAccent  = Color(0xFFFFAB91)
val AppGreen   = Color(0xFF66BB6A)
val AppRed     = Color(0xFFEF5350)


private enum class AuthMode { WELCOME, SIGN_IN, SIGN_UP, FORGOT_PASSWORD }


@Composable
fun AuthScreen(
    pendingUsername : String,
    pendingCurrency : String,
    onAuthSuccess   : () -> Unit,
    userViewModel   : UserViewModel = viewModel(factory = UserViewModelFactory())
) {
    var mode           by remember { mutableStateOf(AuthMode.WELCOME) }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf<String?>(null) }


    var siEmail        by remember { mutableStateOf("") }
    var siPassword     by remember { mutableStateOf("") }
    var siPwVisible    by remember { mutableStateOf(false) }


    var suEmail        by remember { mutableStateOf("") }
    var suPassword     by remember { mutableStateOf("") }
    var suConfirm      by remember { mutableStateOf("") }
    var suPwVisible    by remember { mutableStateOf(false) }
    var suCfVisible    by remember { mutableStateOf(false) }


    var fpEmail        by remember { mutableStateOf("") }
    var fpSent         by remember { mutableStateOf(false) }

    fun saveProfile(uid: String, email: String) {
        FirebaseDatabase.getInstance()
            .getReference("users/$uid/profile")
            .setValue(
                mapOf(
                    "username"       to pendingUsername,
                    "email"          to email,
                    "currency"       to pendingCurrency,
                    "currencySymbol" to getCurrencySymbol(pendingCurrency)
                )
            )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(Color(0x20FFB74D), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.85f, size.height * 0.1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            AnimatedVisibility(mode == AuthMode.WELCOME,
                enter = fadeIn() + slideInVertically { 20 },
                exit  = fadeOut() + slideOutVertically { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Finpatch",
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                    Text(
                        "Personal finance tracker",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E))
                    )
                    Spacer(Modifier.height(16.dp))

                    // User chip
                    Surface(
                        shape  = RoundedCornerShape(50),
                        color  = AppCard,
                        border = BorderStroke(1.dp, Color(0xFF3A2E24))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👤", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                pendingUsername,
                                style = TextStyle(fontSize = 14.sp, color = AppAccent, fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "·  ${getCurrencySymbol(pendingCurrency)} $pendingCurrency",
                                style = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E))
                            )
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                    errorMsg?.let { AuthErrorBanner(it); Spacer(Modifier.height(12.dp)) }

                    AuthPrimaryButton("Sign In") {
                        mode = AuthMode.SIGN_IN; errorMsg = null
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick  = { mode = AuthMode.SIGN_UP; errorMsg = null },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, AppAccent.copy(alpha = 0.5f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent)
                    ) {
                        Text("Create Account", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Forgot your password?",
                        style    = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E)),
                        modifier = Modifier.clickable { mode = AuthMode.FORGOT_PASSWORD; errorMsg = null }
                    )
                }
            }

            AnimatedVisibility(mode == AuthMode.SIGN_IN,
                enter = fadeIn() + slideInVertically { 30 },
                exit  = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo()
                    Spacer(Modifier.height(12.dp))
                    AuthSectionTitle("Welcome back", "Sign in to continue")
                    Spacer(Modifier.height(28.dp))

                    errorMsg?.let { AuthErrorBanner(it); Spacer(Modifier.height(12.dp)) }

                    AuthTextField(siEmail, { siEmail = it; errorMsg = null },
                        "Email", "you@example.com", KeyboardType.Email)
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(siPassword, { siPassword = it; errorMsg = null },
                        "Password", "••••••••", KeyboardType.Password,
                        if (siPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ siPwVisible = !siPwVisible }) {
                                Icon(
                                    painterResource(
                                        if (siPwVisible) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ), null, tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            "Forgot password?",
                            style    = TextStyle(fontSize = 13.sp, color = AppAccent),
                            modifier = Modifier.clickable { mode = AuthMode.FORGOT_PASSWORD; errorMsg = null }
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(36.dp))
                    } else {
                        AuthPrimaryButton("Sign In") {
                            if (siEmail.isBlank() || siPassword.isBlank()) {
                                errorMsg = "Please fill in all fields."; return@AuthPrimaryButton
                            }
                            isLoading = true
                            userViewModel.login(siEmail.trim(), siPassword) { success, message ->
                                isLoading = false
                                if (success) onAuthSuccess() else errorMsg = message
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AuthNavRow("Don't have an account?", "Sign up") {
                        mode = AuthMode.SIGN_UP; errorMsg = null
                    }
                    Spacer(Modifier.height(10.dp))
                    AuthBackLink { mode = AuthMode.WELCOME; errorMsg = null }
                }
            }

            AnimatedVisibility(mode == AuthMode.SIGN_UP,
                enter = fadeIn() + slideInVertically { 30 },
                exit  = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo()
                    Spacer(Modifier.height(12.dp))
                    AuthSectionTitle("Create Account", "Start tracking your finances")
                    Spacer(Modifier.height(28.dp))

                    errorMsg?.let { AuthErrorBanner(it); Spacer(Modifier.height(12.dp)) }

                    AuthTextField(suEmail, { suEmail = it; errorMsg = null },
                        "Email", "you@example.com", KeyboardType.Email)
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(suPassword, { suPassword = it; errorMsg = null },
                        "Password", "Min 6 characters", KeyboardType.Password,
                        if (suPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ suPwVisible = !suPwVisible }) {
                                Icon(
                                    painterResource(
                                        if (suPwVisible) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ), null, tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(suConfirm, { suConfirm = it; errorMsg = null },
                        "Confirm Password", "Re-enter password", KeyboardType.Password,
                        if (suCfVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ suCfVisible = !suCfVisible }) {
                                Icon(
                                    painterResource(
                                        if (suCfVisible) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ), null, tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(24.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(36.dp))
                    } else {
                        AuthPrimaryButton("Create Account") {
                            when {
                                suEmail.isBlank()        -> errorMsg = "Please enter your email."
                                suPassword.isBlank()     -> errorMsg = "Please enter a password."
                                suPassword.length < 6    -> errorMsg = "Password must be at least 6 characters."
                                suPassword != suConfirm  -> errorMsg = "Passwords do not match."
                                else -> {
                                    isLoading = true
                                    userViewModel.register(suEmail.trim(), suPassword) { success, message, userId ->
                                        isLoading = false
                                        if (success) {
                                            // Save profile (username + currency) to Firebase
                                            saveProfile(userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: "", suEmail.trim())
                                            onAuthSuccess()
                                        } else {
                                            errorMsg = message
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AuthNavRow("Already have an account?", "Sign in") {
                        mode = AuthMode.SIGN_IN; errorMsg = null
                    }
                    Spacer(Modifier.height(10.dp))
                    AuthBackLink { mode = AuthMode.WELCOME; errorMsg = null }
                }
            }

            AnimatedVisibility(mode == AuthMode.FORGOT_PASSWORD,
                enter = fadeIn() + slideInVertically { 30 },
                exit  = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo()
                    Spacer(Modifier.height(12.dp))
                    Text("🔑", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    AuthSectionTitle("Reset Password", "We'll send a reset link to your email")
                    Spacer(Modifier.height(28.dp))

                    if (fpSent) {
                        AuthSuccessBanner("Reset link sent! Check your inbox.")
                        Spacer(Modifier.height(24.dp))
                    } else {
                        errorMsg?.let { AuthErrorBanner(it); Spacer(Modifier.height(12.dp)) }
                        AuthTextField(fpEmail, { fpEmail = it; errorMsg = null },
                            "Email address", "you@example.com", KeyboardType.Email)
                        Spacer(Modifier.height(24.dp))
                        if (isLoading) {
                            CircularProgressIndicator(color = AppAccent, modifier = Modifier.size(36.dp))
                        } else {
                            AuthPrimaryButton("Send Reset Link") {
                                if (fpEmail.isBlank()) {
                                    errorMsg = "Please enter your email."; return@AuthPrimaryButton
                                }
                                isLoading = true
                                userViewModel.forgetPassword(fpEmail.trim()) { success, message ->
                                    isLoading = false
                                    if (success) fpSent = true else errorMsg = message
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AuthBackLink("← Back to Sign In") { mode = AuthMode.SIGN_IN; errorMsg = null; fpSent = false }
                }
            }
        }
    }
}

@Composable
fun AppLogo() {
    androidx.compose.foundation.Image(
        painter            = painterResource(R.drawable.logo2),
        contentDescription = "Logo",
        modifier           = Modifier.size(72.dp)
    )
}

@Composable
private fun AuthSectionTitle(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E), textAlign = TextAlign.Center))
    }
}

@Composable
fun AuthPrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = AppAccent)
    ) {
        Text(label, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF120D09)))
    }
}

@Composable
private fun AuthTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color(0xFF4A4A4A)) },
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AppAccent,
            unfocusedBorderColor    = Color(0xFF3A2E24),
            focusedLabelColor       = AppAccent,
            unfocusedLabelColor     = Color(0xFF9E9E9E),
            focusedContainerColor   = AppCard,
            unfocusedContainerColor = AppCard,
            cursorColor             = AppAccent,
            focusedTextColor        = Color.White,
            unfocusedTextColor      = Color.White,
        ),
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthNavRow(prompt: String, action: String, onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.Center) {
        Text(prompt, style = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E)))
        Spacer(Modifier.width(4.dp))
        Text(action, style = TextStyle(fontSize = 13.sp, color = AppAccent, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.clickable(onClick = onClick))
    }
}

@Composable
private fun AuthBackLink(text: String = "← Back", onClick: () -> Unit) {
    Text(text, style = TextStyle(fontSize = 13.sp, color = Color(0xFF9E9E9E)),
        modifier = Modifier.clickable(onClick = onClick))
}

@Composable
fun AuthErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D1515), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF5C2020), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text("⚠ $message", style = TextStyle(fontSize = 13.sp, color = Color(0xFFFC8181), lineHeight = 18.sp))
    }
}

@Composable
private fun AuthSuccessBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D2D1E), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1A5C3A), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text("✓ $message", style = TextStyle(fontSize = 13.sp, color = Color(0xFF68D391), lineHeight = 18.sp))
    }
}