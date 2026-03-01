package com.example.myapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

// ─── Routes ──────────────────────────────────────────────────────────────────

object Routes {
    const val LANDING   = "landing"
    const val AUTH      = "auth/{username}/{currency}"
    const val DASHBOARD = "dashboard"

    // FIX 1: currency parameter changed from Int to String,
    //         since currency.code (e.g. "USD") is already a String
    fun authRoute(username: String, currency: String): String {
        val u = URLEncoder.encode(username, "UTF-8")
        // FIX 2: URLEncoder.encode() requires a String — currency is now String so this works
        val c = URLEncoder.encode(currency, "UTF-8")
        return "auth/$u/$c"
    }
}

// ─── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun FinpatchNavGraph(
    startDestination: String = Routes.LANDING
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Landing: username + currency ──────────────────────────────────────
        composable(Routes.LANDING) {
            LandingScreen(
                onProceed = { username, currency ->
                    navController.navigate(
                        // FIX 3: currency.code is a String, which now matches authRoute(String, String)
                        Routes.authRoute(username, currency.code)
                    )
                }
            )
        }

        // ── Auth: Google / Email sign-in / Forgot password ────────────────────
        // FIX 4: Moved arguments list to a separate val for clarity —
        //         this resolves the navArgument overload resolution error on lines 61/62
        composable(
            route = Routes.AUTH,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("currency") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = URLDecoder.decode(
                backStackEntry.arguments?.getString("username") ?: "", "UTF-8"
            )
            val currency = URLDecoder.decode(
                backStackEntry.arguments?.getString("currency") ?: "USD", "UTF-8"
            )

            AuthScreen(
                pendingUsername = username,
                pendingCurrency = currency,
                onAuthSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Clear entire back stack so Back doesn't return to auth
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ─────────────────────────────────────────────────────────
        composable(Routes.DASHBOARD) {
            DashboardPlaceholder()
        }
    }
}

// ─── Placeholder until you build DashboardScreen ─────────────────────────────

@Composable
private fun DashboardPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F1A)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎉 Dashboard — coming soon!",
            style = TextStyle(fontSize = 20.sp, color = Color.White)
        )
    }
}