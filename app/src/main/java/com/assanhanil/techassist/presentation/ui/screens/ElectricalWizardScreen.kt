package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Electrical Wizard Screen - Comprehensive electrical calculations and engineering tools.
 * 
 * Features:
 * - Ohm's Law Calculator (V = I × R)
 * - Power Calculator (P, Q, S calculations with power factor)
 * - Cable Sizing Calculator (current capacity & voltage drop)
 * - Motor Current Calculator (single/three phase motors)
 * - Transformer Calculator (voltage/current transformations)
 * - Capacitor Calculator (power factor correction)
 * - Short Circuit Calculator (fault current analysis)
 * - Circuit Breaker/Fuse Selector
 * - Motor Starting Calculator (DOL, Star-Delta, VFD)
 * - Energy Consumption Calculator (kWh & cost analysis)
 * - Earthing/Grounding Calculator
 * - Lighting Calculator (lux calculations)
 */
@Composable
fun ElectricalWizardScreen(
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    var selectedCalculator by remember { mutableStateOf(0) }
    
    val calculatorTabs = listOf(
        "Ohm Yasası",
        "Güç",
        "Kablo",
        "Motor Akım",
        "Trafo",
        "Kapasitör",
        "Kısa Devre",
        "Sigorta",
        "Yol Verme",
        "Enerji",
        "Topraklama",
        "Aydınlatma"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "⚡ Elektrik Sihirbazı",
            style = MaterialTheme.typography.headlineSmall,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Kapsamlı elektrik hesaplamaları ve mühendislik araçları",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calculator Selection - Horizontal scrollable tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            calculatorTabs.forEachIndexed { index, title ->
                CalculatorTab(
                    title = title,
                    isSelected = selectedCalculator == index,
                    onClick = { selectedCalculator = index },
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Calculator Content
        when (selectedCalculator) {
            0 -> OhmLawCalculator()
            1 -> PowerCalculator()
            2 -> CableSizeCalculator()
            3 -> MotorCurrentCalculator()
            4 -> TransformerCalculator()
            5 -> CapacitorCalculator()
            6 -> ShortCircuitCalculator()
            7 -> CircuitBreakerSelector()
            8 -> MotorStartingCalculator()
            9 -> EnergyConsumptionCalculator()
            10 -> EarthingCalculator()
            11 -> LightingCalculator()
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
    val themeColors = LocalThemeColors.current
    
    val backgroundColor = if (isSelected) {
        themeColors.primary
    } else {
        themeColors.surface
    }
    
    val textColor = if (isSelected) {
        themeColors.background
    } else {
        themeColors.textSecondary
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
    val themeColors = LocalThemeColors.current
    
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
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "V = I × R formülü ile hesaplama yapın. İki değer girin, üçüncüsü hesaplanacak.",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textSecondary
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
                suffix = { Text("V", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("A", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("Ω", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                    containerColor = themeColors.primary
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
                        color = themeColors.secondary,
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
    val themeColors = LocalThemeColors.current
    
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
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "P = V × I × cos(φ) formülü ile güç hesaplama",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textSecondary
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
                suffix = { Text("V", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("A", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                    containerColor = themeColors.primary
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
                        color = themeColors.secondary,
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
    val themeColors = LocalThemeColors.current
    
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
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Akım taşıma kapasitesi ve gerilim düşümüne göre kablo kesiti hesapla",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textSecondary
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
                suffix = { Text("A", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("V", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("m", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                suffix = { Text("%", color = themeColors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.primary,
                    unfocusedBorderColor = themeColors.glassBorder
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
                        // Find suitable cable based on both current capacity AND voltage drop
                        val rho = 0.0175 // Copper resistivity (Ω·mm²/m)
                        
                        // Find the smallest cable that satisfies BOTH current capacity AND voltage drop
                        val suitableCable = cableSizes.firstOrNull { (size, capacity) ->
                            // Check current capacity
                            val currentOk = capacity >= i
                            
                            // Check voltage drop
                            val voltageDrop = (2 * l * i * rho) / size
                            val voltageDropPercent = (voltageDrop / v) * 100
                            val voltageDropOk = voltageDropPercent <= maxVd
                            
                            currentOk && voltageDropOk
                        }
                        
                        // Also find cable based only on current (for comparison)
                        val currentBasedCable = cableSizes.firstOrNull { (_, capacity) -> capacity >= i }
                        
                        if (suitableCable != null) {
                            val (size, capacity) = suitableCable
                            
                            // Calculate actual voltage drop for selected cable
                            val voltageDrop = (2 * l * i * rho) / size
                            val voltageDropPercent = (voltageDrop / v) * 100
                            
                            """
                            ✓ Önerilen Kablo: ${size} mm²
                            Akım Kapasitesi: ${capacity} A (Yük: ${String.format("%.1f", i)} A)
                            Gerilim Düşümü: ${String.format("%.2f", voltageDrop)} V (${String.format("%.2f", voltageDropPercent)}%)
                            Max İzin Verilen: ${maxVd}%
                            Durum: Uygun - Her iki kriter de karşılanıyor
                            """.trimIndent()
                        } else if (currentBasedCable != null) {
                            // Cable found for current but voltage drop is too high
                            val (size, capacity) = currentBasedCable
                            val voltageDrop = (2 * l * i * rho) / size
                            val voltageDropPercent = (voltageDrop / v) * 100
                            
                            // Suggest larger cable
                            val largerCables = cableSizes.filter { (s, _) -> s > size }
                            val suggestion = if (largerCables.isNotEmpty()) {
                                "Daha kalın kesit deneyin: ${largerCables.first().first} mm² veya üstü"
                            } else {
                                "Paralel kablo kullanımı veya gerilim seviyesini artırma gerekebilir"
                            }
                            
                            """
                            ✗ Akım için: ${size} mm² (${capacity} A) yeterli
                            Ancak gerilim düşümü: ${String.format("%.2f", voltageDropPercent)}% > ${maxVd}%
                            $suggestion
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
                    containerColor = themeColors.primary
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
                        color = themeColors.secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
