package com.example.stugbygget.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.example.stugbygget.data.local.ThemePreferences
import com.example.stugbygget.ui.theme.AppTheme
import com.example.stugbygget.ui.theme.DarkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppThemeState(
    val appTheme: AppTheme = AppTheme.SVENSK_SOMMAR,
    val darkMode: DarkMode = DarkMode.SYSTEM,
)

class AppThemeViewModel(
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AppThemeState(
            appTheme = themePreferences.getTheme(),
            darkMode = themePreferences.getDarkMode(),
        )
    )
    val state: StateFlow<AppThemeState> = _state.asStateFlow()

    fun setTheme(theme: AppTheme) {
        themePreferences.setTheme(theme)
        _state.update { it.copy(appTheme = theme) }
    }

    fun setDarkMode(darkMode: DarkMode) {
        themePreferences.setDarkMode(darkMode)
        _state.update { it.copy(darkMode = darkMode) }
    }
}
