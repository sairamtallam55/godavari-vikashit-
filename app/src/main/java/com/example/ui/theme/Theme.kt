package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HeritageGold,
    onPrimary = NavyDeep,
    primaryContainer = NavyLight,
    onPrimaryContainer = HeritageGoldLight,
    secondary = SaffronLight,
    onSecondary = Color.White,
    secondaryContainer = SaffronDark,
    onSecondaryContainer = Color.White,
    tertiary = RiverBlueLight,
    onTertiary = NavyDeep,
    tertiaryContainer = RiverBlueDark,
    onTertiaryContainer = Color.White,
    background = NavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = NavySurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = NavyLight,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = HeritageGoldLight,
    onPrimaryContainer = NavyDeep,
    secondary = NavyDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = NavyDeep,
    tertiary = RiverBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = RiverBlueDark,
    background = ParchmentLight,
    onBackground = NavyDeep,
    surface = WarmCard,
    onSurface = NavyDeep,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder
)

@Composable
fun GodavariVirasatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
