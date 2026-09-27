package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BorePileColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = NavyOnPrimary,
    primaryContainer = NavyPrimaryContainer,
    onPrimaryContainer = NavyOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = EmeraldTertiary,
    onTertiary = EmeraldOnTertiary,
    tertiaryContainer = EmeraldTertiaryContainer,
    onTertiaryContainer = EmeraldOnTertiaryContainer,
    background = SlateBackground,
    onBackground = SlateOnBackground,
    surface = PureSurface,
    onSurface = SlateOnSurface,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateOnSurfaceVariant,
    outline = SlateOutline,
    outlineVariant = SlateOutlineVariant
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BorePileColorScheme,
        typography = Typography,
        content = content
    )
}
