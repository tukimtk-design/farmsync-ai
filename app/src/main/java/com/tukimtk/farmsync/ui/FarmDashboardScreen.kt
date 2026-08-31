package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FarmDashboardScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Farm Name: Sunset Farm", style = MaterialTheme.typography.titleLarge)
                Text("Season: Spring, Day: 12, Year: 2")
                Text("Money: 15,400g")
                Text("Save Status: Synced & Verified")
            }
        }

    }
}
