package com.assanhanil.techassist.presentation.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.service.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reports Screen - Excel report generation feature.
 * 
 * Features:
 * - Create new maintenance reports
 * - Generate Excel (.xlsx) files
 * - View existing reports
 * - Share/export reports
 */
@Composable
fun ReportsScreen(
    excelService: ExcelService,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    
    var showNewReportDialog by remember { mutableStateOf(false) }
    var reportTitle by remember { mutableStateOf("") }
    var operatorName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedReports by remember { mutableStateOf<List<File>>(emptyList()) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Load existing reports
    LaunchedEffect(Unit) {
        val outputDir = excelService.getOutputDirectory()
        generatedReports = if (outputDir.exists()) {
            outputDir.listFiles()
                ?.filter { it.extension == "xlsx" }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else {
            emptyList()
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
            text = "Excel Rapor Oluşturucu",
            style = MaterialTheme.typography.headlineSmall,
            color = themeColors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Bakım raporlarını Excel formatında oluşturun ve paylaşın",
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // New Report Button
        NeonCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showNewReportDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yeni Rapor",
                    tint = themeColors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Yeni Rapor Oluştur",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Excel (.xlsx) formatında bakım raporu",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.textSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Existing Reports
        Text(
            text = "Mevcut Raporlar",
            style = MaterialTheme.typography.titleMedium,
            color = themeColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (generatedReports.isEmpty()) {
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
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = themeColors.textDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz rapor oluşturulmadı",
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeColors.textDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(generatedReports) { file ->
                    ReportItem(
                        file = file,
                        onShare = {
                            shareExcelFile(context, file)
                        },
                        onDelete = {
                            val deleted = file.delete()
                            if (deleted) {
                                generatedReports = generatedReports.filter { it != file }
                            } else {
                                Toast.makeText(context, "Dosya silinemedi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
    
    // New Report Dialog
    if (showNewReportDialog) {
        AlertDialog(
            onDismissRequest = { showNewReportDialog = false },
            title = {
                Text(
                    text = "Yeni Rapor Oluştur",
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        label = { Text("Rapor Başlığı") },
                        placeholder = { Text("Örn: Aylık Bakım Raporu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.glassBorder
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = operatorName,
                        onValueChange = { operatorName = it },
                        label = { Text("Operatör Adı") },
                        placeholder = { Text("Örn: Mehmet Yılmaz") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.glassBorder
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Açıklama") },
                        placeholder = { Text("Yapılan işlemler...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
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
                    onClick = {
                        if (reportTitle.isNotBlank()) {
                            isGenerating = true
                            scope.launch {
                                try {
                                    val file = generateExcelReport(
                                        excelService = excelService,
                                        title = reportTitle,
                                        operatorName = operatorName,
                                        description = description
                                    )
                                    generatedReports = listOf(file) + generatedReports
                                    showSuccessMessage = "Rapor başarıyla oluşturuldu: ${file.name}"
                                    showNewReportDialog = false
                                    reportTitle = ""
                                    operatorName = ""
                                    description = ""
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Rapor oluşturulurken hata: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isGenerating = false
                                }
                            }
                        }
                    },
                    enabled = reportTitle.isNotBlank() && !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = themeColors.background,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewReportDialog = false }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Success Snackbar
    showSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            showSuccessMessage = null
        }
    }
}

@Composable
private fun ReportItem(
    file: File,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = themeColors.secondary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.nameWithoutExtension,
                    style = MaterialTheme.typography.bodyLarge,
                    color = themeColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(file.lastModified())),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textSecondary
                )
                Text(
                    text = formatFileSize(file.length()),
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.textDisabled
                )
            }
            
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Paylaş",
                    tint = themeColors.primary
                )
            }
            
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = themeColors.error
                )
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Raporu Sil", color = themeColors.textPrimary) },
            text = {
                Text(
                    "Bu raporu silmek istediğinizden emin misiniz?",
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

private suspend fun generateExcelReport(
    excelService: ExcelService,
    title: String,
    operatorName: String,
    description: String
): File = withContext(Dispatchers.IO) {
    val workbook = excelService.createWorkbook()
    val sheet = excelService.createSheetWithHeader(workbook, "Bakım Raporu", title)
    val dataStyle = excelService.createDataStyle(workbook)
    
    var rowIndex = 5 // Start after header
    
    // Operator Info Section
    rowIndex = excelService.addSectionHeader(workbook, sheet, rowIndex, "Operatör Bilgileri")
    excelService.addDataRow(sheet, rowIndex++, listOf("Operatör:", operatorName), dataStyle)
    excelService.addDataRow(sheet, rowIndex++, listOf("Tarih:", 
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())), dataStyle)
    
    rowIndex++ // Empty row
    
    // Description Section
    rowIndex = excelService.addSectionHeader(workbook, sheet, rowIndex, "Yapılan İşlemler")
    excelService.addDataRow(sheet, rowIndex++, listOf("Açıklama:", description), dataStyle)
    
    rowIndex++ // Empty row
    
    // Status Section
    rowIndex = excelService.addSectionHeader(workbook, sheet, rowIndex, "Durum")
    excelService.addDataRow(sheet, rowIndex++, listOf("Rapor Durumu:", "Tamamlandı"), dataStyle)
    
    // Save
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "${title.replace(" ", "_")}_$timestamp.xlsx"
    val outputFile = File(excelService.getOutputDirectory(), fileName)
    
    excelService.saveWorkbook(workbook, outputFile.absolutePath)
    workbook.close()
    
    outputFile
}

private fun shareExcelFile(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Raporu Paylaş"))
    } catch (e: Exception) {
        Toast.makeText(context, "Paylaşım hatası: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
    }
}
