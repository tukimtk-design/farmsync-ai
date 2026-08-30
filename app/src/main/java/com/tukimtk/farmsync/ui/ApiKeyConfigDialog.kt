package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun ApiKeyConfigDialog() {
    val apiKey = remember { mutableStateOf("") }
    Column {
        Text("BYOK Gemini API Key Configuration")
        TextField(value = apiKey.value, onValueChange = { apiKey.value = it }, label = { Text("API Key") })
        Button(onClick = { /* Save */ }) {
            Text("Save")
        }
    }
}
