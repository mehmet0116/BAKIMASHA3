package com.assanhanil.techassist.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages theme preferences using SharedPreferences.
 * Allows switching between dark and light modes.
 */
class ThemePreferences(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "techassist_theme_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_USE_SYSTEM_THEME = "use_system_theme"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(getDarkModePreference())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _useSystemTheme = MutableStateFlow(getUseSystemThemePreference())
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()
    
    /**
     * Gets the current dark mode preference.
     * Defaults to true (dark mode) if not set.
     */
    fun getDarkModePreference(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, true)
    }
    
    /**
     * Sets the dark mode preference.
     */
    fun setDarkModePreference(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }
    
    /**
     * Gets whether to use system theme.
     * Defaults to false (manual control).
     */
    fun getUseSystemThemePreference(): Boolean {
        return prefs.getBoolean(KEY_USE_SYSTEM_THEME, false)
    }
    
    /**
     * Sets whether to use system theme.
     */
    fun setUseSystemThemePreference(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_THEME, enabled).apply()
        _useSystemTheme.value = enabled
    }
}
