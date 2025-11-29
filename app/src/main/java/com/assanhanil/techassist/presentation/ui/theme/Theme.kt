package com.assanhanil.techassist.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Local composition for theme colors based on dark/light mode.
 */
data class ThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val secondary: Color,
    val secondaryDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val corporateBlue: Color,
    val corporateGray: Color,
    val error: Color,
    val warning: Color,
    val success: Color
)

val LocalThemeColors = compositionLocalOf { 
    ThemeColors(
        background = TechAssistColors.Background,
        surface = TechAssistColors.Surface,
        surfaceVariant = TechAssistColors.SurfaceVariant,
        primary = TechAssistColors.Primary,
        primaryDark = TechAssistColors.PrimaryDark,
        primaryLight = TechAssistColors.PrimaryLight,
        secondary = TechAssistColors.Secondary,
        secondaryDark = TechAssistColors.SecondaryDark,
        textPrimary = TechAssistColors.TextPrimary,
        textSecondary = TechAssistColors.TextSecondary,
        textDisabled = TechAssistColors.TextDisabled,
        glassBackground = TechAssistColors.GlassBackground,
        glassBorder = TechAssistColors.GlassBorder,
        corporateBlue = TechAssistColors.CorporateBlue,
        corporateGray = TechAssistColors.CorporateGray,
        error = TechAssistColors.Error,
        warning = TechAssistColors.Warning,
        success = TechAssistColors.Success
    )
}

/**
 * Dark theme colors for AMOLED optimization.
 */
private val DarkThemeColors = ThemeColors(
    background = TechAssistColors.Background,
    surface = TechAssistColors.Surface,
    surfaceVariant = TechAssistColors.SurfaceVariant,
    primary = TechAssistColors.Primary,
    primaryDark = TechAssistColors.PrimaryDark,
    primaryLight = TechAssistColors.PrimaryLight,
    secondary = TechAssistColors.Secondary,
    secondaryDark = TechAssistColors.SecondaryDark,
    textPrimary = TechAssistColors.TextPrimary,
    textSecondary = TechAssistColors.TextSecondary,
    textDisabled = TechAssistColors.TextDisabled,
    glassBackground = TechAssistColors.GlassBackground,
    glassBorder = TechAssistColors.GlassBorder,
    corporateBlue = TechAssistColors.CorporateBlue,
    corporateGray = TechAssistColors.CorporateGray,
    error = TechAssistColors.Error,
    warning = TechAssistColors.Warning,
    success = TechAssistColors.Success
)

/**
 * Light theme colors for daytime use.
 */
private val LightThemeColors = ThemeColors(
    background = TechAssistColors.LightBackground,
    surface = TechAssistColors.LightSurface,
    surfaceVariant = TechAssistColors.LightSurfaceVariant,
    primary = TechAssistColors.LightPrimary,
    primaryDark = TechAssistColors.LightPrimaryDark,
    primaryLight = TechAssistColors.LightPrimaryLight,
    secondary = TechAssistColors.LightSecondary,
    secondaryDark = TechAssistColors.LightSecondaryDark,
    textPrimary = TechAssistColors.LightTextPrimary,
    textSecondary = TechAssistColors.LightTextSecondary,
    textDisabled = TechAssistColors.LightTextDisabled,
    glassBackground = TechAssistColors.LightGlassBackground,
    glassBorder = TechAssistColors.LightGlassBorder,
    corporateBlue = TechAssistColors.LightCorporateBlue,
    corporateGray = TechAssistColors.LightCorporateGray,
    error = TechAssistColors.Error,
    warning = TechAssistColors.Warning,
    success = TechAssistColors.Success
)

/**
 * Industrial Dark Mode color scheme for ASSANHANİL TECH-ASSIST.
 */
private val DarkColorScheme = darkColorScheme(
    primary = TechAssistColors.Primary,
    onPrimary = TechAssistColors.Background,
    primaryContainer = TechAssistColors.PrimaryDark,
    onPrimaryContainer = TechAssistColors.TextPrimary,
    
    secondary = TechAssistColors.Secondary,
    onSecondary = TechAssistColors.Background,
    secondaryContainer = TechAssistColors.SecondaryDark,
    onSecondaryContainer = TechAssistColors.TextPrimary,
    
    background = TechAssistColors.Background,
    onBackground = TechAssistColors.TextPrimary,
    
    surface = TechAssistColors.Surface,
    onSurface = TechAssistColors.TextPrimary,
    surfaceVariant = TechAssistColors.SurfaceVariant,
    onSurfaceVariant = TechAssistColors.TextSecondary,
    
    error = TechAssistColors.Error,
    onError = TechAssistColors.TextPrimary,
    
    outline = TechAssistColors.GlassBorder
)

/**
 * Light Mode color scheme for ASSANHANİL TECH-ASSIST.
 */
private val LightColorScheme = lightColorScheme(
    primary = TechAssistColors.LightPrimary,
    onPrimary = TechAssistColors.LightSurface,
    primaryContainer = TechAssistColors.LightPrimaryLight,
    onPrimaryContainer = TechAssistColors.LightTextPrimary,
    
    secondary = TechAssistColors.LightSecondary,
    onSecondary = TechAssistColors.LightSurface,
    secondaryContainer = TechAssistColors.LightSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = TechAssistColors.LightTextPrimary,
    
    background = TechAssistColors.LightBackground,
    onBackground = TechAssistColors.LightTextPrimary,
    
    surface = TechAssistColors.LightSurface,
    onSurface = TechAssistColors.LightTextPrimary,
    surfaceVariant = TechAssistColors.LightSurfaceVariant,
    onSurfaceVariant = TechAssistColors.LightTextSecondary,
    
    error = TechAssistColors.Error,
    onError = TechAssistColors.LightSurface,
    
    outline = TechAssistColors.LightGlassBorder
)

/**
 * TechAssist Theme - Supports both Dark and Light modes.
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The composable content to render with the theme.
 */
@Composable
fun TechAssistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val themeColors = if (darkTheme) DarkThemeColors else LightThemeColors
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = themeColors.background.toArgb()
            window.navigationBarColor = themeColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
