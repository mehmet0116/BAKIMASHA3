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
import com.assanhanil.techassist.domain.model.MachineName
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.domain.model.SecurityStatus
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.components.NoOperatorsSignatureDialog
import com.assanhanil.techassist.presentation.ui.components.OperatorSignature
import com.assanhanil.techassist.presentation.ui.components.OperatorSignatureDialog
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.presentation.viewmodel.MachineNameViewModel
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
    val securityStatus: SecurityStatus = SecurityStatus.NOT_SET,
    val requiresWorkOrder: Boolean = false,
    val workOrderDetails: String = ""
)

/**
 * General Control Screen - Enhanced field inspection photo capture with machine management.
 * 
 * Features:
 * - Machine/Title creation and persistence (Başlık oluşturma ve kaydetme)
 * - Machine names are templates that persist for quick selection
 * - Operator selection for controls (Operatör seçimi)
 * - Security Control section with photo capture (Güvenlik Kontrol)
 * - Security status options: Active/Inactive (Güvenlik Devrede Aktif / Devrede Değil)
 * - Export button to send control data to Excel (Gönder)
 * - Share and Save with user-selectable destinations
 * - Excel export with images and operators embedded
 * - Control data is cleared after export (data is already in Excel report)
 */
@Composable
fun GeneralControlScreen(
    excelService: ExcelService,
    machineNameViewModel: MachineNameViewModel,
    operatorViewModel: OperatorViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Collect machine names (templates) from database
    val machineNames by machineNameViewModel.machineNames.collectAsState()
    
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
    var showMachineNamesDialog by remember { mutableStateOf(false) }
    var showOperatorSelectionDialog by remember { mutableStateOf(false) }
    
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedItem by remember { mutableStateOf<ControlItem?>(null) }
    var mergedFile by remember { mutableStateOf<File?>(null) }
    var mergedOperatorNames by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Signature dialog states
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showNoOperatorsDialog by remember { mutableStateOf(false) }
    var pendingExportData by remember { mutableStateOf<PendingExportData?>(null) }
    
    // Track if current machine should be cleared after export
    var clearCurrentAfterExport by remember { mutableStateOf(false) }
    
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
            
            // Load Machine Names (templates) Button
            OutlinedButton(
                onClick = { showMachineNamesDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Makinalar (${machineNames.size})")
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
            
            // Clear Controls Button - clears current machine's control items
            OutlinedButton(
                onClick = {
                    // Just clear the control items, don't delete the machine name
                    controlItems = emptyList()
                    nextItemId = 1
                    selectedOperatorIds = emptySet()
                    Toast.makeText(context, "Kontrol öğeleri temizlendi", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.error
                ),
                enabled = controlItems.isNotEmpty()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kontrolleri Temizle")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Merge All and Export Buttons (Always visible when there's control data)
        if (controlItems.isNotEmpty()) {
            Divider(color = themeColors.glassBorder)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Export Button
            Button(
                onClick = { showMergeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gönder")
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
                // Save machine name as template
                machineNameViewModel.saveMachineName(title)
                Toast.makeText(context, "Makina oluşturuldu: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Machine Names Dialog (templates for quick selection)
    if (showMachineNamesDialog) {
        MachineNamesDialog(
            machineNames = machineNames,
            onDismiss = { showMachineNamesDialog = false },
            onSelect = { machineName ->
                // Only set the machine name, don't load any control data
                currentMachineTitle = machineName.name
                currentMachineId = 0
                controlItems = emptyList()
                nextItemId = 1
                selectedOperatorIds = emptySet()
                showMachineNamesDialog = false
                Toast.makeText(context, "Makina seçildi: ${machineName.name}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { machineName ->
                machineNameViewModel.deleteMachineName(machineName.id) {
                    Toast.makeText(context, "Makina silindi: ${machineName.name}", Toast.LENGTH_SHORT).show()
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
            onSave = { title, notes, requiresWorkOrder, workOrderDetails ->
                val newItem = ControlItem(
                    id = nextItemId,
                    title = title,
                    notes = notes,
                    bitmap = pendingBitmap!!,
                    timestamp = Date(),
                    status = if (requiresWorkOrder) STATUS_WORK_ORDER_REQUIRED else "Kontrol Edildi",
                    securityStatus = SecurityStatus.NOT_SET,
                    requiresWorkOrder = requiresWorkOrder,
                    workOrderDetails = workOrderDetails
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
            onSave = { title, notes, securityStatus, requiresWorkOrder, workOrderDetails ->
                val newItem = ControlItem(
                    id = nextItemId,
                    title = title,
                    notes = notes,
                    bitmap = pendingBitmap!!,
                    timestamp = Date(),
                    status = if (requiresWorkOrder) STATUS_WORK_ORDER_REQUIRED else if (securityStatus == SecurityStatus.ACTIVE) "Güvenlik Aktif" else "Güvenlik Devre Dışı",
                    securityStatus = securityStatus,
                    requiresWorkOrder = requiresWorkOrder,
                    workOrderDetails = workOrderDetails
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
    
    // Merge Dialog - simplified to export current machine controls only
    if (showMergeDialog) {
        ExportDialog(
            currentMachineTitle = currentMachineTitle,
            currentControlItems = controlItems,
            allOperators = allOperators,
            currentOperatorIds = selectedOperatorIds,
            onDismiss = { showMergeDialog = false },
            onExport = {
                if (controlItems.isNotEmpty()) {
                    val allItems = controlItems.map { item ->
                        currentMachineTitle to item
                    }
                    val operatorsForSignature = allOperators.filter { it.id in selectedOperatorIds }
                    
                    // Store pending export data
                    pendingExportData = PendingExportData(
                        items = allItems,
                        operators = operatorsForSignature
                    )
                    
                    // Mark that we should clear data after export
                    clearCurrentAfterExport = true
                    
                    showMergeDialog = false
                    
                    // Show signature dialog if there are operators, otherwise show no-operators dialog
                    if (operatorsForSignature.isNotEmpty()) {
                        showSignatureDialog = true
                    } else {
                        showNoOperatorsDialog = true
                    }
                } else {
                    Toast.makeText(context, "Gönderilecek veri bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    // Signature Dialog - shown before export
    if (showSignatureDialog && pendingExportData != null) {
        OperatorSignatureDialog(
            operators = pendingExportData!!.operators,
            onDismiss = { 
                showSignatureDialog = false
                pendingExportData = null
            },
            onConfirm = { signatures ->
                scope.launch {
                    try {
                        val operatorNames = pendingExportData!!.operators.map { it.name }
                        val file = exportMergedToExcelWithSignatures(
                            context = context,
                            excelService = excelService,
                            allItems = pendingExportData!!.items,
                            operatorNames = operatorNames,
                            operatorSignatures = signatures
                        )
                        mergedFile = file
                        mergedOperatorNames = operatorNames
                        showSignatureDialog = false
                        pendingExportData = null
                        showSaveLocationDialog = true
                    } catch (e: Exception) {
                        Toast.makeText(context, "Excel oluşturma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    // No Operators Dialog - shown when trying to export without operators
    if (showNoOperatorsDialog && pendingExportData != null) {
        NoOperatorsSignatureDialog(
            onDismiss = {
                showNoOperatorsDialog = false
                pendingExportData = null
            },
            onContinueWithoutSignature = {
                scope.launch {
                    try {
                        val file = exportMergedToExcel(
                            context = context,
                            excelService = excelService,
                            allItems = pendingExportData!!.items,
                            operatorNames = emptyList()
                        )
                        mergedFile = file
                        mergedOperatorNames = emptyList()
                        showNoOperatorsDialog = false
                        pendingExportData = null
                        showSaveLocationDialog = true
                    } catch (e: Exception) {
                        Toast.makeText(context, "Excel oluşturma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    // Save/Share Location Dialog
    if (showSaveLocationDialog && mergedFile != null) {
        SaveShareDialog(
            onDismiss = { 
                showSaveLocationDialog = false
                // Clear data after closing dialog (export completed)
                if (clearCurrentAfterExport) {
                    controlItems = emptyList()
                    nextItemId = 1
                    selectedOperatorIds = emptySet()
                    clearCurrentAfterExport = false
                    Toast.makeText(context, "Kontrol verileri temizlendi", Toast.LENGTH_SHORT).show()
                }
            },
            onSave = {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                createDocumentLauncher.launch("GenelKontrol_Birlesik_$timestamp.xlsx")
                // Clear data after save
                if (clearCurrentAfterExport) {
                    controlItems = emptyList()
                    nextItemId = 1
                    selectedOperatorIds = emptySet()
                    clearCurrentAfterExport = false
                }
            },
            onShare = {
                mergedFile?.let { file ->
                    shareExcelFile(context, file)
                }
                showSaveLocationDialog = false
                // Clear data after share
                if (clearCurrentAfterExport) {
                    controlItems = emptyList()
                    nextItemId = 1
                    selectedOperatorIds = emptySet()
                    clearCurrentAfterExport = false
                    Toast.makeText(context, "Kontrol verileri temizlendi", Toast.LENGTH_SHORT).show()
                }
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
                    val statusIcon = if (item.requiresWorkOrder)
                        Icons.Default.Build
                    else if (item.securityStatus != SecurityStatus.NOT_SET) 
                        Icons.Default.Security 
                    else 
                        Icons.Default.CheckCircle
                    val statusColor = if (item.requiresWorkOrder)
                        themeColors.error
                    else when (item.securityStatus) {
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
                
                // Show work order details indicator if present
                if (item.requiresWorkOrder && item.workOrderDetails.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Yapılacak İşler: ${item.workOrderDetails}",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.error.copy(alpha = 0.8f),
                        maxLines = 1
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
private fun MachineNamesDialog(
    machineNames: List<MachineName>,
    onDismiss: () -> Unit,
    onSelect: (MachineName) -> Unit,
    onDelete: (MachineName) -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Makinalar",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (machineNames.isEmpty()) {
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
                    Text(
                        text = "Kontrol yapmak için bir makina seçin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    machineNames.forEach { machineName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(machineName) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = machineName.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = themeColors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            IconButton(onClick = { onDelete(machineName) }) {
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
private fun ExportDialog(
    currentMachineTitle: String,
    currentControlItems: List<ControlItem>,
    allOperators: List<Operator>,
    currentOperatorIds: Set<Long>,
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    val selectedOperators = allOperators.filter { it.id in currentOperatorIds }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Gönderim",
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Aşağıdaki kontrol verilerini Excel'e aktarılacak ve ardından temizlenecek:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current machine info
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = currentMachineTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = themeColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentControlItems.size} kontrol öğesi",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.secondary
                        )
                    }
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Not: Gönderim sonrası kontrol verileri temizlenecektir. Veriler Excel raporunda saklanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textDisabled
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onExport,
                enabled = currentControlItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gönder")
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
private fun TitleInputDialog(
    itemNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var requiresWorkOrder by remember { mutableStateOf(false) }
    var workOrderDetails by remember { mutableStateOf("") }
    
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Work Order Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requiresWorkOrder = !requiresWorkOrder }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = requiresWorkOrder,
                        onCheckedChange = { requiresWorkOrder = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = themeColors.error
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "İş Emri Oluştur",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (requiresWorkOrder) themeColors.error else themeColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Uygunsuzluk tespit edildi, iş emri gerekli",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary
                        )
                    }
                }
                
                // Work Order Details (shown when checkbox is checked)
                if (requiresWorkOrder) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = workOrderDetails,
                        onValueChange = { workOrderDetails = it },
                        label = { Text("Yapılacak İşler") },
                        placeholder = { Text("Gerekli işlemleri detaylı yazın...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.error,
                            unfocusedBorderColor = themeColors.error.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { "Kontrol #$itemNumber" }, notes, requiresWorkOrder, workOrderDetails) },
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
    onSave: (String, String, SecurityStatus, Boolean, String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var securityStatus by remember { mutableStateOf(SecurityStatus.ACTIVE) }
    var requiresWorkOrder by remember { mutableStateOf(false) }
    var workOrderDetails by remember { mutableStateOf("") }
    
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Work Order Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requiresWorkOrder = !requiresWorkOrder }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = requiresWorkOrder,
                        onCheckedChange = { requiresWorkOrder = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = themeColors.error
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "İş Emri Oluştur",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (requiresWorkOrder) themeColors.error else themeColors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Uygunsuzluk tespit edildi, iş emri gerekli",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary
                        )
                    }
                }
                
                // Work Order Details (shown when checkbox is checked)
                if (requiresWorkOrder) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = workOrderDetails,
                        onValueChange = { workOrderDetails = it },
                        label = { Text("Yapılacak İşler") },
                        placeholder = { Text("Gerekli işlemleri detaylı yazın...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.error,
                            unfocusedBorderColor = themeColors.error.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { "Güvenlik Kontrolü #$itemNumber" }, notes, securityStatus, requiresWorkOrder, workOrderDetails) },
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
                    val statusIcon = if (item.requiresWorkOrder)
                        Icons.Default.Build
                    else if (item.securityStatus != SecurityStatus.NOT_SET) 
                        Icons.Default.Security 
                    else 
                        Icons.Default.CheckCircle
                    val statusColor = if (item.requiresWorkOrder)
                        themeColors.error
                    else when (item.securityStatus) {
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
                
                // Work Order Details Section
                if (item.requiresWorkOrder) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Yapılacak İşler:",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.workOrderDetails.isNotEmpty()) item.workOrderDetails else "Detay girilmedi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.error.copy(alpha = 0.8f)
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
private const val STATUS_WORK_ORDER_REQUIRED = "İş Emri Gerekli"

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
    
    // Store temp files for work order items separately (for Yapılacak İşler sheet)
    val workOrderTempFiles = mutableMapOf<Int, File>()
    
    try {
        // Partition items into control items (for main sheet) and work order items (for Yapılacak İşler sheet)
        // Work order items will only appear in the "Yapılacak İşler" sheet
        val (controlItems, workOrderItems) = allItems.partition { (_, item) -> !item.requiresWorkOrder }
        
        var currentRow = startDataRow + 1
        controlItems.forEachIndexed { index, (machineTitle, item) ->
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
        
        // Create "Yapılacak İşler" sheet for work order items (from partition above)
        if (workOrderItems.isNotEmpty()) {
            val workOrderSheet = excelService.createSheetWithHeader(
                workbook = workbook,
                sheetName = "Yapılacak İşler",
                title = "İş Emri - Yapılacak İşler"
            )
            
            // Set column widths for work order sheet
            workOrderSheet.setColumnWidth(0, 10 * 256)  // No
            workOrderSheet.setColumnWidth(1, 25 * 256)  // Makina
            workOrderSheet.setColumnWidth(2, 30 * 256)  // Başlık
            workOrderSheet.setColumnWidth(3, 20 * 256)  // Tarih
            workOrderSheet.setColumnWidth(4, 20 * 256)  // Durum
            workOrderSheet.setColumnWidth(5, 50 * 256)  // Yapılacak İşler
            workOrderSheet.setColumnWidth(6, 50 * 256)  // Fotoğraf
            
            // Add header row for work order sheet
            val woHeaderRow = workOrderSheet.createRow(5)
            woHeaderRow.heightInPoints = 25f
            val woHeaders = listOf("No", "Makina", "Başlık", "Tarih", "Durum", "Yapılacak İşler", "Fotoğraf")
            woHeaders.forEachIndexed { index, header ->
                val cell = woHeaderRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = dataStyle
            }
            
            // Add work order items
            var woCurrentRow = 6
            workOrderItems.forEachIndexed { index, pair ->
                val (machineTitle, item) = pair
                val row = workOrderSheet.createRow(woCurrentRow)
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
                
                // Tarih
                row.createCell(3).apply {
                    setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp))
                    cellStyle = dataStyle
                }
                
                // Durum
                row.createCell(4).apply {
                    setCellValue(item.status)
                    cellStyle = dataStyle
                }
                
                // Yapılacak İşler
                row.createCell(5).apply {
                    setCellValue(item.workOrderDetails)
                    cellStyle = dataStyle
                }
                
                // Fotoğraf - create temp file for work order item
                val woTempImageFile = File(context.cacheDir, "workorder_${exportSessionId}_${index}.jpg")
                FileOutputStream(woTempImageFile).use { outputStream ->
                    item.bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }
                workOrderTempFiles[index] = woTempImageFile
                
                excelService.embedImageInCell(
                    workbook = workbook,
                    sheet = workOrderSheet,
                    imagePath = woTempImageFile.absolutePath,
                    row = woCurrentRow,
                    column = 6
                )
                
                woCurrentRow++
            }
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GenelKontrol_Birlesik_$timestamp.xlsx"
        val outputFile = File(excelService.getOutputDirectory(), fileName)
        
        excelService.saveWorkbook(workbook, outputFile.absolutePath)
        workbook.close()
        
        outputFile
    } finally {
        tempFiles.forEach { it.delete() }
        workOrderTempFiles.values.forEach { it.delete() }
    }
}

/**
 * Data class to hold pending export data between dialogs.
 */
private data class PendingExportData(
    val items: List<Pair<String, ControlItem>>,
    val operators: List<Operator>
)

/**
 * Export merged data to Excel with operator signatures.
 * This function includes signatures at the bottom of the Excel report.
 */
private suspend fun exportMergedToExcelWithSignatures(
    context: Context,
    excelService: ExcelService,
    allItems: List<Pair<String, ControlItem>>,
    operatorNames: List<String>,
    operatorSignatures: List<OperatorSignature>
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
    val signatureTempFiles = mutableListOf<File>()
    val workOrderTempFiles = mutableMapOf<Int, File>()
    
    try {
        // Partition items into control items (for main sheet) and work order items (for Yapılacak İşler sheet)
        val (controlItems, workOrderItems) = allItems.partition { (_, item) -> !item.requiresWorkOrder }
        
        var currentRow = startDataRow + 1
        controlItems.forEachIndexed { index, (machineTitle, item) ->
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
            val tempImageFile = File(context.cacheDir, "merged_sig_${exportSessionId}_${index}.jpg")
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
        
        // Add spacing before signatures
        currentRow += 2
        
        // Save signature bitmaps to temp files and embed them
        val signatureData = operatorSignatures.mapNotNull { sig ->
            sig.signatureBitmap?.let { bitmap ->
                val sigFile = File(context.cacheDir, "signature_${exportSessionId}_${sig.operatorId}.png")
                FileOutputStream(sigFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                signatureTempFiles.add(sigFile)
                sig.operatorName to sigFile.absolutePath
            }
        }
        
        // Embed operator signatures
        if (signatureData.isNotEmpty()) {
            currentRow = excelService.embedOperatorSignatures(
                workbook = workbook,
                sheet = sheet,
                signatures = signatureData,
                startRow = currentRow
            )
        }
        
        // Create "Yapılacak İşler" sheet for work order items
        if (workOrderItems.isNotEmpty()) {
            val workOrderSheet = excelService.createSheetWithHeader(
                workbook = workbook,
                sheetName = "Yapılacak İşler",
                title = "İş Emri - Yapılacak İşler"
            )
            
            // Set column widths for work order sheet
            workOrderSheet.setColumnWidth(0, 10 * 256)  // No
            workOrderSheet.setColumnWidth(1, 25 * 256)  // Makina
            workOrderSheet.setColumnWidth(2, 30 * 256)  // Başlık
            workOrderSheet.setColumnWidth(3, 20 * 256)  // Tarih
            workOrderSheet.setColumnWidth(4, 20 * 256)  // Durum
            workOrderSheet.setColumnWidth(5, 50 * 256)  // Yapılacak İşler
            workOrderSheet.setColumnWidth(6, 50 * 256)  // Fotoğraf
            
            // Add header row for work order sheet
            val woHeaderRow = workOrderSheet.createRow(5)
            woHeaderRow.heightInPoints = 25f
            val woHeaders = listOf("No", "Makina", "Başlık", "Tarih", "Durum", "Yapılacak İşler", "Fotoğraf")
            woHeaders.forEachIndexed { index, header ->
                val cell = woHeaderRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = dataStyle
            }
            
            // Add work order items
            var woCurrentRow = 6
            workOrderItems.forEachIndexed { index, pair ->
                val (machineTitle, item) = pair
                val row = workOrderSheet.createRow(woCurrentRow)
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
                
                // Tarih
                row.createCell(3).apply {
                    setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp))
                    cellStyle = dataStyle
                }
                
                // Durum
                row.createCell(4).apply {
                    setCellValue(item.status)
                    cellStyle = dataStyle
                }
                
                // Yapılacak İşler
                row.createCell(5).apply {
                    setCellValue(item.workOrderDetails)
                    cellStyle = dataStyle
                }
                
                // Fotoğraf
                val woTempImageFile = File(context.cacheDir, "workorder_sig_${exportSessionId}_${index}.jpg")
                FileOutputStream(woTempImageFile).use { outputStream ->
                    item.bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }
                workOrderTempFiles[index] = woTempImageFile
                
                excelService.embedImageInCell(
                    workbook = workbook,
                    sheet = workOrderSheet,
                    imagePath = woTempImageFile.absolutePath,
                    row = woCurrentRow,
                    column = 6
                )
                
                woCurrentRow++
            }
            
            // Add signatures to work order sheet as well
            if (signatureData.isNotEmpty()) {
                woCurrentRow += 2
                excelService.embedOperatorSignatures(
                    workbook = workbook,
                    sheet = workOrderSheet,
                    signatures = signatureData,
                    startRow = woCurrentRow
                )
            }
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GenelKontrol_Birlesik_$timestamp.xlsx"
        val outputFile = File(excelService.getOutputDirectory(), fileName)
        
        excelService.saveWorkbook(workbook, outputFile.absolutePath)
        workbook.close()
        
        outputFile
    } finally {
        tempFiles.forEach { it.delete() }
        signatureTempFiles.forEach { it.delete() }
        workOrderTempFiles.values.forEach { it.delete() }
    }
}
