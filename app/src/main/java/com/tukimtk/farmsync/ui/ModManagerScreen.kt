package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun ModManagerScreen() {
    val modEnabled = remember { mutableStateOf(true) }
    Column {
        Text("Mod Manager")
        Switch(checked = modEnabled.value, onCheckedChange = { modEnabled.value = it })
        Button(onClick = { /* Install Mod */ }) {
            Text("Install .zip Mod")
        }
    }
}
