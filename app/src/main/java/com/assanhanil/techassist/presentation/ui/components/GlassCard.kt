package com.assanhanil.techassist.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors

/**
 * Glassmorphism Card Component.
 * Creates a frosted glass effect on menus and cards.
 * 
 * Design Philosophy: Futuristic, Industrial Dark Mode
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TechAssistColors.GlassBackground,
                        TechAssistColors.GlassBackground.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = TechAssistColors.GlassBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Neon-accented card for primary actions.
 */
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowColor: Color = TechAssistColors.Primary,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(TechAssistColors.Surface)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor,
                        glowColor.copy(alpha = 0.5f),
                        glowColor
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(1.dp)
    ) {
        content()
    }
}

/**
 * Industrial styled surface for content areas.
 */
@Composable
fun IndustrialSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TechAssistColors.SurfaceVariant)
            .border(
                width = 1.dp,
                color = TechAssistColors.CorporateGray,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        content()
    }
}
