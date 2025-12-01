package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2500L

/**
 * Animated Splash Screen for ASSANHANİL BURSA.
 * 
 * Features:
 * - Animated logo with scale and fade effects
 * - Tagline "Operational Reporting System"
 * - Dynamic theme support
 * - Neon Blue accents
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit = {}
) {
    val themeColors = LocalThemeColors.current
    
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    
    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "logoScale"
    )
    
    // Logo alpha animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearEasing
        ),
        label = "logoAlpha"
    )
    
    // Tagline alpha animation (delayed)
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 500,
            easing = LinearEasing
        ),
        label = "taglineAlpha"
    )
    
    // Neon glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Start animation and navigate after delay
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(SPLASH_DURATION_MS)
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Main Logo Text with Neon Glow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // Glow layer (behind)
                Text(
                    text = "ASSAN HANAİL",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        letterSpacing = 4.sp
                    ),
                    color = themeColors.primary.copy(alpha = glowAlpha * 0.3f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                )
                
                // Main text
                Text(
                    text = "ASSAN HANAİL",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        letterSpacing = 4.sp
                    ),
                    color = themeColors.primary,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // BURSA BAKIM subtitle
            Text(
                text = "BURSA BAKIM",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 8.sp
                ),
                color = themeColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider line with gradient
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp)
                    .alpha(taglineAlpha)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                themeColors.primary.copy(alpha = 0f),
                                themeColors.primary,
                                themeColors.primary.copy(alpha = 0f)
                            )
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tagline
            Text(
                text = "Operasyonel Raporlama Sistemi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp
                ),
                color = themeColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tech-Assist subtitle
            Text(
                text = "TECH-ASSIST",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp
                ),
                color = themeColors.primary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
        
        // Bottom branding
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha)
        ) {
            Text(
                text = "Endüstriyel Mühendislik Platformu",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textDisabled,
                textAlign = TextAlign.Center
            )
        }
    }
}
