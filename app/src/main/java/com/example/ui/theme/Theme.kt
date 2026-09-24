package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NatureDarkColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = ForestDarkBg,
  primaryContainer = ForestBorder,
  onPrimaryContainer = TextMain,
  secondary = SunlightAmber,
  onSecondary = ForestDarkBg,
  background = ForestDarkBg,
  onBackground = TextMain,
  surface = ForestCard,
  onSurface = TextMain,
  surfaceVariant = ForestSubtle,
  onSurfaceVariant = TextMuted,
  outline = ForestBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NatureDarkColorScheme,
    typography = Typography,
    content = content
  )
}
