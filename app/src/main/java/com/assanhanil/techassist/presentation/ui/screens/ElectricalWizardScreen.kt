package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Electrical Wizard Screen - Electrical calculations and cable sizing.
 * 
 * Features:
 * - Cable sizing calculator
 * - Voltage drop calculator
 * - Power factor calculations
 * - Ohm's Law calculator
 */
@Composable
fun ElectricalWizardScreen(
    modifier: Modifier = Modifier
) {
    var selectedCalculator by remember { mutableStateOf(0) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Elektrik Sihirbazı",
            style = MaterialTheme.typography.headlineSmall,
            color = TechAssistColors.Primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Elektrik hesaplamaları ve kablo boyutlandırma",
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Calculator Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorTab(
                title = "Ohm",
                isSelected = selectedCalculator == 0,
                onClick = { selectedCalculator = 0 },
                modifier = Modifier.weight(1f)
            )
            CalculatorTab(
                title = "Güç",
                isSelected = selectedCalculator == 1,
                onClick = { selectedCalculator = 1 },
                modifier = Modifier.weight(1f)
            )
            CalculatorTab(
                title = "Kablo",
                isSelected = selectedCalculator == 2,
                onClick = { selectedCalculator = 2 },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Calculator Content
        when (selectedCalculator) {
            0 -> OhmLawCalculator()
            1 -> PowerCalculator()
            2 -> CableSizeCalculator()
        }
    }
}

@Composable
private fun CalculatorTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        TechAssistColors.Primary
    } else {
        TechAssistColors.Surface
    }
    
    val textColor = if (isSelected) {
        TechAssistColors.Background
    } else {
        TechAssistColors.TextSecondary
    }
    
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Ohm's Law Calculator: V = I * R
 */
