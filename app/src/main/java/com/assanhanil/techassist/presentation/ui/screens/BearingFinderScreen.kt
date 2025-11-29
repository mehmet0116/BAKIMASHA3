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
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors

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
                text = "Bearing Dimensions",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Technical drawing representation
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechAssistColors.SurfaceVariant)
                    .border(
                        width = 2.dp,
                        color = TechAssistColors.Primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(TechAssistColors.CorporateGray)
                        .border(
                            width = 3.dp,
                            color = TechAssistColors.Primary,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner ring
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(TechAssistColors.Surface)
                            .border(
                                width = 2.dp,
                                color = TechAssistColors.Secondary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center hole
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(TechAssistColors.Background)
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
                DimensionLegend(color = TechAssistColors.Primary, label = "OD - Outer Diameter")
                Spacer(modifier = Modifier.height(4.dp))
                DimensionLegend(color = TechAssistColors.Secondary, label = "ID - Inner Diameter")
                Spacer(modifier = Modifier.height(4.dp))
                DimensionLegend(color = TechAssistColors.TextSecondary, label = "W - Width")
            }
        }
    }
}

@Composable
private fun DimensionLegend(color: Color, label: String) {
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
            color = TechAssistColors.TextSecondary
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
            text = "Visual Bearing Finder",
            style = MaterialTheme.typography.headlineSmall,
            color = TechAssistColors.Primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter measured dimensions to find the ISO code",
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextSecondary
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
                    label = "Inner Diameter (ID)",
                    placeholder = "e.g., 20.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Outer Diameter
                DimensionInputField(
                    value = outerDiameter,
                    onValueChange = { outerDiameter = it },
                    label = "Outer Diameter (OD)",
                    placeholder = "e.g., 47.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Width
                DimensionInputField(
                    value = width,
                    onValueChange = { width = it },
                    label = "Width (W)",
                    placeholder = "e.g., 14.0"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tolerance
                DimensionInputField(
                    value = tolerance,
                    onValueChange = { tolerance = it },
                    label = "Tolerance (±mm)",
                    placeholder = "0.5"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Search button
                Button(
                    onClick = {
                        val id = innerDiameter.toDoubleOrNull() ?: 0.0
                        val od = outerDiameter.toDoubleOrNull() ?: 0.0
                        val w = width.toDoubleOrNull() ?: 0.0
                        val tol = tolerance.toDoubleOrNull() ?: 0.5
                        onSearchBearing(id, od, w, tol)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechAssistColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Find Bearing",
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = TechAssistColors.TextDisabled) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TechAssistColors.Primary,
            unfocusedBorderColor = TechAssistColors.GlassBorder,
            focusedLabelColor = TechAssistColors.Primary,
            unfocusedLabelColor = TechAssistColors.TextSecondary,
            cursorColor = TechAssistColors.Primary
        ),
        suffix = { Text("mm", color = TechAssistColors.TextSecondary) }
    )
}

@Composable
private fun SearchResultCard(result: BearingSearchResult) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Search Results",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.Primary,
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
                        color = TechAssistColors.Warning
                    )
                }
                is BearingSearchResult.Error -> {
                    Text(
                        text = "Error: ${result.exception.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechAssistColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun BearingResultItem(bearing: Bearing) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TechAssistColors.Surface
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
                    color = TechAssistColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = bearing.sealType,
                    style = MaterialTheme.typography.labelMedium,
                    color = TechAssistColors.Secondary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = bearing.type,
                style = MaterialTheme.typography.bodyMedium,
                color = TechAssistColors.TextSecondary
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TechAssistColors.TextDisabled
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
