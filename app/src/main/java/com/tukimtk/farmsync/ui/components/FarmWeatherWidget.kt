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

data class WeatherForecast(
    val today: String,
    val tomorrow: String,
    val season: String,
    val daysRemaining: Int
)

@Composable
fun FarmWeatherWidget(
    forecast: WeatherForecast,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⛅", fontSize = 20.sp)
                    Text(
                        text = "Weather Forecast",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "${forecast.season} - ${forecast.daysRemaining} days remaining",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDayItem(
                    label = "Today",
                    weather = forecast.today
                )
                WeatherDayItem(
                    label = "Tomorrow",
                    weather = forecast.tomorrow
                )
            }
        }
    }
}

@Composable
private fun WeatherDayItem(label: String, weather: String) {
    val (icon, color) = when (weather.lowercase()) {
        "sunny" -> "☀️" to AmberWheatPrimary
        "rainy" -> "🌧️" to Color(0xFF4B89AC) // Soft blue
        "stormy" -> "⛈️" to Color(0xFF555555)
        "snow" -> "❄️" to Color(0xFFA0C4FF)
        "festival" -> "🎉" to TerracottaPrimary
        else -> "⛅" to Color.Gray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(text = icon, fontSize = 32.sp)
        Text(
            text = weather,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
