package com.example.myapp.view

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import java.net.URLDecoder
import java.net.URLEncoder

// ─── Routes ──────────────────────────────────────────────────────────────────

object Routes {
    const val LANDING   = "landing"
    const val AUTH      = "auth/{username}/{currency}"
    const val DASHBOARD = "dashboard"

    fun authRoute(username: String, currency: String): String {
        val u = URLEncoder.encode(username, "UTF-8")
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

        // ── Landing: username + currency picker ───────────────────────────────
        composable(Routes.LANDING) {
            LandingScreen(
                onProceed = { username, currency ->
                    navController.navigate(Routes.authRoute(username, currency.code))
                }
            )
        }

        // ── Auth: Google / Email sign-in / Register / Forgot password ─────────
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
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard: bottom nav tabs ────────────────────────────────────────
        composable(Routes.DASHBOARD) {
            DashboardScreen()
        }
    }
}