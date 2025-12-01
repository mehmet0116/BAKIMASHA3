package com.assanhanil.techassist.presentation.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.assanhanil.techassist.domain.model.ControlItemData
import com.assanhanil.techassist.domain.model.MachineControl
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.domain.model.SecurityStatus
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.presentation.viewmodel.MachineControlViewModel
import com.assanhanil.techassist.presentation.viewmodel.OperatorViewModel
import com.assanhanil.techassist.service.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a control item with photo and metadata.
 */
data class ControlItem(
    val id: Int,
    val title: String,
    val notes: String,
    val bitmap: Bitmap,
    val timestamp: Date,
    val status: String,
    val securityStatus: SecurityStatus = SecurityStatus.NOT_SET
)

/**
 * General Control Screen - Enhanced field inspection photo capture with machine management.
 * 
 * Features:
 * - Machine/Title creation and persistence (Başlık oluşturma ve kaydetme)
 * - Operator selection for controls (Operatör seçimi)
 * - Security Control section with photo capture (Güvenlik Kontrol)
 * - Security status options: Active/Inactive (Güvenlik Devrede Aktif / Devrede Değil)
 * - Save/Delete functionality for machines
 * - Merge All button to combine all machine data (Hepsini Birleştir)
 * - Share and Save with user-selectable destinations
 * - Excel export with images and operators embedded
 */
