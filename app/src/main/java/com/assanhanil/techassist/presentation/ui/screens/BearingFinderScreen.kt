package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.domain.model.Bearing
import com.assanhanil.techassist.domain.model.BearingSearchResult
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.components.SplitViewLayout
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors

/**
 * Visual Bearing Finder Screen.
 * 
 * Features:
 * - Show a technical drawing of a bearing
 * - User inputs measured dimensions (ID, OD, Width)
 * - App queries offline DB and finds the ISO Code (e.g., "6204-ZZ")
 * - Adaptive Split-View layout in landscape
 */
@Composable
fun BearingFinderScreen(
    onSearchBearing: (Double, Double, Double, Double) -> Unit = { _, _, _, _ -> },
    searchResult: BearingSearchResult? = null,
    modifier: Modifier = Modifier
) {
    SplitViewLayout(
        menuContent = {
            BearingDiagram(modifier = Modifier.fillMaxSize())
        },
        workspaceContent = {
            BearingFinderWorkspace(
                onSearchBearing = onSearchBearing,
                searchResult = searchResult,
                modifier = Modifier.fillMaxSize()
            )
        },
        modifier = modifier
    )
}

/**
 * Technical bearing diagram display.
 * Shows a visual representation of bearing dimensions.
 */
@Composable
fun BearingDiagram(
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    
    GlassCard(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rulman Boyutları",
                style = MaterialTheme.typography.titleMedium,
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Technical drawing representation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = themeColors.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(themeColors.corporateGray)
                        .border(
                            width = 3.dp,
                            color = themeColors.primary,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner ring
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(themeColors.surface)
                            .border(
                                width = 2.dp,
                                color = themeColors.secondary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center hole
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(themeColors.background)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp)
            ) {
                DimensionLegend(color = themeColors.primary, label = "OD - Dış Çap")
                Spacer(modifier = Modifier.height(4.dp))
                DimensionLegend(color = themeColors.secondary, label = "ID - İç Çap")
                Spacer(modifier = Modifier.height(4.dp))
                DimensionLegend(color = themeColors.textSecondary, label = "W - Genişlik")
            }
        }
    }
}

@Composable
private fun DimensionLegend(color: Color, label: String) {
    val themeColors = LocalThemeColors.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.textSecondary
        )
    }
}

/**
 * Bearing finder workspace with input fields and results.
 */
@Composable
fun BearingFinderWorkspace(
    onSearchBearing: (Double, Double, Double, Double) -> Unit,
    searchResult: BearingSearchResult?,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    
    var innerDiameter by remember { mutableStateOf("") }
    var outerDiameter by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var tolerance by remember { mutableStateOf("0.5") }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Görsel Rulman Bulucu",
            style = MaterialTheme.typography.headlineSmall,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "ISO kodunu bulmak için ölçülen boyutları girin",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Input fields
        NeonCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Inner Diameter
                DimensionInputField(
                    value = innerDiameter,
                    onValueChange = { innerDiameter = it },
                    label = "İç Çap (ID)",
                    placeholder = "örn., 20.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Outer Diameter
                DimensionInputField(
                    value = outerDiameter,
                    onValueChange = { outerDiameter = it },
                    label = "Dış Çap (OD)",
                    placeholder = "örn., 47.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Width
                DimensionInputField(
                    value = width,
                    onValueChange = { width = it },
                    label = "Genişlik (W)",
                    placeholder = "örn., 14.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tolerance
                DimensionInputField(
                    value = tolerance,
                    onValueChange = { tolerance = it },
                    label = "Tolerans (±mm)",
                    placeholder = "0.5"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Validation helper
                val isValidInput = innerDiameter.toDoubleOrNull()?.let { it > 0 } == true &&
                        outerDiameter.toDoubleOrNull()?.let { it > 0 } == true &&
                        width.toDoubleOrNull()?.let { it > 0 } == true &&
                        tolerance.toDoubleOrNull()?.let { it > 0 } == true &&
                        (innerDiameter.toDoubleOrNull() ?: 0.0) < (outerDiameter.toDoubleOrNull() ?: 0.0)
                
                // Search button
                Button(
                    onClick = {
                        val id = innerDiameter.toDoubleOrNull() ?: return@Button
                        val od = outerDiameter.toDoubleOrNull() ?: return@Button
                        val w = width.toDoubleOrNull() ?: return@Button
                        val tol = tolerance.toDoubleOrNull() ?: 0.5
                        onSearchBearing(id, od, w, tol)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValidInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary,
                        disabledContainerColor = themeColors.primary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rulman Bul",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Results
        searchResult?.let { result ->
            SearchResultCard(result = result)
        }
    }
}

@Composable
private fun DimensionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    val themeColors = LocalThemeColors.current
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = themeColors.textDisabled) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColors.primary,
            unfocusedBorderColor = themeColors.glassBorder,
            focusedLabelColor = themeColors.primary,
            unfocusedLabelColor = themeColors.textSecondary,
            cursorColor = themeColors.primary
        ),
        suffix = { Text("mm", color = themeColors.textSecondary) }
    )
}

@Composable
private fun SearchResultCard(result: BearingSearchResult) {
    val themeColors = LocalThemeColors.current
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Arama Sonuçları",
                style = MaterialTheme.typography.titleMedium,
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when (result) {
                is BearingSearchResult.Found -> {
                    result.bearings.forEach { bearing ->
                        BearingResultItem(bearing = bearing)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                is BearingSearchResult.NotFound -> {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.warning
                    )
                }
                is BearingSearchResult.Error -> {
                    Text(
                        text = "Hata: ${result.exception.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BearingResultItem(bearing: Bearing) {
    val themeColors = LocalThemeColors.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bearing.isoCode,
                    style = MaterialTheme.typography.titleLarge,
                    color = themeColors.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = bearing.sealType,
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = bearing.type,
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.textSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DimensionChip(label = "ID", value = "${bearing.innerDiameter}mm")
                DimensionChip(label = "OD", value = "${bearing.outerDiameter}mm")
                DimensionChip(label = "W", value = "${bearing.width}mm")
            }
        }
    }
}

@Composable
private fun DimensionChip(label: String, value: String) {
    val themeColors = LocalThemeColors.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = themeColors.textDisabled
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
