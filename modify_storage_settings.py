import re

with open("app/src/main/java/com/tukimtk/farmsync/ui/StorageSettingsScreen.kt", "r") as f:
    content = f.read()

imports_to_add = """import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.tukimtk.farmsync.data.StorageSyncEngine
import android.net.Uri"""

content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\n" + imports_to_add)

content = content.replace("fun StorageSettingsScreen() {", "fun StorageSettingsScreen(snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {")

old_block = """    val showDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {"""

new_block = """    val showDialog = remember { mutableStateOf(false) }
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {"""

content = content.replace(old_block, new_block)

old_button = """        Text("Storage Provider: Google Drive (Selected)")
        Button(onClick = { /* Switch provider */ }, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Switch Provider (SMB / OneDrive)")
        }"""

new_button = """        Text("Local Storage Provider (SAF)")
        Text(text = "Current Path: ${selectedUri.value?.path ?: "None"}", color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = { safLauncher.launch(null) }, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Select Sync Folder")
        }"""

content = content.replace(old_button, new_button)

with open("app/src/main/java/com/tukimtk/farmsync/ui/StorageSettingsScreen.kt", "w") as f:
    f.write(content)
