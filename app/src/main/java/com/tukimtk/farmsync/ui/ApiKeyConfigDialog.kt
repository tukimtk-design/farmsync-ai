package com.tukimtk.farmsync.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun ApiKeyConfigDialog(onDismissRequest: () -> Unit) {
    val apiKey = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Gemini API Key") },
        text = {
            TextField(
                value = apiKey.value,
                onValueChange = { apiKey.value = it },
                label = { Text("Enter BYOK Key") }
            )
        },
        confirmButton = {
            Button(onClick = onDismissRequest) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) { Text("Cancel") }
        }
    )
}
