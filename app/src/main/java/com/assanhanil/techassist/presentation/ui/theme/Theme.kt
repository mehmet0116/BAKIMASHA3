package com.assanhanil.techassist.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
 * TechAssist Theme - Industrial Dark Mode design.
 * Always uses dark theme for AMOLED optimization.
 */
@Composable
fun TechAssistTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TechAssistColors.Background.toArgb()
            window.navigationBarColor = TechAssistColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
