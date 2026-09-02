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
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = AmberWheatPrimary,
    onSecondary = MidnightNavyBackground,
    secondaryContainer = BarnwoodDark,
    onSecondaryContainer = AmberWheatLight,
    tertiary = TerracottaPrimary,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
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
