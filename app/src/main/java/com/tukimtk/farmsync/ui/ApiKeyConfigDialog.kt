package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApiKeyConfigDialog(onDismiss: () -> Unit = {}) {
    var apiKey by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔑 Bring Your Own Key (BYOK)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter your free Google Gemini API Key to enable AI Thai mod translation and persona dialogue generation without subscription fees.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isSaved) {
                    Text("✓ API Key saved successfully!", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                isSaved = true
                onDismiss()
            }) {
                Text("Save & Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
