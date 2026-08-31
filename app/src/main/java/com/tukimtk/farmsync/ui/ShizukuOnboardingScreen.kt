package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShizukuOnboardingScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Step 1: Install Shizuku from Play Store")
        Text("Step 2: Enable Wireless Debugging")
        Text("Step 3: Start Shizuku Service")
        Button(onClick = { /* Trigger Shizuku check */ }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Verify Shizuku Permission")
        }
    }
}
