package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val IvoryLightColorScheme =
  lightColorScheme(
    primary = MedTakenGreen,
    onPrimary = Color.White,
    primaryContainer = MedDoneBannerBg,
    onPrimaryContainer = MedDoneBannerText,
    secondary = WaterBluePrimary,
    onSecondary = Color.White,
    secondaryContainer = WaterBlueSurface,
    onSecondaryContainer = WaterBlueDark,
    tertiary = MorningAmberInk,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = EspressoInk,
    surface = IvoryCardSurface,
    onSurface = EspressoInk,
    surfaceVariant = IvoryElevatedBanner,
    onSurfaceVariant = WarmSlateInk,
    outline = IvoryCardBorder,
  )

private val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun SeniorHealthTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = IvoryLightColorScheme,
    typography = SeniorTypography,
    shapes = SeniorShapes,
    content = content,
  )
}
