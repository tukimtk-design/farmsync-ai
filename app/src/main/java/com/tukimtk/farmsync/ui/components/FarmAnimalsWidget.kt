package com.tukimtk.farmsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.ui.theme.*

data class FarmAnimalSummary(
    val cows: Int = 0,
    val chickens: Int = 0,
    val ducks: Int = 0,
    val pigs: Int = 0,
    val wasPetToday: Boolean = false,
    val averageHappiness: Int = 0,
    val produceReady: Int = 0
)

@Composable
fun FarmAnimalsWidget(
    summary: FarmAnimalSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🐾", fontSize = 20.sp)
                Text(
                    text = "Farm Animals Summary",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimalCountItem(icon = "🐄", name = "Cows", count = summary.cows)
                AnimalCountItem(icon = "🐔", name = "Chickens", count = summary.chickens)
                AnimalCountItem(icon = "🦆", name = "Ducks", count = summary.ducks)
                AnimalCountItem(icon = "🐖", name = "Pigs", count = summary.pigs)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusItem(
                    label = "Pet Today",
                    value = if (summary.wasPetToday) "Yes" else "No",
                    valueColor = if (summary.wasPetToday) EvergreenForestPrimary else TerracottaPrimary
                )
                StatusItem(
                    label = "Avg. Happiness",
                    value = "${summary.averageHappiness}%",
                    valueColor = AmberWheatPrimary
                )
                StatusItem(
                    label = "Produce Ready",
                    value = summary.produceReady.toString(),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AnimalCountItem(icon: String, name: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusItem(label: String, value: String, valueColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
