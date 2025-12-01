package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors

/**
 * Navigation destinations for the app.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Ana Sayfa", Icons.Default.Home)
    data object BearingFinder : Screen("bearing_finder", "Rulman Bulucu", Icons.Default.Search)
    data object ElectricalWizard : Screen("electrical_wizard", "Elektrik Sihirbazı", Icons.Default.Bolt)
    data object Reports : Screen("reports", "Raporlar", Icons.Default.Description)
    data object ExcelTemplateBuilder : Screen("excel_template_builder", "Excel Şablon Oluşturucu", Icons.Default.TableChart)
    data object GeneralControl : Screen("general_control", "Birleşik Kontroller", Icons.Default.CheckCircle)
    data object WorkOrder : Screen("work_order", "İş Emri", Icons.Default.Build)
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
    val themeColors = LocalThemeColors.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header - Only showing ASSAN HANİL BURSA BAKIM
        Text(
            text = "ASSAN HANİL",
            style = MaterialTheme.typography.headlineMedium,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "BURSA BAKIM",
            style = MaterialTheme.typography.titleMedium,
            color = themeColors.textSecondary
        )
    }
}
