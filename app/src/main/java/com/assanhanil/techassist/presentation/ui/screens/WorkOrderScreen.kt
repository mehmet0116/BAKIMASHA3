package com.assanhanil.techassist.presentation.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 * Data class representing a work order item with photo and metadata.
 */
data class WorkOrderItem(
    val id: Int,
    val machineTitle: String,
    val machineId: Long,
    val title: String,
    val notes: String,
    val bitmap: Bitmap?,
    val imagePath: String,
    val timestamp: Date,
    val status: String,
    val securityStatus: SecurityStatus = SecurityStatus.NOT_SET,
    val workOrderDetails: String = ""
)

/**
 * Work Order Screen - Shows all control items that require work orders.
 * 
 * Features:
 * - Lists all control items with requiresWorkOrder = true from all machines
 * - Shows machine name, control title, and work order details
 * - Allows viewing details and photos
 * - Excel export of work order items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderScreen(
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
    
    // Extract all work order items from all machines
    val workOrderItems = remember(savedMachineControls) {
        savedMachineControls.flatMap { machine ->
            machine.controlItems
                .filter { it.requiresWorkOrder }
                .map { data ->
                    WorkOrderItem(
                        id = data.id,
                        machineTitle = machine.title,
                        machineId = machine.id,
                        title = data.title,
                        notes = data.notes,
                        bitmap = loadBitmapFromFileSafe(data.imagePath),
                        imagePath = data.imagePath,
                        timestamp = Date(data.timestamp),
                        status = data.status,
                        securityStatus = data.securityStatus,
                        workOrderDetails = data.workOrderDetails
                    )
                }
        }.sortedByDescending { it.timestamp }
    }
    
    // Dialog states
    var selectedItem by remember { mutableStateOf<WorkOrderItem?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var showSaveLocationDialog by remember { mutableStateOf(false) }
    
    // Document create launcher for save location
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            generatedFile?.let { file ->
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
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "İş Emri",
                    style = MaterialTheme.typography.headlineSmall,
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "İş emri gerektiren kontroller",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
            }
            
            // Work order count badge
            if (workOrderItems.isNotEmpty()) {
                Badge(
                    containerColor = themeColors.error,
                    contentColor = themeColors.background
                ) {
                    Text(
                        text = workOrderItems.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Export button (only shown when there are items)
        if (workOrderItems.isNotEmpty()) {
            NeonCard(
                glowColor = themeColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExportDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Excel'e Aktar",
                        tint = themeColors.error,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Excel'e Aktar",
                            style = MaterialTheme.typography.titleMedium,
                            color = themeColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tüm iş emirlerini Excel dosyasına aktar",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Work Order Items List
        if (workOrderItems.isEmpty()) {
            // Empty state
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = themeColors.secondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "İş Emri Yok",
                        style = MaterialTheme.typography.titleLarge,
                        color = themeColors.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tüm kontroller tamamlandı, iş emri gerektiren öğe bulunmuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workOrderItems) { item ->
                    WorkOrderItemCard(
                        item = item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        }
    }
    
    // Item Detail Dialog
    selectedItem?.let { item ->
        WorkOrderItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "İş Emirlerini Aktar",
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${workOrderItems.size} iş emrini Excel dosyasına aktarmak istiyor musunuz?",
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val file = exportWorkOrdersToExcel(
                                    context = context,
                                    excelService = excelService,
                                    workOrderItems = workOrderItems,
                                    allOperators = allOperators,
                                    savedMachineControls = savedMachineControls
                                )
                                generatedFile = file
                                showExportDialog = false
                                showSaveLocationDialog = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Aktarma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.error)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aktar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Save/Share Location Dialog
    if (showSaveLocationDialog && generatedFile != null) {
        AlertDialog(
            onDismissRequest = { showSaveLocationDialog = false },
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
                        onClick = {
                            generatedFile?.let { file ->
                                shareExcelFileSafe(context, file)
                            }
                            showSaveLocationDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paylaş")
                    }
                    
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            createDocumentLauncher.launch("IsEmri_$timestamp.xlsx")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kaydet")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveLocationDialog = false }) {
                    Text("Kapat", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

// ===== Work Order Item Card =====

@Composable
private fun WorkOrderItemCard(
    item: WorkOrderItem,
    onClick: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Machine Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = themeColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.machineTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Thumbnail
                item.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Kontrol Fotoğrafı",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    // Control Title
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Timestamp
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Work Order Status Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = themeColors.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "İş Emri Gerekli",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Work Order Details Preview
                    if (item.workOrderDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.workOrderDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.error.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

// ===== Work Order Item Detail Dialog =====

@Composable
private fun WorkOrderItemDetailDialog(
    item: WorkOrderItem,
    onDismiss: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = themeColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.machineTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    color = themeColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Photo
                item.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Kontrol Fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Timestamp
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
                
                // Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = themeColors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.error,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Notes
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
                
                // Work Order Details
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Yapılacak İşler:",
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (item.workOrderDetails.isNotEmpty()) item.workOrderDetails else "Detay girilmedi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.error.copy(alpha = 0.9f),
                        modifier = Modifier.padding(12.dp)
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

// ===== Helper Functions =====

private const val MAX_BITMAP_DIMENSION = 800

private fun loadBitmapFromFileSafe(path: String): Bitmap? {
    return try {
        val file = File(path)
        if (file.exists()) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            
            options.inSampleSize = calculateInSampleSizeSafe(options, MAX_BITMAP_DIMENSION, MAX_BITMAP_DIMENSION)
            options.inJustDecodeBounds = false
            
            BitmapFactory.decodeFile(path, options)
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun calculateInSampleSizeSafe(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

private fun shareExcelFileSafe(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "İş Emri Dosyasını Paylaş"))
    } catch (e: Exception) {
        Toast.makeText(context, "Paylaşma hatası: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private suspend fun exportWorkOrdersToExcel(
    context: Context,
    excelService: ExcelService,
    workOrderItems: List<WorkOrderItem>,
    allOperators: List<Operator>,
    savedMachineControls: List<MachineControl>
): File = withContext(Dispatchers.IO) {
    val workbook = excelService.createWorkbook()
    val sheet = excelService.createSheetWithHeader(
        workbook = workbook,
        sheetName = "İş Emirleri",
        title = "İş Emri - Yapılacak İşler"
    )
    
    // Set column widths
    sheet.setColumnWidth(0, 10 * 256)  // No
    sheet.setColumnWidth(1, 25 * 256)  // Makina
    sheet.setColumnWidth(2, 30 * 256)  // Başlık
    sheet.setColumnWidth(3, 20 * 256)  // Tarih
    sheet.setColumnWidth(4, 20 * 256)  // Durum
    sheet.setColumnWidth(5, 50 * 256)  // Yapılacak İşler
    sheet.setColumnWidth(6, 50 * 256)  // Fotoğraf
    
    val dataStyle = excelService.createDataStyle(workbook)
    
    // Get all unique operator IDs from machines with work order items
    val machineIdsWithWorkOrders = workOrderItems.map { it.machineId }.toSet()
    val allOperatorIds = savedMachineControls
        .filter { it.id in machineIdsWithWorkOrders }
        .flatMap { it.operatorIds }
        .toSet()
    val operatorNames = allOperators
        .filter { it.id in allOperatorIds }
        .map { it.name }
    
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
    val headers = listOf("No", "Makina", "Başlık", "Tarih", "Durum", "Yapılacak İşler", "Fotoğraf")
    headers.forEachIndexed { index, header ->
        val cell = headerRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = dataStyle
    }
    
    val exportSessionId = java.util.UUID.randomUUID().toString().take(8)
    // Map index to temp file path for proper image reuse across sheets
    val tempFilesMap = mutableMapOf<Int, File>()
    
    // Create "Yapılacak İşler" sheet at the beginning alongside main sheet
    val yapilacakIslerSheet = excelService.createSheetWithHeader(
        workbook = workbook,
        sheetName = "Yapılacak İşler",
        title = "Yapılacak İşler Detay"
    )
    
    try {
        var currentRow = startDataRow + 1
        workOrderItems.forEachIndexed { index, item ->
            val row = sheet.createRow(currentRow)
            row.heightInPoints = 150f
            
            // No
            row.createCell(0).apply {
                setCellValue((index + 1).toString())
                cellStyle = dataStyle
            }
            
            // Makina
            row.createCell(1).apply {
                setCellValue(item.machineTitle)
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
            item.bitmap?.let { bitmap ->
                val tempImageFile = File(context.cacheDir, "techassist_workorder_${exportSessionId}_${index}.jpg")
                FileOutputStream(tempImageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }
                tempFilesMap[index] = tempImageFile
                
                excelService.embedImageInCell(
                    workbook = workbook,
                    sheet = sheet,
                    imagePath = tempImageFile.absolutePath,
                    row = currentRow,
                    column = 6
                )
            }
            
            currentRow++
        }
        
        // Configure "Yapılacak İşler" sheet
        
        // Set column widths for Yapılacak İşler sheet
        yapilacakIslerSheet.setColumnWidth(0, 10 * 256)  // No
        yapilacakIslerSheet.setColumnWidth(1, 30 * 256)  // Makina İsmi
        yapilacakIslerSheet.setColumnWidth(2, 35 * 256)  // Kontrol Başlığı
        yapilacakIslerSheet.setColumnWidth(3, 50 * 256)  // Açıklama (Notes)
        yapilacakIslerSheet.setColumnWidth(4, 50 * 256)  // Yapılacak İşler
        yapilacakIslerSheet.setColumnWidth(5, 20 * 256)  // Tarih
        yapilacakIslerSheet.setColumnWidth(6, 50 * 256)  // Fotoğraf
        
        // Add Operators row to Yapılacak İşler sheet if operators are available
        var yiStartDataRow = 5
        if (operatorNames.isNotEmpty()) {
            val yiOperatorsRow = yapilacakIslerSheet.createRow(4)
            yiOperatorsRow.heightInPoints = 20f
            val yiOperatorsCell = yiOperatorsRow.createCell(0)
            yiOperatorsCell.setCellValue("Kontrol Yapan Operatörler: ${operatorNames.joinToString(", ")}")
            yiOperatorsCell.cellStyle = dataStyle
            yiStartDataRow = 6
        }
        
        // Add header row to Yapılacak İşler sheet
        val yiHeaderRow = yapilacakIslerSheet.createRow(yiStartDataRow)
        yiHeaderRow.heightInPoints = 25f
        val yiHeaders = listOf("No", "Makina İsmi", "Kontrol Başlığı", "Açıklama", "Yapılacak İşler", "Tarih", "Fotoğraf")
        yiHeaders.forEachIndexed { index, header ->
            val cell = yiHeaderRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = dataStyle
        }
        
        // Add work order items to Yapılacak İşler sheet
        var yiCurrentRow = yiStartDataRow + 1
        workOrderItems.forEachIndexed { index, item ->
            val row = yapilacakIslerSheet.createRow(yiCurrentRow)
            row.heightInPoints = 150f
            
            // No
            row.createCell(0).apply {
                setCellValue((index + 1).toString())
                cellStyle = dataStyle
            }
            
            // Makina İsmi
            row.createCell(1).apply {
                setCellValue(item.machineTitle)
                cellStyle = dataStyle
            }
            
            // Kontrol Başlığı
            row.createCell(2).apply {
                setCellValue(item.title)
                cellStyle = dataStyle
            }
            
            // Açıklama (Notes)
            row.createCell(3).apply {
                setCellValue(item.notes)
                cellStyle = dataStyle
            }
            
            // Yapılacak İşler
            row.createCell(4).apply {
                setCellValue(item.workOrderDetails)
                cellStyle = dataStyle
            }
            
            // Tarih
            row.createCell(5).apply {
                setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp))
                cellStyle = dataStyle
            }
            
            // Fotoğraf - reuse existing temp files using proper index mapping
            tempFilesMap[index]?.let { tempFile ->
                excelService.embedImageInCell(
                    workbook = workbook,
                    sheet = yapilacakIslerSheet,
                    imagePath = tempFile.absolutePath,
                    row = yiCurrentRow,
                    column = 6
                )
            }
            
            yiCurrentRow++
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IsEmri_$timestamp.xlsx"
        val outputFile = File(excelService.getOutputDirectory(), fileName)
        
        excelService.saveWorkbook(workbook, outputFile.absolutePath)
        workbook.close()
        
        outputFile
    } finally {
        tempFilesMap.values.forEach { file -> runCatching { file.delete() } }
    }
}
