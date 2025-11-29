package com.assanhanil.techassist.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
 * Settings Screen - Application configuration.
 * 
 * Features:
 * - User profile settings
 * - Notification preferences
 * - Data management
 * - About section
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var operatorName by remember { mutableStateOf("") }
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineSmall,
            color = TechAssistColors.Primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Uygulama yapılandırması",
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Section
        SettingsSectionHeader(title = "Profil")
        
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = { Text("Operatör Adı") },
                    placeholder = { Text("Adınızı girin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TechAssistColors.Primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechAssistColors.Primary,
                        unfocusedBorderColor = TechAssistColors.GlassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Bu isim raporlarda kullanılacaktır",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextDisabled
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Preferences Section
        SettingsSectionHeader(title = "Tercihler")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsToggleItem(
                    icon = Icons.Default.Save,
                    title = "Otomatik Kaydet",
                    subtitle = "Raporları otomatik kaydet",
                    checked = autoSaveEnabled,
                    onCheckedChange = { autoSaveEnabled = it }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    subtitle = "Bakım hatırlatıcıları",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Karanlık Mod",
                    subtitle = "AMOLED optimizasyonu",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it },
                    enabled = false // Always dark mode for this app
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Data Section
        SettingsSectionHeader(title = "Veri Yönetimi")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.Backup,
                    title = "Verileri Yedekle",
                    subtitle = "Tüm raporları yedekle",
                    onClick = { /* Backup functionality */ }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.Restore,
                    title = "Yedeği Geri Yükle",
                    subtitle = "Yedekten verileri geri yükle",
                    onClick = { /* Restore functionality */ }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Tüm Verileri Sil",
                    subtitle = "Dikkat: Bu işlem geri alınamaz",
                    onClick = { showClearDataDialog = true },
                    isDestructive = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // About Section
        SettingsSectionHeader(title = "Hakkında")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "Uygulama Hakkında",
                    subtitle = "Versiyon 1.0",
                    onClick = { showAboutDialog = true }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.Policy,
                    title = "Gizlilik Politikası",
                    subtitle = "Veri kullanım politikası",
                    onClick = { /* Open privacy policy */ }
                )
                
                HorizontalDivider(color = TechAssistColors.GlassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.Help,
                    title = "Yardım ve Destek",
                    subtitle = "SSS ve iletişim",
                    onClick = { /* Open help */ }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Footer
        Text(
            text = "ASSANHANİL TECH-ASSIST © 2024",
            style = MaterialTheme.typography.bodySmall,
            color = TechAssistColors.TextDisabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Assan Hanil Bursa",
            style = MaterialTheme.typography.bodySmall,
            color = TechAssistColors.TextDisabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ASSANHANİL",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TechAssistColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TECH-ASSIST",
                        style = MaterialTheme.typography.titleMedium,
                        color = TechAssistColors.TextSecondary
                    )
                }
            },
            text = {
                Column {
                    InfoRow(label = "Versiyon", value = "1.0.0")
                    InfoRow(label = "Platform", value = "Android (Kotlin)")
                    InfoRow(label = "UI Framework", value = "Jetpack Compose")
                    InfoRow(label = "Database", value = "Room")
                    InfoRow(label = "Excel Engine", value = "Apache POI")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Dijital Saha Mühendisi Platformu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechAssistColors.TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Endüstriyel bakım ve raporlama işlemlerini dijitalleştiren, offline-first mimariye sahip profesyonel bir platformdur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechAssistColors.TextDisabled
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechAssistColors.Primary
                    )
                ) {
                    Text("Tamam")
                }
            },
            containerColor = TechAssistColors.Surface
        )
    }
    
    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Tüm Verileri Sil",
                    color = TechAssistColors.Error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu işlem tüm raporları, tarifleri ve ayarları silecektir. Bu işlem geri alınamaz!",
                    color = TechAssistColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Clear all data
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechAssistColors.Error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("İptal", color = TechAssistColors.TextSecondary)
                }
            },
            containerColor = TechAssistColors.Surface
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = TechAssistColors.Primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) TechAssistColors.Primary else TechAssistColors.TextDisabled,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) TechAssistColors.TextPrimary else TechAssistColors.TextDisabled,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TechAssistColors.TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TechAssistColors.Primary,
                checkedTrackColor = TechAssistColors.Primary.copy(alpha = 0.5f),
                uncheckedThumbColor = TechAssistColors.TextSecondary,
                uncheckedTrackColor = TechAssistColors.SurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) TechAssistColors.Error else TechAssistColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) TechAssistColors.Error else TechAssistColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TechAssistColors.TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TechAssistColors.TextDisabled
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
