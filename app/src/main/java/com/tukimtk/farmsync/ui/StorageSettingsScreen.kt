package com.tukimtk.farmsync.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.data.StorageSyncEngine
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ConnectionStatus {
    IDLE, TESTING, CONNECTED, AUTH_FAILED, UNREACHABLE
}

enum class SyncDirection {
    PUSH_TO_PC, PULL_TO_ANDROID, AUTO_DETECT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageSettingsScreen(snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncEngine = remember { StorageSyncEngine(context) }
    val selectedUri = remember { mutableStateOf<Uri?>(null) }
    
    val prefs = remember {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "farmsync_smb_prefs_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    var smbHost by remember { mutableStateOf(prefs.getString("smb_host", "") ?: "") }
    var smbShare by remember { mutableStateOf(prefs.getString("smb_share", "") ?: "") }
    var smbUser by remember { mutableStateOf(prefs.getString("smb_user", "") ?: "") }
    var smbPass by remember { mutableStateOf(prefs.getString("smb_pass", "") ?: "") }

    var connectionStatus by remember { mutableStateOf(ConnectionStatus.IDLE) }
    var syncDirection by remember { mutableStateOf(SyncDirection.AUTO_DETECT) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Storage & Sync Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Local Storage Provider
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Local Storage Provider (SAF)", fontWeight = FontWeight.SemiBold)
                Text(text = "Current Path: ${selectedUri.value?.path ?: "None"}", color = Color.Gray, fontSize = 14.sp)
                Button(onClick = { safLauncher.launch(null) }, shape = MaterialTheme.shapes.small) {
                    Text("Select Sync Folder")
                }
            }
        }

        // LAN / SMB Quick Sync Card
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LAN / SMB Quick Sync", fontWeight = FontWeight.SemiBold)
                
                OutlinedTextField(
                    value = smbHost,
                    onValueChange = { smbHost = it },
                    label = { Text("PC Host IP / Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = smbShare,
                    onValueChange = { smbShare = it },
                    label = { Text("Shared Folder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = smbUser,
                    onValueChange = { smbUser = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = smbPass,
                    onValueChange = { smbPass = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            prefs.edit().apply {
                                putString("smb_host", smbHost)
                                putString("smb_share", smbShare)
                                putString("smb_user", smbUser)
                                putString("smb_pass", smbPass)
                                apply()
                            }
                            
                            scope.launch {
                                connectionStatus = ConnectionStatus.TESTING
                                delay(1500) // Simulate network test
                                if (smbHost.isBlank()) {
                                    connectionStatus = ConnectionStatus.UNREACHABLE
                                } else if (smbUser == "wrong") {
                                    connectionStatus = ConnectionStatus.AUTH_FAILED
                                } else {
                                    connectionStatus = ConnectionStatus.CONNECTED
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Test Connection")
                    }

                    if (connectionStatus != ConnectionStatus.IDLE) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    when (connectionStatus) {
                                        ConnectionStatus.TESTING -> "Testing..."
                                        ConnectionStatus.CONNECTED -> "Connected"
                                        ConnectionStatus.AUTH_FAILED -> "Auth Failed"
                                        ConnectionStatus.UNREACHABLE -> "Unreachable"
                                        else -> ""
                                    }
                                )
                            },
                            leadingIcon = {
                                if (connectionStatus == ConnectionStatus.TESTING) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> Color(0xFFE8F5E9)
                                    ConnectionStatus.AUTH_FAILED, ConnectionStatus.UNREACHABLE -> Color(0xFFFFEBEE)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                labelColor = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> Color(0xFF2E7D32)
                                    ConnectionStatus.AUTH_FAILED, ConnectionStatus.UNREACHABLE -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        )
                    }
                }
            }
        }

        // 1-Click Sync Section
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1-Click Sync PC <-> Android", fontWeight = FontWeight.SemiBold)
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = syncDirection == SyncDirection.PUSH_TO_PC,
                        onClick = { syncDirection = SyncDirection.PUSH_TO_PC },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Push to PC", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = syncDirection == SyncDirection.AUTO_DETECT,
                        onClick = { syncDirection = SyncDirection.AUTO_DETECT },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Auto-Detect", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = syncDirection == SyncDirection.PULL_TO_ANDROID,
                        onClick = { syncDirection = SyncDirection.PULL_TO_ANDROID },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Pull to Android", fontSize = 12.sp)
                    }
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Syncing with direction: ${syncDirection.name}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Start Sync")
                }
            }
        }

        // AI Translation Settings
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI Translation Settings", fontWeight = FontWeight.SemiBold)
                Button(onClick = { showDialog.value = true }, shape = MaterialTheme.shapes.small) {
                    Text("Configure API Key")
                }
            }
        }
    }

    if (showDialog.value) {
        ApiKeyConfigDialog(onDismiss = { showDialog.value = false })
    }
}
