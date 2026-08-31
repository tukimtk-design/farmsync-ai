package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StorageSettingsScreen() {
    val showDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Storage Provider: Google Drive (Selected)")
        Button(onClick = { /* Switch provider */ }, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Switch Provider (SMB / OneDrive)")
        }

        Text("AI Translation Settings")
        Button(onClick = { showDialog.value = true }) {
            Text("Configure API Key")
        }
    }

    if (showDialog.value) {
        ApiKeyConfigDialog(onDismissRequest = { showDialog.value = false })
    }
}
