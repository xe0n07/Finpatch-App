package com.example.myapp.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

// ─── Data ────────────────────────────────────────────────────────────────────

data class Currency(val code: String, val symbol: String, val name: String, val flag: String)

val CURRENCIES = listOf(
    Currency("USD", "$",   "US Dollar",         "🇺🇸"),
    Currency("EUR", "€",   "Euro",              "🇪🇺"),
    Currency("GBP", "£",   "British Pound",     "🇬🇧"),
    Currency("JPY", "¥",   "Japanese Yen",      "🇯🇵"),
    Currency("INR", "₹",   "Indian Rupee",      "🇮🇳"),
    Currency("NPR", "Rs",  "Nepali Rupee",      "🇳🇵"),
    Currency("CAD", "C$",  "Canadian Dollar",   "🇨🇦"),
    Currency("AUD", "A$",  "Australian Dollar", "🇦🇺"),
    Currency("CHF", "Fr",  "Swiss Franc",       "🇨🇭"),
    Currency("CNY", "¥",   "Chinese Yuan",      "🇨🇳"),
    Currency("SGD", "S$",  "Singapore Dollar",  "🇸🇬"),
    Currency("AED", "د.إ", "UAE Dirham",        "🇦🇪"),
)

// ─── Screen ──────────────────────────────────────────────────────────────────

/**
 * Onboarding screen: collect username + preferred currency.
 * Called directly from NavGraph — NOT an Activity.
 */
@Composable
fun LandingScreen(
    onProceed: (username: String, currency: Currency) -> Unit
) {
    var username         by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf<Currency?>(null) }
    var usernameError    by remember { mutableStateOf<String?>(null) }
    var currencyError    by remember { mutableStateOf(false) }
    var step             by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gradientShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2A3B82F6), Color.Transparent),
                    center = Offset(w * (0.2f + gradientShift * 0.3f), h * 0.2f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * (0.2f + gradientShift * 0.3f), h * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1A10B981), Color.Transparent),
                    center = Offset(w * (0.8f - gradientShift * 0.2f), h * 0.75f),
                    radius = w * 0.5f
                ),
                radius = w * 0.5f,
                center = Offset(w * (0.8f - gradientShift * 0.2f), h * 0.75f)
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
            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("₣", style = TextStyle(fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Finpatch",
                style = TextStyle(
                    fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, letterSpacing = (-1).sp
                )
            )
            Text(
                "Your personal finance hub",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF6B7585), letterSpacing = 0.5.sp)
            )

            Spacer(Modifier.height(48.dp))

            // Step indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepDot(active = step == 0, done = step > 0, label = "1")
                Box(
                    modifier = Modifier
                        .width(40.dp).height(2.dp)
                        .background(
                            if (step > 0) Color(0xFF3B82F6) else Color(0xFF252A36),
                            RoundedCornerShape(1.dp)
                        )
                )
                StepDot(active = step == 1, done = false, label = "2")
            }

            Spacer(Modifier.height(36.dp))

            // ── STEP 0: Username ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = step == 0,
                enter = fadeIn() + slideInVertically { 30 },
                exit = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "What should we call you?",
                        style = TextStyle(
                            fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This will be your display name inside Finpatch.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF6B7585), textAlign = TextAlign.Center)
                    )
                    Spacer(Modifier.height(28.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; usernameError = null },
                        label = { Text("Username") },
                        placeholder = { Text("e.g. alex_finance", color = Color(0xFF3A4252)) },
                        singleLine = true,
                        isError = usernameError != null,
                        supportingText = usernameError?.let { msg -> { Text(msg, color = Color(0xFFEF4444)) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF252A36),
                            errorBorderColor = Color(0xFFEF4444),
                            focusedLabelColor = Color(0xFF3B82F6),
                            unfocusedLabelColor = Color(0xFF6B7585),
                            focusedContainerColor = Color(0xFF161921),
                            unfocusedContainerColor = Color(0xFF161921),
                            errorContainerColor = Color(0xFF161921),
                            cursorColor = Color(0xFF3B82F6),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(28.dp))

                    LandingPrimaryButton("Continue →") {
                        when {
                            username.isBlank()  -> usernameError = "Username can't be empty"
                            username.length < 3 -> usernameError = "At least 3 characters"
                            username.length > 20 -> usernameError = "Max 20 characters"
                            !username.matches(Regex("^[a-zA-Z0-9._]+$")) ->
                                usernameError = "Only letters, numbers, . and _"
                            else -> step = 1
                        }
                    }
                }
            }

            // ── STEP 1: Currency ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = step == 1,
                enter = fadeIn() + slideInVertically { 30 },
                exit = fadeOut() + slideOutVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Pick your currency",
                        style = TextStyle(
                            fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All transactions and analytics will use this currency.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF6B7585), textAlign = TextAlign.Center)
                    )

                    if (currencyError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Please select a currency to continue.",
                            style = TextStyle(fontSize = 12.sp, color = Color(0xFFEF4444))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        items(CURRENCIES) { currency ->
                            CurrencyCard(
                                currency = currency,
                                selected = selectedCurrency == currency,
                                onClick = { selectedCurrency = currency; currencyError = false }
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 0 },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF252A36)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9AA5B8))
                        ) { Text("← Back") }

                        LandingPrimaryButton(
                            text = "Let's Go 🚀",
                            modifier = Modifier.weight(2f)
                        ) {
                            if (selectedCurrency == null) currencyError = true
                            else onProceed(username.trim(), selectedCurrency!!)
                        }
                    }
                }
            }
        }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun StepDot(active: Boolean, done: Boolean, label: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = when {
                    done   -> Color(0xFF10B981)
                    active -> Color(0xFF3B82F6)
                    else   -> Color(0xFF161921)
                },
                shape = CircleShape
            )
            .border(
                width = if (!active && !done) 1.dp else 0.dp,
                color = Color(0xFF252A36),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (done) "✓" else label,
            style = TextStyle(
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (active || done) Color.White else Color(0xFF6B7585)
            )
        )
    }
}

@Composable
private fun CurrencyCard(currency: Currency, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .aspectRatio(1f)
            .background(
                color = if (selected) Color(0xFF1E3A5F) else Color(0xFF161921),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFF3B82F6) else Color(0xFF252A36),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currency.flag, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                currency.code,
                style = TextStyle(
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (selected) Color(0xFF3B82F6) else Color.White
                )
            )
            Text(currency.symbol, style = TextStyle(fontSize = 11.sp, color = Color(0xFF6B7585)))
        }
    }
}

// Renamed to LandingPrimaryButton to avoid clash with AuthScreen's PrimaryButton
@Composable
fun LandingPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B82F6),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp))
    }
}