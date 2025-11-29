package com.assanhanil.techassist.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for ASSANHANİL TECH-ASSIST.
 * Supports both Industrial Dark Mode and Light Mode themes.
 */
object TechAssistColors {
    // =====================
    // DARK MODE COLORS
    // =====================
    
    // Primary colors - Industrial Dark Mode
    val Background = Color(0xFF000000)        // AMOLED Black
    val Surface = Color(0xFF121212)           // Dark surface
    val SurfaceVariant = Color(0xFF1E1E1E)    // Slightly lighter surface
    
    // Accent colors - Neon Blue
    val Primary = Color(0xFF00D4FF)           // Neon Blue
    val PrimaryDark = Color(0xFF0099CC)       // Darker Neon Blue
    val PrimaryLight = Color(0xFF66E0FF)      // Lighter Neon Blue
    
    // Secondary accent
    val Secondary = Color(0xFF00FF88)         // Neon Green (for success states)
    val SecondaryDark = Color(0xFF00CC6A)
    
    // Status colors
    val Error = Color(0xFFFF4444)             // Red for errors
    val Warning = Color(0xFFFFAA00)           // Orange for warnings
    val Success = Color(0xFF00FF88)           // Green for success
    
    // Text colors
    val TextPrimary = Color(0xFFFFFFFF)       // White text
    val TextSecondary = Color(0xFFB0B0B0)     // Light gray text
    val TextDisabled = Color(0xFF606060)      // Dark gray text
    
    // Glassmorphism
    val GlassBackground = Color(0x33FFFFFF)   // Semi-transparent white
    val GlassBorder = Color(0x66FFFFFF)       // More opaque border
    
    // Corporate colors
    val CorporateBlue = Color(0xFF003366)     // Assan Hanil Blue
    val CorporateGray = Color(0xFF333333)
    
    // =====================
    // LIGHT MODE COLORS
    // =====================
    
    // Light mode backgrounds
    val LightBackground = Color(0xFFFAFAFA)   // Light gray background
    val LightSurface = Color(0xFFFFFFFF)      // Pure white surface
    val LightSurfaceVariant = Color(0xFFF0F0F0) // Slightly darker surface
    
    // Light mode accent colors
    val LightPrimary = Color(0xFF0088CC)      // Darker blue for light mode
    val LightPrimaryDark = Color(0xFF006699)
    val LightPrimaryLight = Color(0xFF33AADD)
    
    // Light mode secondary
    val LightSecondary = Color(0xFF00AA5C)    // Darker green for light mode
    val LightSecondaryDark = Color(0xFF008847)
    
    // Light mode text colors
    val LightTextPrimary = Color(0xFF1A1A1A)   // Dark text
    val LightTextSecondary = Color(0xFF666666) // Gray text
    val LightTextDisabled = Color(0xFFAAAAAA)  // Light gray text
    
    // Light mode glassmorphism
    val LightGlassBackground = Color(0x33000000) // Semi-transparent dark
    val LightGlassBorder = Color(0x33000000)     // Darker border
    
    // Light mode corporate
    val LightCorporateBlue = Color(0xFF004080)
    val LightCorporateGray = Color(0xFFE0E0E0)
}
