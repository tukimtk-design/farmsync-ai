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
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.tukimtk.farmsync.data.StorageSyncEngine
import android.net.Uri

@Composable
fun StorageSettingsScreen(snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncEngine = remember { StorageSyncEngine(context) }
    val selectedUri = remember { mutableStateOf<Uri?>(null) }

    val safLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    selectedUri.value = uri
                    launch(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("SAF Tree Permission Granted: ${uri.lastPathSegment}")
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Error granting SAF permission: ${e.message}")
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Local Storage Provider (SAF)")
        Text(text = "Current Path: ${selectedUri.value?.path ?: "None"}", color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = { safLauncher.launch(null) }, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Select Sync Folder")
        }

        Text("AI Translation Settings")
        Button(onClick = { showDialog.value = true }) {
            Text("Configure API Key")
        }
    }

    if (showDialog.value) {
        ApiKeyConfigDialog(onDismiss = { showDialog.value = false })
    }
}
