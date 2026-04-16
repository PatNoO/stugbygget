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

enum class AppTheme {
    SVENSK_SOMMAR,
    SVERIGE,
    SOMMARDAG,
    VIRKE,
}

enum class DarkMode {
    SYSTEM,
    LIGHT,
    DARK,
}

// ── Svensk Sommar ──────────────────────────────────
private val SvenskSommarLight = lightColorScheme(
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

private val SvenskSommarDark = darkColorScheme(
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

// ── Sverige ────────────────────────────────────────
private val SverigeLight = lightColorScheme(
    primary = SverigeBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = SverigeBlueContainer,
    onPrimaryContainer = OnSverigeBlueContainer,
    secondary = SverigeYellowDark,
    onSecondary = SverigeTextDark,
    secondaryContainer = SverigeYellowContainer,
    onSecondaryContainer = OnSverigeYellowContainer,
    tertiary = SverigeBlueLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = SverigeBlueContainer,
    onTertiaryContainer = OnSverigeBlueContainer,
    background = SverigeBackground,
    onBackground = SverigeTextDark,
    surface = SverigeCardWhite,
    onSurface = SverigeTextDark,
    surfaceVariant = SverigeSurface,
    onSurfaceVariant = SverigeTextMedium,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = SverigeBorder,
    outlineVariant = SverigeBorderLight,
    inverseSurface = SverigeTextDark,
    inverseOnSurface = SverigeBackground,
    inversePrimary = SverigeBlueContainer,
    surfaceTint = SverigeBlue,
)

private val SverigeDark = darkColorScheme(
    primary = SverigeBlueLight,
    onPrimary = Color(0xFF002B47),
    primaryContainer = SverigeBlueDark,
    onPrimaryContainer = SverigeBlueContainer,
    secondary = SverigeYellowLight,
    onSecondary = Color(0xFF3A2F00),
    secondaryContainer = SverigeYellowDark,
    onSecondaryContainer = SverigeYellowContainer,
    tertiary = SverigeBlueLight,
    onTertiary = Color(0xFF002B47),
    tertiaryContainer = SverigeBlueDark,
    onTertiaryContainer = SverigeBlueContainer,
    background = Color(0xFF0D1A26),
    onBackground = Color(0xFFD6E8F5),
    surface = Color(0xFF142030),
    onSurface = Color(0xFFD6E8F5),
    surfaceVariant = Color(0xFF1C2E3E),
    onSurfaceVariant = Color(0xFFA8C4D8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF2E4A62),
    outlineVariant = Color(0xFF1E3448),
)

// ── Sommardag ──────────────────────────────────────
private val SommardagLight = lightColorScheme(
    primary = SommarSky,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = SommarSkyContainer,
    onPrimaryContainer = OnSommarSkyContainer,
    secondary = SommarSunshineDark,
    onSecondary = SommarTextDark,
    secondaryContainer = SommarSunshineContainer,
    onSecondaryContainer = OnSommarSunshineContainer,
    tertiary = SommarMeadow,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = SommarMeadowContainer,
    onTertiaryContainer = OnSommarMeadowContainer,
    background = SommarBackground,
    onBackground = SommarTextDark,
    surface = SommarCardWhite,
    onSurface = SommarTextDark,
    surfaceVariant = SommarSurfaceVariant,
    onSurfaceVariant = SommarTextMedium,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = SommarBorder,
    outlineVariant = SommarBorderLight,
    inverseSurface = SommarTextDark,
    inverseOnSurface = SommarBackground,
    inversePrimary = SommarSkyContainer,
    surfaceTint = SommarSky,
)

private val SommardagDark = darkColorScheme(
    primary = SommarSkyLight,
    onPrimary = Color(0xFF0D2E4A),
    primaryContainer = SommarSkyDark,
    onPrimaryContainer = SommarSkyContainer,
    secondary = SommarSunshineLight,
    onSecondary = Color(0xFF3A2C00),
    secondaryContainer = SommarSunshineDark,
    onSecondaryContainer = SommarSunshineContainer,
    tertiary = SommarMeadowLight,
    onTertiary = Color(0xFF0D2E10),
    tertiaryContainer = SommarMeadowDark,
    onTertiaryContainer = SommarMeadowContainer,
    background = Color(0xFF101E10),
    onBackground = Color(0xFFD8ECD8),
    surface = Color(0xFF182018),
    onSurface = Color(0xFFD8ECD8),
    surfaceVariant = Color(0xFF202C20),
    onSurfaceVariant = Color(0xFFA8C8A8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF3A5C3A),
    outlineVariant = Color(0xFF243C24),
)

// ── Virke ──────────────────────────────────────────
private val VirkeLight = lightColorScheme(
    primary = VirkePine,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = VirkePineContainer,
    onPrimaryContainer = OnVirkePineContainer,
    secondary = VirkeOrange,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = VirkeOrangeContainer,
    onSecondaryContainer = OnVirkeOrangeContainer,
    tertiary = VirkeConcrete,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = VirkeConcreteContainer,
    onTertiaryContainer = OnVirkeConcreteContainer,
    background = VirkeBackground,
    onBackground = VirkeTextDark,
    surface = VirkeCardWhite,
    onSurface = VirkeTextDark,
    surfaceVariant = VirkeSurface,
    onSurfaceVariant = VirkeTextMedium,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = VirkeBorder,
    outlineVariant = VirkeBorderLight,
    inverseSurface = VirkeTextDark,
    inverseOnSurface = VirkeBackground,
    inversePrimary = VirkePineContainer,
    surfaceTint = VirkePine,
)

private val VirkeDark = darkColorScheme(
    primary = VirkePineLight,
    onPrimary = Color(0xFF2A1500),
    primaryContainer = VirkePineDark,
    onPrimaryContainer = VirkePineContainer,
    secondary = VirkeOrangeLight,
    onSecondary = Color(0xFF3A1800),
    secondaryContainer = VirkeOrangeDark,
    onSecondaryContainer = VirkeOrangeContainer,
    tertiary = VirkeConcreteLight,
    onTertiary = Color(0xFF1C1C1C),
    tertiaryContainer = VirkeConcreteDark,
    onTertiaryContainer = VirkeConcreteContainer,
    background = Color(0xFF1A1008),
    onBackground = Color(0xFFECE0D0),
    surface = Color(0xFF241810),
    onSurface = Color(0xFFECE0D0),
    surfaceVariant = Color(0xFF2E2018),
    onSurfaceVariant = Color(0xFFC8B09A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF5C3E26),
    outlineVariant = Color(0xFF3E2818),
)

@Composable
fun StugbyggetTheme(
    appTheme: AppTheme = AppTheme.SVENSK_SOMMAR,
    darkMode: DarkMode = DarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (darkMode) {
        DarkMode.DARK -> true
        DarkMode.LIGHT -> false
        DarkMode.SYSTEM -> systemDark
    }

    val colorScheme = when (appTheme) {
        AppTheme.SVENSK_SOMMAR -> if (useDark) SvenskSommarDark else SvenskSommarLight
        AppTheme.SVERIGE -> if (useDark) SverigeDark else SverigeLight
        AppTheme.SOMMARDAG -> if (useDark) SommardagDark else SommardagLight
        AppTheme.VIRKE -> if (useDark) VirkeDark else VirkeLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StugbyggetTypography,
        shapes = StugbyggetShapes,
        content = content
    )
}
