package com.assanhanil.techassist.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.service.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * General Control Screen - Field inspection photo capture with Excel export.
 * 
 * Features:
 * - Camera permission handling
 * - Photo capture with title/header input
 * - "Kontrol Edildi" (Checked) status marking
 * - Excel export with images embedded inside cells
 */
@Composable
fun GeneralControlScreen(
    excelService: ExcelService,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var controlItems by remember { mutableStateOf<List<ControlItem>>(emptyList()) }
    var nextItemId by remember { mutableStateOf(1) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedItem by remember { mutableStateOf<ControlItem?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(16.dp)
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
            text = "Sahada kontrol edilen öğelerin fotoğrafını çekin",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Capture Button
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
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(themeColors.primary.copy(alpha = 0.2f))
                        .border(2.dp, themeColors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Fotoğraf Çek",
                        tint = themeColors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Kontrol Fotoğrafı Çek",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasCameraPermission) "Dokunarak fotoğraf çekin ve başlık girin" else "İzin gerekli",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control Items Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kontrol Edilen Öğeler",
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (controlItems.isEmpty()) {
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
                        tint = themeColors.textDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz kontrol yapılmadı",
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeColors.textDisabled
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yukarıdaki butona dokunarak fotoğraf çekin",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textDisabled
                    )
                }
            }
        } else {
            // Control items list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(controlItems) { item ->
                    ControlItemCard(
                        item = item,
                        onClick = { selectedItem = item },
                        onDelete = {
                            controlItems = controlItems.filter { it.id != item.id }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Export to Excel Button
            Button(
                onClick = {
                    isExporting = true
                    scope.launch {
                        try {
                            val file = exportToExcel(
                                context = context,
                                excelService = excelService,
                                controlItems = controlItems
                            )
                            Toast.makeText(
                                context,
                                "Excel oluşturuldu: ${file.name}",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Hata: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isExporting = false
                        }
                    }
                },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.secondary
                )
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = themeColors.background,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isExporting) "Oluşturuluyor..." else "Excel'e Aktar")
            }
        }
    }
    
    // Title Input Dialog - shown after taking photo
    if (showTitleDialog && pendingBitmap != null) {
        TitleInputDialog(
            itemNumber = nextItemId,
            onDismiss = { 
                showTitleDialog = false
                pendingBitmap = null
            },
            onSave = { title ->
                val newItem = ControlItem(
                    id = nextItemId,
                    title = title,
                    bitmap = pendingBitmap!!,
                    timestamp = Date(),
                    status = "Kontrol Edildi"
                )
                controlItems = controlItems + newItem
                nextItemId++
                showTitleDialog = false
                pendingBitmap = null
                Toast.makeText(context, "Kontrol kaydedildi", Toast.LENGTH_SHORT).show()
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
}

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
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
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
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = themeColors.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Delete button
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

@Composable
private fun TitleInputDialog(
    itemNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val themeColors = LocalThemeColors.current
    var title by remember { mutableStateOf("") }
    
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
                    text = "Kontrol edilen öğe için başlık girin:",
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { "Kontrol #$itemNumber" }) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                )
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = themeColors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                )
            ) {
                Text("Kapat")
            }
        },
        containerColor = themeColors.surface
    )
}

/**
 * Data class representing a control item with photo and metadata.
 */
data class ControlItem(
    val id: Int,
    val title: String,
    val bitmap: Bitmap,
    val timestamp: Date,
    val status: String
)

/**
 * Exports control items to an Excel file with images embedded INSIDE cells.
 */
private suspend fun exportToExcel(
    context: android.content.Context,
    excelService: ExcelService,
    controlItems: List<ControlItem>
): File = withContext(Dispatchers.IO) {
    val workbook = excelService.createWorkbook()
    val sheet = excelService.createSheetWithHeader(
        workbook = workbook,
        sheetName = "Genel Kontrol",
        title = "Saha Genel Kontrol Raporu"
    )
    
    // Set column widths
    sheet.setColumnWidth(0, 15 * 256)  // No column
    sheet.setColumnWidth(1, 30 * 256)  // Title column
    sheet.setColumnWidth(2, 20 * 256)  // Date column
    sheet.setColumnWidth(3, 15 * 256)  // Status column
    sheet.setColumnWidth(4, 50 * 256)  // Image column - wider for images
    
    // Create data style
    val dataStyle = excelService.createDataStyle(workbook)
    
    // Add header row at row 5 (after corporate header)
    val headerRow = sheet.createRow(5)
    headerRow.heightInPoints = 25f
    val headers = listOf("No", "Başlık", "Tarih", "Durum", "Fotoğraf")
    headers.forEachIndexed { index, header ->
        val cell = headerRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = dataStyle
    }
    
    // Create a unique export session ID to avoid file conflicts
    val exportSessionId = System.currentTimeMillis()
    val tempFiles = mutableListOf<File>()
    
    try {
        // Add control items
        var currentRow = 6
        controlItems.forEachIndexed { index, item ->
            val row = sheet.createRow(currentRow)
            row.heightInPoints = 150f  // Set appropriate height for images
            
            // No
            val noCell = row.createCell(0)
            noCell.setCellValue((index + 1).toString())
            noCell.cellStyle = dataStyle
            
            // Title
            val titleCell = row.createCell(1)
            titleCell.setCellValue(item.title)
            titleCell.cellStyle = dataStyle
            
            // Date
            val dateCell = row.createCell(2)
            dateCell.setCellValue(
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.timestamp)
            )
            dateCell.cellStyle = dataStyle
            
            // Status
            val statusCell = row.createCell(3)
            statusCell.setCellValue(item.status)
            statusCell.cellStyle = dataStyle
            
            // Image - save bitmap to temp file with unique name and embed in cell
            val tempImageFile = File(context.cacheDir, "control_${exportSessionId}_${item.id}.jpg")
            saveBitmapToFile(item.bitmap, tempImageFile)
            tempFiles.add(tempImageFile)
            
            // Embed image inside the cell
            excelService.embedImageInCell(
                workbook = workbook,
                sheet = sheet,
                imagePath = tempImageFile.absolutePath,
                row = currentRow,
                column = 4
            )
            
            currentRow++
        }
        
        // Save file
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GenelKontrol_$timestamp.xlsx"
        val outputFile = File(excelService.getOutputDirectory(), fileName)
        
        excelService.saveWorkbook(workbook, outputFile.absolutePath)
        workbook.close()
        
        outputFile
    } finally {
        // Clean up all temp files
        tempFiles.forEach { it.delete() }
    }
}

/**
 * Saves a bitmap to a file.
 */
private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    }
}
