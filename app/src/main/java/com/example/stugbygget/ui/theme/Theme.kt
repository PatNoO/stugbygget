package com.example.stugbygget.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FaluRed,
    onPrimary = TextOnPrimary,
    primaryContainer = FaluRedContainer,
    onPrimaryContainer = OnFaluRedContainer,

    secondary = LakeBlue,
    onSecondary = TextOnPrimary,
    secondaryContainer = LakeBlueContainer,
    onSecondaryContainer = OnLakeBlueContainer,

    tertiary = MeadowGreen,
    onTertiary = TextOnPrimary,
    tertiaryContainer = MeadowGreenContainer,
    onTertiaryContainer = OnMeadowGreenContainer,

    background = CreamBackground,
    onBackground = TextDark,
    surface = CardWhite,
    onSurface = TextDark,
    surfaceVariant = ParchmentSoft,
    onSurfaceVariant = TextMedium,

    error = Error,
    onError = TextOnPrimary,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF410E0B),

    outline = Border,
    outlineVariant = BorderLight,
    inverseSurface = TextDark,
    inverseOnSurface = CreamBackground,
    inversePrimary = FaluRedContainer,
    surfaceTint = FaluRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = FaluRedLight,
    onPrimary = Color(0xFF3B0E04),
    primaryContainer = FaluRedDark,
    onPrimaryContainer = FaluRedContainer,

    secondary = LakeBlueLight,
    onSecondary = Color(0xFF0A2A3A),
    secondaryContainer = LakeBlueDark,
    onSecondaryContainer = LakeBlueContainer,

    tertiary = MeadowGreenLight,
    onTertiary = Color(0xFF0E2A16),
    tertiaryContainer = MeadowGreenDark,
    onTertiaryContainer = MeadowGreenContainer,

    background = Color(0xFF1A1410),
    onBackground = Color(0xFFE8E0D6),
    surface = Color(0xFF241E18),
    onSurface = Color(0xFFE8E0D6),
    surfaceVariant = Color(0xFF2E2620),
    onSurfaceVariant = Color(0xFFC4B8A8),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF5C4A3A),
    outlineVariant = Color(0xFF3E3228),
)

@Composable
fun StugbyggetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StugbyggetTypography,
        shapes = StugbyggetShapes,
        content = content
    )
}
