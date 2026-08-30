package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FarmDashboardScreen() {
    Column {
        Card {
            Text("Farm Dashboard")
            Text("Season: Spring, Date: 1, Money: 500")
            Text("Save Status: OK")
        }
        Button(onClick = { /* Sync */ }) {
            Text("1-Click Sync")
        }
    }
}
