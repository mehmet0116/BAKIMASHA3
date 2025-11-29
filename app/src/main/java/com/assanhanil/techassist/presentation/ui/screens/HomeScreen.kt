package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors

/**
 * Navigation destinations for the app.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Ana Sayfa", Icons.Default.Home)
    data object BearingFinder : Screen("bearing_finder", "Rulman Bulucu", Icons.Default.Search)
    data object ElectricalWizard : Screen("electrical_wizard", "Elektrik Sihirbazı", Icons.Default.Bolt)
    data object Reports : Screen("reports", "Raporlar", Icons.Default.Description)
    data object Recipes : Screen("recipes", "Tarifler", Icons.Default.Book)
    data object Camera : Screen("camera", "Kamera", Icons.Default.CameraAlt)
    data object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
}

/**
 * Main Home Screen with navigation menu.
 */
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        Screen.BearingFinder,
        Screen.ElectricalWizard,
        Screen.Reports,
        Screen.Recipes,
        Screen.Camera,
        Screen.Settings
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "ASSANHANİL",
            style = MaterialTheme.typography.headlineMedium,
            color = TechAssistColors.Primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "TECH-ASSIST",
            style = MaterialTheme.typography.titleMedium,
            color = TechAssistColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dijital Saha Mühendisi Platformu",
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextDisabled
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Menu Grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { screen ->
                        MenuCard(
                            screen = screen,
                            onClick = { onNavigate(screen) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty space if odd number
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    screen: Screen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonCard(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = TechAssistColors.Primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = screen.title,
                style = MaterialTheme.typography.titleSmall,
                color = TechAssistColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