@Composable
private fun OhmLawCalculator() {
    var voltage by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var resistance by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    
    NeonCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ohm Yasası Hesaplayıcı",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "V = I × R formülü ile hesaplama yapın. İki değer girin, üçüncüsü hesaplanacak.",
                style = MaterialTheme.typography.bodySmall,
                color = TechAssistColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voltage Input
            OutlinedTextField(
                value = voltage,
                onValueChange = { voltage = it },
                label = { Text("Gerilim (V)") },
                placeholder = { Text("Volt") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("V", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Current Input
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Akım (I)") },
                placeholder = { Text("Amper") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("A", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Resistance Input
            OutlinedTextField(
                value = resistance,
                onValueChange = { resistance = it },
                label = { Text("Direnç (R)") },
                placeholder = { Text("Ohm") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("Ω", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val v = voltage.toDoubleOrNull()
                    val i = current.toDoubleOrNull()
                    val r = resistance.toDoubleOrNull()
                    
                    result = when {
                        v != null && i != null && r == null -> {
                            val calculatedR = v / i
                            resistance = String.format("%.4f", calculatedR)
                            "Direnç: ${String.format("%.4f", calculatedR)} Ω"
                        }
                        v != null && r != null && i == null -> {
                            val calculatedI = v / r
                            current = String.format("%.4f", calculatedI)
                            "Akım: ${String.format("%.4f", calculatedI)} A"
                        }
                        i != null && r != null && v == null -> {
                            val calculatedV = i * r
                            voltage = String.format("%.4f", calculatedV)
                            "Gerilim: ${String.format("%.4f", calculatedV)} V"
                        }
                        else -> "Lütfen tam olarak 2 değer girin"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechAssistColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hesapla", fontWeight = FontWeight.Bold)
            }
            
            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = TechAssistColors.Secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Power Calculator: P = V * I, P = I² * R, P = V² / R
 */
@Composable
private fun PowerCalculator() {
    var voltage by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var power by remember { mutableStateOf("") }
    var powerFactor by remember { mutableStateOf("1.0") }
    var result by remember { mutableStateOf<String?>(null) }
    
    NeonCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Güç Hesaplayıcı",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "P = V × I × cos(φ) formülü ile güç hesaplama",
                style = MaterialTheme.typography.bodySmall,
                color = TechAssistColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voltage Input
            OutlinedTextField(
                value = voltage,
                onValueChange = { voltage = it },
                label = { Text("Gerilim (V)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("V", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Current Input
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Akım (I)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("A", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Power Factor Input
            OutlinedTextField(
                value = powerFactor,
                onValueChange = { powerFactor = it },
                label = { Text("Güç Faktörü (cos φ)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val v = voltage.toDoubleOrNull()
                    val i = current.toDoubleOrNull()
                    val pf = powerFactor.toDoubleOrNull() ?: 1.0
                    
                    result = if (v != null && i != null) {
                        val apparentPower = v * i
                        val activePower = apparentPower * pf
                        val reactivePower = apparentPower * sqrt(1 - pf.pow(2))
                        
                        """
                        Görünür Güç (S): ${String.format("%.2f", apparentPower)} VA
                        Aktif Güç (P): ${String.format("%.2f", activePower)} W
                        Reaktif Güç (Q): ${String.format("%.2f", reactivePower)} VAR
                        """.trimIndent()
                    } else {
                        "Lütfen gerilim ve akım değerlerini girin"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechAssistColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hesapla", fontWeight = FontWeight.Bold)
            }
            
            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechAssistColors.Secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Cable Size Calculator based on current capacity
 */
@Composable
private fun CableSizeCalculator() {
    var current by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("380") }
    var length by remember { mutableStateOf("") }
    var maxVoltageDrop by remember { mutableStateOf("3") }
    var result by remember { mutableStateOf<String?>(null) }
    
    // Standard cable sizes (mm²) and their current capacities (A) for copper
    val cableSizes = listOf(
        1.5 to 16,
        2.5 to 21,
        4.0 to 28,
        6.0 to 36,
        10.0 to 50,
        16.0 to 66,
        25.0 to 84,
        35.0 to 104,
        50.0 to 125,
        70.0 to 160,
        95.0 to 194,
        120.0 to 225,
        150.0 to 260,
        185.0 to 297,
        240.0 to 346
    )
    
    NeonCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kablo Kesit Hesaplayıcı",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Akım taşıma kapasitesi ve gerilim düşümüne göre kablo kesiti hesapla",
                style = MaterialTheme.typography.bodySmall,
                color = TechAssistColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current Input
            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Yük Akımı") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("A", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Voltage Input
            OutlinedTextField(
                value = voltage,
                onValueChange = { voltage = it },
                label = { Text("Sistem Gerilimi") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("V", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Length Input
            OutlinedTextField(
                value = length,
                onValueChange = { length = it },
                label = { Text("Kablo Uzunluğu") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("m", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Max Voltage Drop Input
            OutlinedTextField(
                value = maxVoltageDrop,
                onValueChange = { maxVoltageDrop = it },
                label = { Text("Max Gerilim Düşümü") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("%", color = TechAssistColors.TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val i = current.toDoubleOrNull()
                    val v = voltage.toDoubleOrNull()
                    val l = length.toDoubleOrNull()
                    val maxVd = maxVoltageDrop.toDoubleOrNull()
                    
                    result = if (i != null && v != null && l != null && maxVd != null) {
                        // Find suitable cable based on current capacity
                        val suitableCable = cableSizes.firstOrNull { (_, capacity) -> capacity >= i }
                        
                        if (suitableCable != null) {
                            val (size, capacity) = suitableCable
                            
                            // Calculate voltage drop for this cable
                            // ΔV = (2 * L * I * ρ) / S where ρ = 0.0175 for copper
                            val rho = 0.0175 // Copper resistivity
                            val voltageDrop = (2 * l * i * rho) / size
                            val voltageDropPercent = (voltageDrop / v) * 100
                            
                            """
                            Önerilen Kablo: ${size} mm²
                            Akım Kapasitesi: ${capacity} A
                            Gerilim Düşümü: ${String.format("%.2f", voltageDrop)} V (${String.format("%.2f", voltageDropPercent)}%)
                            Durum: ${if (voltageDropPercent <= maxVd) "✓ Uygun" else "✗ Gerilim düşümü yüksek, daha kalın kesit gerekli"}
                            """.trimIndent()
                        } else {
                            "Bu akım için standart kablo kesiti bulunamadı. Paralel kablo kullanımı gerekebilir."
                        }
                    } else {
                        "Lütfen tüm değerleri girin"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechAssistColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hesapla", fontWeight = FontWeight.Bold)
            }
            
            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechAssistColors.Secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
