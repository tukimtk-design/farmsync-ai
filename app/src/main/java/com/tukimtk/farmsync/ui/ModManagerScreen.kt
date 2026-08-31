package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModManagerScreen() {
    val modEnabled = remember { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Installed Mods")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Content Patcher", modifier = Modifier.weight(1f))
            Switch(checked = modEnabled.value, onCheckedChange = { modEnabled.value = it })
        }
        Button(onClick = { /* Trigger ModInstaller */ }, modifier = Modifier.padding(top = 16.dp)) {
            Text("+ Install .zip")
        }
    }
}
