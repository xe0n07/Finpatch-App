package com.example.myapp.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth

/**
 * The ONE and ONLY Activity in the app.
 * Everything else is a @Composable navigated via NavGraph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // If already signed in, skip onboarding and go straight to dashboard
        val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Routes.DASHBOARD
        } else {
            Routes.LANDING
        }

        setContent {
            FinpatchNavGraph(startDestination = startDestination)
        }
    }
}