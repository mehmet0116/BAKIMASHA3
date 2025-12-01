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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.data.preferences.ThemePreferences
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.presentation.viewmodel.OperatorViewModel

/**
 * Settings Screen - Application configuration.
 * 
 * Features:
 * - User profile settings
 * - Operator management (add/remove operators)
 * - Theme settings (Dark/Light mode)
 * - Notification preferences
 * - Data management
 * - About section
 */
@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences,
    isDarkMode: Boolean,
    operatorViewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val useSystemTheme by themePreferences.useSystemTheme.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAddOperatorDialog by remember { mutableStateOf(false) }
    var showDeleteOperatorDialog by remember { mutableStateOf<Operator?>(null) }
    
    // Collect operators from ViewModel
    val operators by operatorViewModel.operators.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineSmall,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Uygulama yapılandırması",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Operators Section
        SettingsSectionHeader(title = "Operatörler")
        
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kayıtlı Operatörler",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    FilledTonalButton(
                        onClick = { showAddOperatorDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = themeColors.primary.copy(alpha = 0.2f),
                            contentColor = themeColors.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekle")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (operators.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = themeColors.textDisabled,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Henüz operatör eklenmedi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.textDisabled
                        )
                    }
                } else {
                    operators.forEach { operator ->
                        OperatorItem(
                            operator = operator,
                            onDelete = { showDeleteOperatorDialog = operator }
                        )
                        if (operator != operators.last()) {
                            Divider(
                                color = themeColors.glassBorder.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Operatörler genel kontrol raporlarında kullanılacaktır",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textDisabled
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Theme Section
        SettingsSectionHeader(title = "Tema Ayarları")
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsToggleItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Sistem Temasını Kullan",
                    subtitle = "Cihazın tema ayarını takip et",
                    checked = useSystemTheme,
                    onCheckedChange = { 
                        themePreferences.setUseSystemThemePreference(it)
                    }
                )
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
                SettingsToggleItem(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Karanlık Mod",
                    subtitle = if (isDarkMode) "AMOLED optimizasyonu açık" else "Aydınlık tema aktif",
                    checked = isDarkMode,
                    onCheckedChange = { 
                        if (!useSystemTheme) {
                            themePreferences.setDarkModePreference(it)
                        }
                    },
                    enabled = !useSystemTheme
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
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    subtitle = "Bakım hatırlatıcıları",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
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
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.Restore,
                    title = "Yedeği Geri Yükle",
                    subtitle = "Yedekten verileri geri yükle",
                    onClick = { /* Restore functionality */ }
                )
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
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
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
                SettingsClickableItem(
                    icon = Icons.Default.Policy,
                    title = "Gizlilik Politikası",
                    subtitle = "Veri kullanım politikası",
                    onClick = { /* Open privacy policy */ }
                )
                
                Divider(color = themeColors.glassBorder.copy(alpha = 0.3f))
                
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
            color = themeColors.textDisabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Assan Hanil Bursa",
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.textDisabled,
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
                        color = themeColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TECH-ASSIST",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.textSecondary
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
                        color = themeColors.textSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Endüstriyel bakım ve raporlama işlemlerini dijitalleştiren, offline-first mimariye sahip profesyonel bir platformdur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textDisabled
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary
                    )
                ) {
                    Text("Tamam")
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Tüm Verileri Sil",
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu işlem tüm raporları, tarifleri ve ayarları silecektir. Bu işlem geri alınamaz!",
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Clear all data
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Add Operator Dialog
    if (showAddOperatorDialog) {
        AddOperatorDialog(
            onDismiss = { showAddOperatorDialog = false },
            onAdd = { name, department ->
                operatorViewModel.addOperator(name, department) {
                    showAddOperatorDialog = false
                }
            }
        )
    }
    
    // Delete Operator Confirmation Dialog
    showDeleteOperatorDialog?.let { operator ->
        AlertDialog(
            onDismissRequest = { showDeleteOperatorDialog = null },
            title = {
                Text(
                    text = "Operatörü Sil",
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "\"${operator.name}\" operatörünü silmek istediğinizden emin misiniz?",
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        operatorViewModel.deleteOperator(operator.id) {
                            showDeleteOperatorDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteOperatorDialog = null }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val themeColors = LocalThemeColors.current
    
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = themeColors.primary,
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
    val themeColors = LocalThemeColors.current
    
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
            tint = if (enabled) themeColors.primary else themeColors.textDisabled,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) themeColors.textPrimary else themeColors.textDisabled,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = themeColors.primary,
                checkedTrackColor = themeColors.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = themeColors.textSecondary,
                uncheckedTrackColor = themeColors.surfaceVariant
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
    val themeColors = LocalThemeColors.current
    
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
            tint = if (isDestructive) themeColors.error else themeColors.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) themeColors.error else themeColors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.textSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = themeColors.textDisabled
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val themeColors = LocalThemeColors.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OperatorItem(
    operator: Operator,
    onDelete: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = themeColors.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = operator.name,
                style = MaterialTheme.typography.bodyLarge,
                color = themeColors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            if (operator.department.isNotEmpty()) {
                Text(
                    text = operator.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textSecondary
                )
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Sil",
                tint = themeColors.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AddOperatorDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni Operatör Ekle",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Operatör Adı") },
                    placeholder = { Text("Ad Soyad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = themeColors.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Departman (Opsiyonel)") },
                    placeholder = { Text("Örn: Bakım") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = themeColors.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), department.trim())
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = themeColors.textSecondary)
            }
        },
        containerColor = themeColors.surface
    )
}