@Composable
fun GeneralControlScreen(
    excelService: ExcelService,
    machineControlViewModel: MachineControlViewModel,
    operatorViewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Collect saved machine controls from database
    val savedMachineControls by machineControlViewModel.machineControls.collectAsState()
    
    // Collect all available operators
    val allOperators by operatorViewModel.operators.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Current machine state
    var currentMachineTitle by remember { mutableStateOf("") }
    var currentMachineId by remember { mutableStateOf(0L) }
    var controlItems by remember { mutableStateOf<List<ControlItem>>(emptyList()) }
    var nextItemId by remember { mutableStateOf(1) }
    
    // Selected operators for current machine
    var selectedOperatorIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // Dialog states
    var showMachineTitleDialog by remember { mutableStateOf(false) }
    var showSecurityControlDialog by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showSaveLocationDialog by remember { mutableStateOf(false) }
    var showSavedMachinesDialog by remember { mutableStateOf(false) }
    var showOperatorSelectionDialog by remember { mutableStateOf(false) }
    
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedItem by remember { mutableStateOf<ControlItem?>(null) }
    var mergedFile by remember { mutableStateOf<File?>(null) }
    var mergedOperatorNames by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Selected machines for merging
    var selectedMachinesForMerge by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            pendingBitmap = it
            showTitleDialog = true
        }
    }
    
    // Security control camera launcher
    val securityCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            pendingBitmap = it
            showSecurityControlDialog = true
        }
    }
    
    // Document create launcher for save location
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            mergedFile?.let { file ->
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        Toast.makeText(context, "Dosya kaydedildi", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Kaydetme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Genel Kontrol",
            style = MaterialTheme.typography.headlineSmall,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (currentMachineTitle.isNotEmpty()) 
                "Aktif Makina: $currentMachineTitle" 
            else 
                "Makina başlığı oluşturun veya mevcut makina seçin",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Machine Title Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Create New Machine Button
            OutlinedButton(
                onClick = { showMachineTitleDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yeni Makina")
            }
            
            // Load Saved Machines Button
            OutlinedButton(
                onClick = { showSavedMachinesDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kayıtlı (${savedMachineControls.size})")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Operators Section - Always visible
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kontrol Yapan Operatörler",
                            style = MaterialTheme.typography.titleMedium,
                            color = themeColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    FilledTonalButton(
                        onClick = { showOperatorSelectionDialog = true },
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
                        Text("Seç")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedOperatorIds.isEmpty()) {
                    Text(
                        text = "Henüz operatör seçilmedi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textDisabled
                    )
                } else {
                    val selectedOperators = allOperators.filter { it.id in selectedOperatorIds }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedOperators.forEach { operator ->
                            AssistChip(
                                onClick = { 
                                    selectedOperatorIds = selectedOperatorIds - operator.id
                                },
                                label = { Text(operator.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Kaldır",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Only show capture options if machine is selected
        if (currentMachineTitle.isNotEmpty()) {
            // Control Photo Capture
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (hasCameraPermission) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(themeColors.primary.copy(alpha = 0.2f))
                            .border(2.dp, themeColors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Fotoğraf Çek",
                            tint = themeColors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kontrol Fotoğrafı Çek",
                            style = MaterialTheme.typography.titleMedium,
                            color = themeColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fotoğraf çek ve not ekle",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Security Control Button
            NeonCard(
                glowColor = themeColors.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (hasCameraPermission) {
                            securityCameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(themeColors.secondary.copy(alpha = 0.2f))
                            .border(2.dp, themeColors.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Güvenlik Kontrolü",
                            tint = themeColors.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Güvenlik Kontrolü",
                            style = MaterialTheme.typography.titleMedium,
                            color = themeColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Güvenlik durumunu fotoğrafla",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kontrol Öğeleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = themeColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                if (controlItems.isNotEmpty()) {
                    Text(
                        text = "${controlItems.size} öğe",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textDisabled
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (controlItems.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = themeColors.textDisabled,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Henüz kontrol yapılmadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = themeColors.textDisabled
                        )
                    }
                }
            } else {
                // Control items list
                controlItems.forEach { item ->
                    ControlItemCard(
                        item = item,
                        onClick = { selectedItem = item },
                        onDelete = {
                            controlItems = controlItems.filter { it.id != item.id }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Machine Button
                Button(
                    onClick = {
                        scope.launch {
                            val controlItemsData = controlItems.map { item ->
                                // Save bitmap to file
                                val imageFile = saveBitmapToFile(context, item.bitmap, "control_${item.id}")
                                ControlItemData(
                                    id = item.id,
                                    title = item.title,
                                    notes = item.notes,
                                    imagePath = imageFile.absolutePath,
                                    timestamp = item.timestamp.time,
                                    status = item.status,
                                    securityStatus = item.securityStatus
                                )
                            }
                            machineControlViewModel.saveMachineControlWithItems(
                                title = currentMachineTitle,
                                description = "",
                                controlItems = controlItemsData,
                                operatorIds = selectedOperatorIds.toList(),
                                existingId = currentMachineId
                            ) {
                                Toast.makeText(context, "Makina kaydedildi: $currentMachineTitle", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = controlItems.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kaydet")
                }
                
                // Delete Machine Button
                OutlinedButton(
                    onClick = {
                        if (currentMachineId > 0) {
                            scope.launch {
                                machineControlViewModel.deactivateMachineControl(currentMachineId) {
                                    Toast.makeText(context, "Makina silindi", Toast.LENGTH_SHORT).show()
                                    currentMachineTitle = ""
                                    currentMachineId = 0
                                    controlItems = emptyList()
                                    nextItemId = 1
                                    selectedOperatorIds = emptySet()
                                }
                            }
                        } else {
                            currentMachineTitle = ""
                            controlItems = emptyList()
                            nextItemId = 1
                            selectedOperatorIds = emptySet()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = themeColors.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sil")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Merge All and Export Buttons (Always visible when there's data)
        if (savedMachineControls.isNotEmpty() || controlItems.isNotEmpty()) {
            Divider(color = themeColors.glassBorder)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Merge All Button
            Button(
                onClick = { showMergeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.MergeType, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hepsini Birleştir")
            }
        }
    }
    
    // Machine Title Dialog
    if (showMachineTitleDialog) {
        MachineTitleDialog(
            onDismiss = { showMachineTitleDialog = false },
            onSave = { title ->
                currentMachineTitle = title
                currentMachineId = 0
                controlItems = emptyList()
                nextItemId = 1
                selectedOperatorIds = emptySet()
                showMachineTitleDialog = false
                Toast.makeText(context, "Makina oluşturuldu: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Saved Machines Dialog
    if (showSavedMachinesDialog) {
        SavedMachinesDialog(
            machines = savedMachineControls,
            onDismiss = { showSavedMachinesDialog = false },
            onSelect = { machine ->
                currentMachineTitle = machine.title
                currentMachineId = machine.id
                // Load operators from saved machine
                selectedOperatorIds = machine.operatorIds.toSet()
                // Load control items from saved machine
                controlItems = machine.controlItems.mapNotNull { data ->
                    val bitmap = loadBitmapFromFile(data.imagePath)
                    bitmap?.let {
                        ControlItem(
                            id = data.id,
                            title = data.title,
                            notes = data.notes,
                            bitmap = it,
                            timestamp = Date(data.timestamp),
                            status = data.status,
                            securityStatus = data.securityStatus
                        )
                    }
                }
                nextItemId = (controlItems.maxOfOrNull { it.id } ?: 0) + 1
                showSavedMachinesDialog = false
            },
            onDelete = { machine ->
                scope.launch {
                    machineControlViewModel.deactivateMachineControl(machine.id) {
                        Toast.makeText(context, "Makina silindi: ${machine.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    
    // Title Input Dialog - shown after taking photo
    if (showTitleDialog && pendingBitmap != null) {
        TitleInputDialog(
            itemNumber = nextItemId,
            onDismiss = { 
                showTitleDialog = false
                pendingBitmap = null
            },
            onSave = { title, notes ->
                val newItem = ControlItem(
                    id = nextItemId,
                    title = title,
                    notes = notes,
                    bitmap = pendingBitmap!!,
                    timestamp = Date(),
                    status = "Kontrol Edildi",
                    securityStatus = SecurityStatus.NOT_SET
                )
                controlItems = controlItems + newItem
                nextItemId++
                showTitleDialog = false
                pendingBitmap = null
                Toast.makeText(context, "Kontrol kaydedildi", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Security Control Dialog
    if (showSecurityControlDialog && pendingBitmap != null) {
        SecurityControlDialog(
            itemNumber = nextItemId,
            onDismiss = { 
                showSecurityControlDialog = false
                pendingBitmap = null
            },
            onSave = { title, notes, securityStatus ->
                val newItem = ControlItem(
                    id = nextItemId,
                    title = title,
                    notes = notes,
                    bitmap = pendingBitmap!!,
                    timestamp = Date(),
                    status = if (securityStatus == SecurityStatus.ACTIVE) "Güvenlik Aktif" else "Güvenlik Devre Dışı",
                    securityStatus = securityStatus
                )
                controlItems = controlItems + newItem
                nextItemId++
                showSecurityControlDialog = false
                pendingBitmap = null
                Toast.makeText(context, "Güvenlik kontrolü kaydedildi", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Item Detail Dialog
    selectedItem?.let { item ->
        ControlItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
    
    // Merge Dialog
    if (showMergeDialog) {
        MergeDialog(
            machines = savedMachineControls,
            allOperators = allOperators,
            currentMachineTitle = currentMachineTitle,
            currentControlItems = controlItems,
            currentOperatorIds = selectedOperatorIds,
            selectedMachines = selectedMachinesForMerge,
            onSelectionChange = { selectedMachinesForMerge = it },
            onDismiss = { showMergeDialog = false },
            onMerge = { includeCurrentMachine ->
                scope.launch {
                    try {
                        val machinesToMerge = savedMachineControls.filter { it.id in selectedMachinesForMerge }
                        val allItems = mutableListOf<Pair<String, ControlItem>>()
                        val allOperatorIds = mutableSetOf<Long>()
                        
                        // Add current machine items and operators if selected
                        if (includeCurrentMachine && controlItems.isNotEmpty()) {
                            controlItems.forEach { item ->
                                allItems.add(currentMachineTitle to item)
                            }
                            allOperatorIds.addAll(selectedOperatorIds)
                        }
                        
                        // Add saved machine items and operators
                        machinesToMerge.forEach { machine ->
                            allOperatorIds.addAll(machine.operatorIds)
                            machine.controlItems.forEach { data ->
                                val bitmap = loadBitmapFromFile(data.imagePath)
                                bitmap?.let {
                                    val item = ControlItem(
                                        id = data.id,
                                        title = data.title,
                                        notes = data.notes,
                                        bitmap = it,
                                        timestamp = Date(data.timestamp),
                                        status = data.status,
                                        securityStatus = data.securityStatus
                                    )
                                    allItems.add(machine.title to item)
                                }
                            }
                        }
                        
                        // Get operator names for Excel export
                        val operatorNames = allOperators
                            .filter { it.id in allOperatorIds }
                            .map { it.name }
                        mergedOperatorNames = operatorNames
                        
                        if (allItems.isNotEmpty()) {
                            val file = exportMergedToExcel(context, excelService, allItems, operatorNames)
                            mergedFile = file
                            showMergeDialog = false
                            showSaveLocationDialog = true
                        } else {
                            Toast.makeText(context, "Birleştirilecek veri bulunamadı", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Birleştirme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    // Save/Share Location Dialog
    if (showSaveLocationDialog && mergedFile != null) {
        SaveShareDialog(
            onDismiss = { showSaveLocationDialog = false },
            onSave = {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                createDocumentLauncher.launch("GenelKontrol_Birlesik_$timestamp.xlsx")
            },
            onShare = {
                mergedFile?.let { file ->
                    shareExcelFile(context, file)
                }
                showSaveLocationDialog = false
            }
        )
    }
    
    // Operator Selection Dialog
    if (showOperatorSelectionDialog) {
        OperatorSelectionDialog(
            allOperators = allOperators,
            selectedOperatorIds = selectedOperatorIds,
            onSelectionChange = { selectedOperatorIds = it },
            onDismiss = { showOperatorSelectionDialog = false }
        )
    }
}

// ===== Control Item Card =====

@Composable
private fun ControlItemCard(
    item: ControlItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Image(
                bitmap = item.bitmap.asImageBitmap(),
                contentDescription = "Kontrol Fotoğrafı",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = themeColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textSecondary
                )
                
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textSecondary,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Status badge with security indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusIcon = if (item.securityStatus != SecurityStatus.NOT_SET) 
                        Icons.Default.Security else Icons.Default.CheckCircle
                    val statusColor = when (item.securityStatus) {
                        SecurityStatus.ACTIVE -> themeColors.secondary
                        SecurityStatus.INACTIVE -> themeColors.error
                        else -> themeColors.secondary
                    }
                    
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = themeColors.error
                )
            }
        }
    }
}

// ===== Dialogs =====

@Composable
private fun MachineTitleDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni Makina Oluştur",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Makina/başlık adını girin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Makina Adı") },
                    placeholder = { Text("Örn: Pres Makinası 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Text("Oluştur")
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

@Composable
private fun SavedMachinesDialog(
    machines: List<MachineControl>,
    onDismiss: () -> Unit,
    onSelect: (MachineControl) -> Unit,
    onDelete: (MachineControl) -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kayıtlı Makinalar",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (machines.isEmpty()) {
                Text(
                    text = "Kayıtlı makina bulunamadı",
                    color = themeColors.textSecondary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    machines.forEach { machine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(machine) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = machine.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = themeColors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${machine.controlItems.size} kontrol • ${
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(Date(machine.updatedAt))
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.textSecondary
                                )
                            }
                            
                            IconButton(onClick = { onDelete(machine) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = themeColors.error
                                )
                            }
                        }
                        Divider(color = themeColors.glassBorder)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = themeColors.textSecondary)
            }
        },
        containerColor = themeColors.surface
    )
}

@Composable
private fun TitleInputDialog(
    itemNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kontrol Başlığı",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Kontrol edilen öğe için başlık ve not girin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    placeholder = { Text("Örn: Motor Yağı Kontrolü") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar") },
                    placeholder = { Text("Ek açıklamalar, bulgular...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { "Kontrol #$itemNumber" }, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityControlDialog(
    itemNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, SecurityStatus) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var securityStatus by remember { mutableStateOf(SecurityStatus.ACTIVE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Güvenlik Kontrolü",
                color = themeColors.secondary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Güvenlik kontrol bilgilerini girin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    placeholder = { Text("Örn: Acil Stop Kontrolü") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.secondary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar") },
                    placeholder = { Text("Ek açıklamalar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.secondary,
                        unfocusedBorderColor = themeColors.glassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Güvenlik Durumu:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Security Status Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Button
                    FilterChip(
                        selected = securityStatus == SecurityStatus.ACTIVE,
                        onClick = { securityStatus = SecurityStatus.ACTIVE },
                        label = { Text("Güvenlik Devrede Aktif") },
                        leadingIcon = if (securityStatus == SecurityStatus.ACTIVE) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.secondary.copy(alpha = 0.2f),
                            selectedLabelColor = themeColors.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Inactive Button
                    FilterChip(
                        selected = securityStatus == SecurityStatus.INACTIVE,
                        onClick = { securityStatus = SecurityStatus.INACTIVE },
                        label = { Text("Devrede Değil") },
                        leadingIcon = if (securityStatus == SecurityStatus.INACTIVE) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.error.copy(alpha = 0.2f),
                            selectedLabelColor = themeColors.error
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { "Güvenlik Kontrolü #$itemNumber" }, notes, securityStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondary)
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
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

@Composable
private fun ControlItemDetailDialog(
    item: ControlItem,
    onDismiss: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = item.title,
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Image(
                    bitmap = item.bitmap.asImageBitmap(),
                    contentDescription = "Kontrol Fotoğrafı",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = themeColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            .format(item.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusIcon = if (item.securityStatus != SecurityStatus.NOT_SET) 
                        Icons.Default.Security else Icons.Default.CheckCircle
                    val statusColor = when (item.securityStatus) {
                        SecurityStatus.ACTIVE -> themeColors.secondary
                        SecurityStatus.INACTIVE -> themeColors.error
                        else -> themeColors.secondary
                    }
                    
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notlar:",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Text("Kapat")
            }
        },
        containerColor = themeColors.surface
    )
}

@Composable
private fun MergeDialog(
    machines: List<MachineControl>,
    allOperators: List<Operator>,
    currentMachineTitle: String,
    currentControlItems: List<ControlItem>,
    currentOperatorIds: Set<Long>,
    selectedMachines: Set<Long>,
    onSelectionChange: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    onMerge: (Boolean) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var includeCurrentMachine by remember { mutableStateOf(currentControlItems.isNotEmpty()) }
    
    // Calculate all operators involved in selected machines
    val allSelectedOperatorIds = remember(selectedMachines, includeCurrentMachine, currentOperatorIds) {
        val ids = mutableSetOf<Long>()
        if (includeCurrentMachine) {
            ids.addAll(currentOperatorIds)
        }
        machines.filter { it.id in selectedMachines }.forEach { machine ->
            ids.addAll(machine.operatorIds)
        }
        ids
    }
    val selectedOperators = allOperators.filter { it.id in allSelectedOperatorIds }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Makinaları Birleştir",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Birleştirilecek makinaları seçin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current Machine Option
                if (currentControlItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeCurrentMachine = !includeCurrentMachine }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeCurrentMachine,
                            onCheckedChange = { includeCurrentMachine = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = themeColors.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentMachineTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = themeColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${currentControlItems.size} kontrol (aktif)",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.secondary
                            )
                        }
                    }
                    Divider(color = themeColors.glassBorder)
                }
                
                // Saved Machines
                machines.forEach { machine ->
                    val isSelected = machine.id in selectedMachines
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectionChange(
                                    if (isSelected) selectedMachines - machine.id
                                    else selectedMachines + machine.id
                                )
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                onSelectionChange(
                                    if (checked) selectedMachines + machine.id
                                    else selectedMachines - machine.id
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = themeColors.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = machine.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = themeColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${machine.controlItems.size} kontrol",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.textSecondary
                            )
                        }
                    }
                    Divider(color = themeColors.glassBorder)
                }
                
                // Operators Summary Section
                if (selectedOperators.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Kontrol Yapan Operatörler:",
                        style = MaterialTheme.typography.titleSmall,
                        color = themeColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedOperators.forEach { operator ->
                            AssistChip(
                                onClick = { },
                                label = { Text(operator.name, style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMerge(includeCurrentMachine) },
                enabled = includeCurrentMachine || selectedMachines.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondary)
            ) {
                Icon(Icons.Default.MergeType, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Birleştir")
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

@Composable
private fun SaveShareDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Excel Dosyası Hazır",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Dosyayı kaydetmek veya paylaşmak için seçim yapın:",
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.textSecondary
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShare,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paylaş")
                }
                
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = themeColors.textSecondary)
            }
        },
        containerColor = themeColors.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperatorSelectionDialog(
    allOperators: List<Operator>,
    selectedOperatorIds: Set<Long>,
    onSelectionChange: (Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Operatör Seç",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (allOperators.isEmpty()) {
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
                            text = "Henüz operatör eklenmedi.\nAyarlar'dan operatör ekleyebilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.textDisabled
                        )
                    }
                } else {
                    Text(
                        text = "Kontrol yapan operatörleri seçin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    allOperators.forEach { operator ->
                        val isSelected = operator.id in selectedOperatorIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectionChange(
                                        if (isSelected) selectedOperatorIds - operator.id
                                        else selectedOperatorIds + operator.id
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    onSelectionChange(
                                        if (checked) selectedOperatorIds + operator.id
                                        else selectedOperatorIds - operator.id
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = themeColors.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                        }
                        Divider(color = themeColors.glassBorder)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Text("Tamam")
            }
        },
        containerColor = themeColors.surface
    )
}

// ===== Helper Functions =====

private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
private const val MAX_BITMAP_DIMENSION = 800

private fun saveBitmapToFile(context: Context, bitmap: Bitmap, prefix: String): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.filesDir, "${prefix}_$timestamp.jpg")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    }
    return file
}

private fun loadBitmapFromFile(path: String): Bitmap? {
    return try {
        val file = File(path)
        if (file.exists()) {
            // Use inSampleSize to reduce memory usage for large images
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_BITMAP_DIMENSION, MAX_BITMAP_DIMENSION)
            options.inJustDecodeBounds = false
            
            BitmapFactory.decodeFile(path, options)
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun shareExcelFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX,
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Excel Dosyasını Paylaş"))
    } catch (e: Exception) {
        Toast.makeText(context, "Paylaşma hatası: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private suspend fun exportMergedToExcel(
    context: Context,
    excelService: ExcelService,
    allItems: List<Pair<String, ControlItem>>,
    operatorNames: List<String> = emptyList()
): File = withContext(Dispatchers.IO) {
    val workbook = excelService.createWorkbook()
    val sheet = excelService.createSheetWithHeader(
        workbook = workbook,
        sheetName = "Birleşik Kontrol",
        title = "Birleşik Genel Kontrol Raporu"
    )
    
    // Set column widths
    sheet.setColumnWidth(0, 10 * 256)  // No
    sheet.setColumnWidth(1, 25 * 256)  // Makina
    sheet.setColumnWidth(2, 30 * 256)  // Başlık
    sheet.setColumnWidth(3, 30 * 256)  // Notlar
    sheet.setColumnWidth(4, 20 * 256)  // Tarih
    sheet.setColumnWidth(5, 20 * 256)  // Durum
    sheet.setColumnWidth(6, 50 * 256)  // Fotoğraf
    
    val dataStyle = excelService.createDataStyle(workbook)
    
    // Add Operators row if operators are available
    var startDataRow = 5
    if (operatorNames.isNotEmpty()) {
        val operatorsRow = sheet.createRow(4)
        operatorsRow.heightInPoints = 20f
        val operatorsCell = operatorsRow.createCell(0)
        operatorsCell.setCellValue("Kontrol Yapan Operatörler: ${operatorNames.joinToString(", ")}")
        operatorsCell.cellStyle = dataStyle
        startDataRow = 6
    }
    
    // Add header row
    val headerRow = sheet.createRow(startDataRow)
    headerRow.heightInPoints = 25f
    val headers = listOf("No", "Makina", "Başlık", "Notlar", "Tarih", "Durum", "Fotoğraf")
    headers.forEachIndexed { index, header ->
        val cell = headerRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = dataStyle
    }
    
    val exportSessionId = System.currentTimeMillis()
    val tempFiles = mutableListOf<File>()
    
    try {
        var currentRow = startDataRow + 1
        allItems.forEachIndexed { index, (machineTitle, item) ->
            val row = sheet.createRow(currentRow)
            row.heightInPoints = 150f
            
            // No
            row.createCell(0).apply {
                setCellValue((index + 1).toString())
                cellStyle = dataStyle
            }
            
            // Makina
            row.createCell(1).apply {
                setCellValue(machineTitle)
                cellStyle = dataStyle
            }
            
            // Başlık
            row.createCell(2).apply {
                setCellValue(item.title)
                cellStyle = dataStyle
            }
            
            // Notlar
            row.createCell(3).apply {
                setCellValue(item.notes)
                cellStyle = dataStyle
            }
            
            // Tarih
            row.createCell(4).apply {
                setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp))
                cellStyle = dataStyle
            }
            
            // Durum
            row.createCell(5).apply {
                setCellValue(item.status)
                cellStyle = dataStyle
            }
            
            // Fotoğraf
            val tempImageFile = File(context.cacheDir, "merged_${exportSessionId}_${index}.jpg")
            FileOutputStream(tempImageFile).use { outputStream ->
                item.bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            }
            tempFiles.add(tempImageFile)
            
            excelService.embedImageInCell(
                workbook = workbook,
                sheet = sheet,
                imagePath = tempImageFile.absolutePath,
                row = currentRow,
                column = 6
            )
            
            currentRow++
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GenelKontrol_Birlesik_$timestamp.xlsx"
        val outputFile = File(excelService.getOutputDirectory(), fileName)
        
        excelService.saveWorkbook(workbook, outputFile.absolutePath)
        workbook.close()
        
        outputFile
    } finally {
        tempFiles.forEach { it.delete() }
    }
}
