package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShizukuOnboardingScreen() {
    Column {
        Text("Shizuku Onboarding: 3-Step Guide")
        Button(onClick = { /* Check Perms */ }) {
            Text("Check Permission")
        }
    }
}
