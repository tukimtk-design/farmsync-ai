package com.tukimtk.farmsync.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        // Handle the returned URI
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Storage Provider: Google Drive (Selected)")
        Button(
            onClick = { safPickerLauncher.launch("*/*") },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Select Local File (SAF)")
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
