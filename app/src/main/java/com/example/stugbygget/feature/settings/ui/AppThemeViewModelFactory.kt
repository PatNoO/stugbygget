package com.example.stugbygget.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.data.local.ThemePreferences

class AppThemeViewModelFactory(
    private val themePreferences: ThemePreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppThemeViewModel(themePreferences) as T
}
