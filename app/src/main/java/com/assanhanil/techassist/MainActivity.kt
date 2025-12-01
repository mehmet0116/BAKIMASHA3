package com.assanhanil.techassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.assanhanil.techassist.data.preferences.ThemePreferences
import com.assanhanil.techassist.presentation.ui.screens.*
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.presentation.ui.theme.TechAssistTheme
import com.assanhanil.techassist.presentation.viewmodel.BearingFinderViewModel
import com.assanhanil.techassist.presentation.viewmodel.ExcelTemplateViewModel
import com.assanhanil.techassist.presentation.viewmodel.MachineControlViewModel
import com.assanhanil.techassist.presentation.viewmodel.OperatorViewModel
import com.assanhanil.techassist.service.ExcelService
import kotlinx.coroutines.launch

/**
 * Main Activity for ASSANHANİL TECH-ASSIST.
 * 
 * Features:
 * - Jetpack Compose UI with Material 3
 * - Hamburger Menu (Drawer Navigation)
 * - Navigation between screens
 * - Dynamic Dark/Light Mode theme support
 */
class MainActivity : ComponentActivity() {

    private val bearingFinderViewModel: BearingFinderViewModel by viewModels {
        BearingFinderViewModel.Factory(
            (application as TechAssistApplication).bearingRepository
        )
    }

    private val excelTemplateViewModel: ExcelTemplateViewModel by viewModels {
        ExcelTemplateViewModel.Factory(
            (application as TechAssistApplication).excelTemplateRepository
        )
    }

    private val machineControlViewModel: MachineControlViewModel by viewModels {
        MachineControlViewModel.Factory(
            (application as TechAssistApplication).machineControlRepository
        )
    }

    private val operatorViewModel: OperatorViewModel by viewModels {
        OperatorViewModel.Factory(
            (application as TechAssistApplication).operatorRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferences = (application as TechAssistApplication).themePreferences
        
        setContent {
            val useSystemTheme by themePreferences.useSystemTheme.collectAsState()
            val isDarkModePreference by themePreferences.isDarkMode.collectAsState()
            val isSystemDarkTheme = isSystemInDarkTheme()
            
            val isDarkTheme = if (useSystemTheme) isSystemDarkTheme else isDarkModePreference
            
            TechAssistTheme(darkTheme = isDarkTheme) {
                TechAssistApp(
                    bearingFinderViewModel = bearingFinderViewModel,
                    excelTemplateViewModel = excelTemplateViewModel,
                    machineControlViewModel = machineControlViewModel,
                    operatorViewModel = operatorViewModel,
                    excelService = ExcelService(this),
                    themePreferences = themePreferences,
                    isDarkMode = isDarkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechAssistApp(
    bearingFinderViewModel: BearingFinderViewModel,
    excelTemplateViewModel: ExcelTemplateViewModel,
    machineControlViewModel: MachineControlViewModel,
    operatorViewModel: OperatorViewModel,
    excelService: ExcelService,
    themePreferences: ThemePreferences,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val themeColors = LocalThemeColors.current
    
    if (showSplash) {
        SplashScreen(
            onSplashComplete = { showSplash = false }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = themeColors.surface,
                    drawerContentColor = themeColors.textPrimary
                ) {
                    DrawerContent(
                        navController = navController,
                        onItemClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    val currentScreen = getScreenFromRoute(currentRoute)
                    
                    TopAppBar(
                        title = {
                            Text(
                                text = currentScreen?.title ?: "ASSAN HANİL BURSA BAKIM",
                                color = themeColors.textPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü",
                                    tint = themeColors.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = themeColors.background
                        )
                    )
                },
                containerColor = themeColors.background
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
                    
                    composable(Screen.ElectricalWizard.route) {
                        ElectricalWizardScreen()
                    }
                    
                    composable(Screen.Reports.route) {
                        ReportsScreen(excelService = excelService)
                    }
                    
                    composable(Screen.Camera.route) {
                        CameraScreen()
                    }
                    
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            themePreferences = themePreferences,
                            isDarkMode = isDarkMode,
                            operatorViewModel = operatorViewModel
                        )
                    }
                    
                    composable(Screen.ExcelTemplateBuilder.route) {
                        ExcelTemplateBuilderScreen(
                            excelService = excelService,
                            excelTemplateViewModel = excelTemplateViewModel
                        )
                    }
                    
                    composable(Screen.GeneralControl.route) {
                        GeneralControlScreen(
                            excelService = excelService,
                            machineControlViewModel = machineControlViewModel,
                            operatorViewModel = operatorViewModel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Drawer content with navigation menu items.
 */
@Composable
fun DrawerContent(
    navController: NavController,
    onItemClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val themeColors = LocalThemeColors.current
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(themeColors.surface)
            .padding(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ASSAN HANİL",
                    style = MaterialTheme.typography.headlineSmall,
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
        
        Divider(color = themeColors.glassBorder)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Menu Items
        val menuItems = listOf(
            Screen.Home,
            Screen.BearingFinder,
            Screen.ElectricalWizard,
            Screen.Reports,
            Screen.ExcelTemplateBuilder,
            Screen.GeneralControl,
            Screen.Camera,
            Screen.Settings
        )
        
        menuItems.forEach { screen ->
            DrawerMenuItem(
                screen = screen,
                isSelected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onItemClick()
                }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        Text(
            text = "Versiyon 1.0",
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.textDisabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DrawerMenuItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    val backgroundColor = if (isSelected) {
        themeColors.primary.copy(alpha = 0.15f)
    } else {
        themeColors.surface
    }
    
    val contentColor = if (isSelected) {
        themeColors.primary
    } else {
        themeColors.textSecondary
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = screen.title,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun getScreenFromRoute(route: String?): Screen? {
    return when (route) {
        Screen.Home.route -> Screen.Home
        Screen.BearingFinder.route -> Screen.BearingFinder
        Screen.ElectricalWizard.route -> Screen.ElectricalWizard
        Screen.Reports.route -> Screen.Reports
        Screen.ExcelTemplateBuilder.route -> Screen.ExcelTemplateBuilder
        Screen.GeneralControl.route -> Screen.GeneralControl
        Screen.Camera.route -> Screen.Camera
        Screen.Settings.route -> Screen.Settings
        else -> null
    }
}
