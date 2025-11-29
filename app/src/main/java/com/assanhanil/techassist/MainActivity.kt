package com.assanhanil.techassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.assanhanil.techassist.presentation.ui.screens.*
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors
import com.assanhanil.techassist.presentation.ui.theme.TechAssistTheme
import com.assanhanil.techassist.presentation.viewmodel.BearingFinderViewModel

/**
 * Main Activity for ASSANHANİL TECH-ASSIST.
 * 
 * Features:
 * - Jetpack Compose UI with Material 3
 * - Navigation between screens
 * - Industrial Dark Mode theme
 */
class MainActivity : ComponentActivity() {

    private val bearingFinderViewModel: BearingFinderViewModel by viewModels {
        BearingFinderViewModel.Factory(
            (application as TechAssistApplication).bearingRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TechAssistTheme {
                TechAssistApp(
                    bearingFinderViewModel = bearingFinderViewModel
                )
            }
        }
    }
}

@Composable
fun TechAssistApp(
    bearingFinderViewModel: BearingFinderViewModel
) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    
    if (showSplash) {
        SplashScreen(
            onSplashComplete = { showSplash = false }
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            
            composable(Screen.BearingFinder.route) {
                val searchResult by bearingFinderViewModel.searchResult.collectAsState()
                
                BearingFinderScreen(
                    onSearchBearing = { id, od, width, tolerance ->
                        bearingFinderViewModel.searchBearing(id, od, width, tolerance)
                    },
                    searchResult = searchResult
                )
            }
            
            // Placeholder screens for other features
            composable(Screen.ElectricalWizard.route) {
                PlaceholderScreen(title = "Electrical Wizard", subtitle = "Coming Soon")
            }
            
            composable(Screen.Reports.route) {
                PlaceholderScreen(title = "Reports", subtitle = "Excel Report Generator")
            }
            
            composable(Screen.Recipes.route) {
                PlaceholderScreen(title = "Master Recipes", subtitle = "Maintenance Templates")
            }
            
            composable(Screen.Camera.route) {
                PlaceholderScreen(title = "Smart Camera", subtitle = "Photo Capture & Annotation")
            }
            
            composable(Screen.Settings.route) {
                PlaceholderScreen(title = "Settings", subtitle = "App Configuration")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TechAssistColors.Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = TechAssistColors.TextSecondary
            )
        }
    }
}
