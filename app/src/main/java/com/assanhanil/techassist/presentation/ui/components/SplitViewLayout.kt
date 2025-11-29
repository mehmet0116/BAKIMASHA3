package com.assanhanil.techassist.presentation.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors

/**
 * Vertical divider component for split-view layouts.
 * This is a custom implementation for compatibility with older Material3 versions.
 */
@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color)
    )
}

/**
 * Adaptive Split-View Layout Component.
 * 
 * Smart Split-View support: When rotated to Landscape, the screen splits:
 * - Left side: Menu/List
 * - Right side: Workspace
 * 
 * In Portrait mode, only one pane is shown at a time.
 */
@Composable
fun SplitViewLayout(
    menuContent: @Composable () -> Unit,
    workspaceContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    menuWeight: Float = 0.35f,
    workspaceWeight: Float = 0.65f
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    if (isLandscape) {
        // Landscape: Split-View with Menu on left, Workspace on right
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(TechAssistColors.Background)
        ) {
            // Left Pane - Menu/List
            Box(
                modifier = Modifier
                    .weight(menuWeight)
                    .fillMaxHeight()
            ) {
                menuContent()
            }
            
            // Divider
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = TechAssistColors.GlassBorder
            )
            
            // Right Pane - Workspace
            Box(
                modifier = Modifier
                    .weight(workspaceWeight)
                    .fillMaxHeight()
            ) {
                workspaceContent()
            }
        }
    } else {
        // Portrait: Full screen workspace (menu accessed via navigation)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TechAssistColors.Background)
        ) {
            workspaceContent()
        }
    }
}

/**
 * Two-pane layout that always shows both panes regardless of orientation.
 * Useful for tablet-optimized views.
 */
@Composable
fun TwoPaneLayout(
    leftPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leftWeight: Float = 0.4f,
    rightWeight: Float = 0.6f
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
    ) {
        // Left Pane
        Box(
            modifier = Modifier
                .weight(leftWeight)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            leftPane()
        }
        
        // Divider
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            thickness = 1.dp,
            color = TechAssistColors.Primary.copy(alpha = 0.3f)
        )
        
        // Right Pane
        Box(
            modifier = Modifier
                .weight(rightWeight)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            rightPane()
        }
    }
}

/**
 * Adaptive content that changes based on screen width.
 */
@Composable
fun AdaptiveContent(
    compactContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    breakpointDp: Int = 600
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    Box(modifier = modifier) {
        if (screenWidth >= breakpointDp) {
            expandedContent()
        } else {
            compactContent()
        }
    }
}
