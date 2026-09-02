import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.tukimtk.farmsync.desktop.SaveDetector
import com.tukimtk.farmsync.desktop.SaveMetadata
import com.tukimtk.farmsync.desktop.SaveMetadataParser
import java.text.SimpleDateFormat
import java.util.*

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "FarmSync PC Companion") {
        MaterialTheme {
            CompanionApp()
        }
    }
}

@Composable
fun CompanionApp() {
    val savesDir = remember { SaveDetector.getStardewSaveDirectory() }
    val saves = remember { SaveMetadataParser.findAllSaves(savesDir) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Detected Save Path:",
            style = MaterialTheme.typography.h6
        )
        Text(
            text = savesDir?.absolutePath ?: "Not found",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Detected PC Saves:",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (saves.isEmpty()) {
            Text("No saves found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(saves) { save ->
                    SaveItemCard(save)
                }
            }
        }
    }
}

@Composable
fun SaveItemCard(save: SaveMetadata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${save.farmName} Farm",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = formatTimestamp(save.lastModified),
                    style = MaterialTheme.typography.caption
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Farmer: ${save.farmerName} - ${save.money}G",
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "Year ${save.year}, ${save.season.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                style = MaterialTheme.typography.body2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Folder: ${save.folderName}",
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "Status: Up to date (Sync ID: N/A)",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
