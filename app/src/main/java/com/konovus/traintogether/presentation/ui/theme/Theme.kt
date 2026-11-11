package com.konovus.traintogether.presentation.ui.theme

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
    primary = Color(0xFFD0BCFF),           // Soft light purple
    onPrimary = Color(0xFF1E1A2D),         // Contrast text

    primaryContainer = Color(0xFF4F378B),  // Deep violet for filled surfaces
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary = Color(0xFFCCC2DC),         // Muted lavender
    onSecondary = Color(0xFF2A1A3F),

    tertiary = Color(0xFFEFB8C8),          // Soft magenta-pink
    onTertiary = Color(0xFF3F001F),

    background = Color(0xFF202023),        // ✅ Darker background (deep indigo blue)
    onBackground = Color(0xFFECE8FF),      // Very light text

    surface = Color(0xFF1B1B1E),           // Even darker for cards/sheets
    onSurface = Color(0xFFF5F4F8),

    surfaceContainer = Color(0xFF1C1C1E), // This is what Card uses by default
    surfaceContainerHigh = Color(0xFF1C1C1E),    // For elevated cards
    surfaceContainerHighest = Color(0xFF1C1C1E), // For highest elevation
    surfaceContainerLow = Color(0xFF1C1C1E),     // For low elevation
    surfaceContainerLowest = Color(0xFF1C1C1E),

    surfaceVariant = Color(0xFF2C2C2E),    // Used for outlines/containers
    onSurfaceVariant = Color(0xFFCAC4D0),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF370B0B)
)


private val LightColorScheme = lightColorScheme(
    // Primary colors - Deep purple for light mode
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF22005D),

    // Secondary colors
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),

    // Tertiary colors
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF31101D),

    // Background - Softer gray (easier on eyes)
    background = Color(0xFFEAEAEA),
    onBackground = Color(0xFF1C1B1F),

    // Surface - Soft white for cards (not pure white)
    surface = Color(0xFFF7F7F7),
    onSurface = Color(0xFF1C1B1F),

    // Surface containers - Off-white tones for cards
    surfaceContainer = Color(0xFFF7F7F7),           // Main cards - soft white
    surfaceContainerHigh = Color(0xFFF2F2F2),       // Slightly darker
    surfaceContainerHighest = Color(0xFFEEEEEE),    // More depth
    surfaceContainerLow = Color(0xFFFAFAFA),        // Lighter variant
    surfaceContainerLowest = Color(0xFFFCFCFC),     // Lightest

    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    // Outline colors - subtle for comfortable viewing
    outline = Color(0xFFAAAAAA),
    outlineVariant = Color(0xFFD0D0D0),

    // Error colors
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Inverse colors
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF),

    scrim = Color(0xFF000000)
)

@Composable
fun TrainTogetherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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