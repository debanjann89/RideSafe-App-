package com.example.ridesafeautoreply.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    primaryContainer = NeonGreenDark,
    onPrimaryContainer = Color.White,
    secondary = NeonGreenGlow,
    onSecondary = Color.Black,
    background = DeepBlack,
    onBackground = PureWhite,
    surface = CarbonGray,
    onSurface = PureWhite,
    surfaceVariant = CarbonLight,
    onSurfaceVariant = PureWhite,
    outline = DividerGray,
    error = AlertRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreenDark,
    onPrimary = Color.White,
    primaryContainer = NeonGreen,
    onPrimaryContainer = Color.Black,
    secondary = NeonGreenGlow,
    onSecondary = Color.Black,
    background = Color(0xFFF9F9FB),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF1C1C1E),
    outline = Color(0xFFD1D1D6),
    error = AlertRed,
    onError = Color.White
)

@Composable
fun RideSafeAutoReplyTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for the premium dashboard look
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our carbon + neon green brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
