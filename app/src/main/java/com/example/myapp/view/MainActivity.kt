package com.example.myapp.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // If user is already signed in, skip onboarding and go straight to dashboard
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
