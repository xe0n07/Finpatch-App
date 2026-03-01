package com.example.myapp.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myApp.viewmodel.UserViewModel
import com.example.myApp.viewmodel.UserViewModelFactory
import com.example.myapp.R
import com.google.android.gms.common.api.ApiException

// ─── AuthMode ────────────────────────────────────────────────────────────────

private enum class AuthMode { WELCOME, SIGN_IN, FORGOT_PASSWORD }

// ─── Screen ──────────────────────────────────────────────────────────────────

/**
 * @param pendingUsername  The username chosen on LandingScreen
 * @param pendingCurrency  The currency code chosen on LandingScreen
 * @param onAuthSuccess    Called when auth + DB save completes — navigate to dashboard
 */
@Composable
fun AuthScreen(
    pendingUsername: String,
    pendingCurrency: String,
    onAuthSuccess: () -> Unit,
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory())
) {
    var mode by remember { mutableStateOf(AuthMode.WELCOME) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Email / password states (for Sign-In and Forgot Password)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotSent by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // ── Google Sign-In launcher ───────────────────────────────────────────────
    val googleSignInClient: GoogleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val idToken: String? = account.idToken
            if (idToken != null) {
                isLoading = true
                errorMsg = null
                userViewModel.signInWithGoogle(
                    idToken = idToken,
                    username = pendingUsername,
                    currency = pendingCurrency
                ) { success, message ->
                    isLoading = false
                    if (success) onAuthSuccess()
                    else errorMsg = message
                }
            } else {
                errorMsg = "Google sign-in failed: no token received."
            }
        } catch (e: ApiException) {
            errorMsg = "Google sign-in failed (code ${e.statusCode}). Try again."
        }
    }

    // ── Background ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x223B82F6), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.15f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.1f, size.height * 0.15f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── WELCOME / GOOGLE ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = mode == AuthMode.WELCOME,
                enter = fadeIn() + slideInVertically { 20 },
                exit = fadeOut() + slideOutVertically { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WelcomeHeader(username = pendingUsername, currency = pendingCurrency)
                    Spacer(Modifier.height(40.dp))

                    // Error message
                    errorMsg?.let {
                        ErrorBanner(it)
                        Spacer(Modifier.height(16.dp))
                    }

                    // Google Sign-Up / Sign-In button
                    GoogleButton(
                        isLoading = isLoading,
                        onClick = {
                            errorMsg = null
                            googleLauncher.launch(googleSignInClient.signInIntent)
                        }
                    )

                    Spacer(Modifier.height(24.dp))
                    DividerOr()
                    Spacer(Modifier.height(24.dp))

                    // Email sign-in
                    OutlinedButton(
                        onClick = { mode = AuthMode.SIGN_IN; errorMsg = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF252A36)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9AA5B8))
                    ) {
                        Text(
                            "Sign in with Email",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "New to this app? Your Google account setup above will create one automatically.",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF4A5568),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // ── SIGN IN WITH EMAIL ───────────────────────────────────────────
            AnimatedVisibility(
                visible = mode == AuthMode.SIGN_IN,
                enter = fadeIn() + slideInVertically { 30 },
                exit = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AuthSectionTitle(
                        title = "Welcome back",
                        subtitle = "Sign in to your account"
                    )
                    Spacer(Modifier.height(32.dp))

                    errorMsg?.let {
                        ErrorBanner(it)
                        Spacer(Modifier.height(16.dp))
                    }

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null },
                        label = "Email",
                        placeholder = "you@example.com",
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = "Password",
                        placeholder = "••••••••",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) R.drawable.baseline_visibility_off_24
                                        else R.drawable.baseline_visibility_24
                                    ),
                                    contentDescription = null,
                                    tint = Color(0xFF6B7585)
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            "Forgot password?",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF3B82F6)),
                            modifier = Modifier.clickable {
                                mode = AuthMode.FORGOT_PASSWORD
                                errorMsg = null
                            }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        PrimaryButton("Sign In") {
                            if (email.isBlank() || password.isBlank()) {
                                errorMsg = "Please enter your email and password."
                                return@PrimaryButton
                            }
                            isLoading = true
                            userViewModel.login(email.trim(), password) { success, message ->
                                isLoading = false
                                if (success) onAuthSuccess()
                                else errorMsg = message
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "← Back",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF6B7585)),
                        modifier = Modifier.clickable { mode = AuthMode.WELCOME; errorMsg = null }
                    )
                }
            }

            // ── FORGOT PASSWORD ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = mode == AuthMode.FORGOT_PASSWORD,
                enter = fadeIn() + slideInVertically { 30 },
                exit = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔑", fontSize = 40.sp)
                    Spacer(Modifier.height(16.dp))
                    AuthSectionTitle(
                        title = "Reset password",
                        subtitle = "We'll send a reset link to your email"
                    )
                    Spacer(Modifier.height(32.dp))

                    if (forgotSent) {
                        SuccessBanner("Reset link sent! Check your inbox for $forgotEmail")
                    } else {
                        errorMsg?.let {
                            ErrorBanner(it)
                            Spacer(Modifier.height(12.dp))
                        }

                        AuthTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it; errorMsg = null },
                            label = "Email address",
                            placeholder = "you@example.com",
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(Modifier.height(24.dp))

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            PrimaryButton("Send Reset Link") {
                                if (forgotEmail.isBlank()) {
                                    errorMsg = "Please enter your email address."
                                    return@PrimaryButton
                                }
                                isLoading = true
                                userViewModel.forgetPassword(forgotEmail.trim()) { success, message ->
                                    isLoading = false
                                    if (success) forgotSent = true
                                    else errorMsg = message
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "← Back to Sign In",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF6B7585)),
                        modifier = Modifier.clickable {
                            mode = AuthMode.SIGN_IN
                            errorMsg = null
                            forgotSent = false
                        }
                    )
                }
            }
        }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun WelcomeHeader(username: String, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161921), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF252A36), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Hey, $username 👋",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Currency set to $currency",
                    style = TextStyle(fontSize = 13.sp, color = Color(0xFF10B981))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "One last step — connect your account to start tracking finances.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF6B7585),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun AuthSectionTitle(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = TextStyle(
                fontSize = 13.sp,
                color = Color(0xFF6B7585),
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun GoogleButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF161921),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0F1218)
        ),
        border = BorderStroke(1.dp, Color(0xFF252A36)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF3B82F6),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "G",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4285F4)
                )
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Continue with Google",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
    ) {
        Text(
            label,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        )
    }
}

@Composable
private fun DividerOr() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF252A36))
        Text(
            "  OR  ",
            style = TextStyle(fontSize = 12.sp, color = Color(0xFF4A5568))
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF252A36))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color(0xFF3A4252)) },
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF3B82F6),
            unfocusedBorderColor = Color(0xFF252A36),
            focusedLabelColor = Color(0xFF3B82F6),
            unfocusedLabelColor = Color(0xFF6B7585),
            focusedContainerColor = Color(0xFF161921),
            unfocusedContainerColor = Color(0xFF161921),
            cursorColor = Color(0xFF3B82F6),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D1515), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF5C2020), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(
            "⚠ $message",
            style = TextStyle(fontSize = 13.sp, color = Color(0xFFFC8181), lineHeight = 18.sp)
        )
    }
}

@Composable
private fun SuccessBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D2D1E), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1A5C3A), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(
            "✓ $message",
            style = TextStyle(fontSize = 13.sp, color = Color(0xFF68D391), lineHeight = 18.sp)
        )
    }
}