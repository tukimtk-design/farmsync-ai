package com.tukimtk.farmsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EvergreenForestPrimary,
    onPrimary = Color.White,
    primaryContainer = EvergreenForestLight,
    onPrimaryContainer = Color.White,
    secondary = AmberWheatPrimary,
    onSecondary = Color.White,
    secondaryContainer = AmberWheatLight,
    onSecondaryContainer = BarnwoodDark,
    tertiary = TerracottaPrimary,
    background = ParchmentBackground,
    onBackground = BarnwoodDark,
    surface = ParchmentSurface,
    onSurface = BarnwoodDark,
    surfaceVariant = Color(0xFFE5DDCB),
    onSurfaceVariant = BarnwoodDark
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmGlowHighlight,
    onPrimary = MidnightNavyBackground,
    primaryContainer = Color(0xFF1E3A2B),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = AmberWheatPrimary,
    onSecondary = MidnightNavyBackground,
    secondaryContainer = Color(0xFF2D3748),
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = TerracottaPrimary,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF243044),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    errorContainer = Color(0xFF5A1E1E),
    onErrorContainer = Color(0xFFFFD1D1)
)

@Composable
fun FarmSyncAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
