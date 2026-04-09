package com.example.stugbygget.data.local

import android.content.Context
import com.example.stugbygget.ui.theme.AppTheme
import com.example.stugbygget.ui.theme.DarkMode

class ThemePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTheme(): AppTheme = AppTheme.entries.find {
        it.name == prefs.getString(KEY_THEME, null)
    } ?: AppTheme.SVENSK_SOMMAR

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getDarkMode(): DarkMode = DarkMode.entries.find {
        it.name == prefs.getString(KEY_DARK_MODE, null)
    } ?: DarkMode.SYSTEM

    fun setDarkMode(darkMode: DarkMode) {
        prefs.edit().putString(KEY_DARK_MODE, darkMode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
