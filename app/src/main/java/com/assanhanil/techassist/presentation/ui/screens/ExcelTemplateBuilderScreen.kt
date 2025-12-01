package com.assanhanil.techassist.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.domain.model.*
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors
import com.assanhanil.techassist.presentation.viewmodel.ExcelTemplateViewModel
import com.assanhanil.techassist.service.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Excel Template Builder Screen.
 * Allows users to create custom Excel templates by:
 * - Adding/removing columns with custom names and widths
 * - Adding/removing rows
 * - Setting cell values
 * - Generating Excel files from the template
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelTemplateBuilderScreen(
    excelService: ExcelService,
    excelTemplateViewModel: ExcelTemplateViewModel,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Template state
    var templateName by remember { mutableStateOf("Yeni Şablon") }
    var columns by remember { mutableStateOf(listOf(
        TemplateColumn(id = 1, name = "Sütun 1", width = 15),
        TemplateColumn(id = 2, name = "Sütun 2", width = 15),
        TemplateColumn(id = 3, name = "Sütun 3", width = 15)
    )) }
    
    // Initialize with header row and some data rows
    var rows by remember { mutableStateOf(listOf(
        TemplateRow(
            id = 1, 
            rowIndex = 0, 
            isHeader = true,
            cells = listOf(
                TemplateCell(columnId = 1, value = "Başlık 1", isEditable = true),
                TemplateCell(columnId = 2, value = "Başlık 2", isEditable = true),
                TemplateCell(columnId = 3, value = "Başlık 3", isEditable = true)
            )
        ),
        TemplateRow(
            id = 2, 
            rowIndex = 1, 
            isHeader = false,
            cells = listOf(
                TemplateCell(columnId = 1, value = "", isEditable = true),
                TemplateCell(columnId = 2, value = "", isEditable = true),
                TemplateCell(columnId = 3, value = "", isEditable = true)
            )
        )
    )) }
    
    var showAddColumnDialog by remember { mutableStateOf(false) }
    var showEditColumnDialog by remember { mutableStateOf<TemplateColumn?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var templateDescription by remember { mutableStateOf("") }
    var selectedCellPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var editingCellValue by remember { mutableStateOf("") }
    
    // Observe save success state
    val saveSuccess by excelTemplateViewModel.saveSuccess.collectAsState()
    val saveError by excelTemplateViewModel.error.collectAsState()
    
    // Handle save success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "Şablon başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
            excelTemplateViewModel.resetSaveSuccess()
            showSaveDialog = false
            isSaving = false
        }
    }
    
    // Handle save error
    LaunchedEffect(saveError) {
        saveError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            excelTemplateViewModel.clearError()
            isSaving = false
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
                    text = "Excel Şablon Oluşturucu",
                    style = MaterialTheme.typography.headlineSmall,
                    color = themeColors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kendi özel Excel şablonunuzu oluşturun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Template Name Input
        OutlinedTextField(
            value = templateName,
            onValueChange = { templateName = it },
            label = { Text("Şablon Adı") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = themeColors.primary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.primary,
                unfocusedBorderColor = themeColors.glassBorder
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Column Button
            FilledTonalButton(
                onClick = { showAddColumnDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = themeColors.primary.copy(alpha = 0.2f),
                    contentColor = themeColors.primary
                )
            ) {
                Icon(Icons.Default.ViewColumn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sütun Ekle")
            }
            
            // Add Row Button
            FilledTonalButton(
                onClick = {
                    val newRowId = (rows.maxOfOrNull { it.id } ?: 0) + 1
                    val newCells = columns.map { col ->
                        TemplateCell(columnId = col.id, value = "", isEditable = true)
                    }
                    rows = rows + TemplateRow(
                        id = newRowId,
                        rowIndex = rows.size,
                        isHeader = false,
                        cells = newCells
                    )
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = themeColors.secondary.copy(alpha = 0.2f),
                    contentColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.TableRows, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Satır Ekle")
            }
            
            // Delete Row Button
            if (rows.size > 1) {
                FilledTonalButton(
                    onClick = {
                        if (rows.size > 1) {
                            rows = rows.dropLast(1)
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = themeColors.error.copy(alpha = 0.2f),
                        contentColor = themeColors.error
                    )
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Satır Sil")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Excel Preview Area
        Text(
            text = "Şablon Önizleme",
            style = MaterialTheme.typography.titleSmall,
            color = themeColors.textSecondary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Scrollable table
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Column headers with edit/delete buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Row number column
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(48.dp)
                            .background(themeColors.surfaceVariant)
                            .border(0.5.dp, themeColors.glassBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#",
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    columns.forEach { column ->
                        Box(
                            modifier = Modifier
                                .width((column.width * 8).dp)
                                .height(48.dp)
                                .background(themeColors.primary.copy(alpha = 0.1f))
                                .border(0.5.dp, themeColors.glassBorder)
                                .clickable { showEditColumnDialog = column },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = column.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.primary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Düzenle",
                                    tint = themeColors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                
                // Data rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(rows) { rowIndex, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Row number
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .background(themeColors.surfaceVariant)
                                    .border(0.5.dp, themeColors.glassBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rowIndex + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.textSecondary
                                )
                            }
                            
                            // Cells
                            columns.forEachIndexed { colIndex, column ->
                                val cell = row.cells.find { it.columnId == column.id }
                                val isSelected = selectedCellPosition == Pair(rowIndex, colIndex)
                                
                                Box(
                                    modifier = Modifier
                                        .width((column.width * 8).dp)
                                        .height(40.dp)
                                        .background(
                                            if (row.isHeader) themeColors.secondary.copy(alpha = 0.1f)
                                            else if (isSelected) themeColors.primary.copy(alpha = 0.2f)
                                            else themeColors.surface
                                        )
                                        .border(
                                            if (isSelected) 2.dp else 0.5.dp,
                                            if (isSelected) themeColors.primary else themeColors.glassBorder
                                        )
                                        .clickable {
                                            selectedCellPosition = Pair(rowIndex, colIndex)
                                            editingCellValue = cell?.value ?: ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        OutlinedTextField(
                                            value = editingCellValue,
                                            onValueChange = { newValue ->
                                                editingCellValue = newValue
                                                // Update the cell value
                                                rows = rows.mapIndexed { rIdx, r ->
                                                    if (rIdx == rowIndex) {
                                                        r.copy(
                                                            cells = r.cells.map { c ->
                                                                if (c.columnId == column.id) {
                                                                    c.copy(value = newValue)
                                                                } else c
                                                            }
                                                        )
                                                    } else r
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp),
                                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                                textAlign = TextAlign.Center,
                                                color = themeColors.textPrimary
                                            ),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = themeColors.primary,
                                                unfocusedBorderColor = themeColors.glassBorder.copy(alpha = 0f)
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = cell?.value ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (row.isHeader) themeColors.secondary else themeColors.textPrimary,
                                            fontWeight = if (row.isHeader) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Generate Excel Button
            Button(
                onClick = {
                    if (templateName.isNotBlank()) {
                        isGenerating = true
                        scope.launch {
                            try {
                                val file = generateExcelFromTemplate(
                                    excelService = excelService,
                                    templateName = templateName,
                                    columns = columns,
                                    rows = rows
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
                                isGenerating = false
                            }
                        }
                    }
                },
                enabled = templateName.isNotBlank() && !isGenerating,
                modifier = Modifier.weight(1f),
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
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Excel Oluştur")
            }
            
            // Save Template Button
            OutlinedButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeColors.secondary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Şablonu Kaydet")
            }
        }
    }
    
    // Add Column Dialog
    if (showAddColumnDialog) {
        var newColumnName by remember { mutableStateOf("") }
        var newColumnWidth by remember { mutableStateOf("15") }
        
        AlertDialog(
            onDismissRequest = { showAddColumnDialog = false },
            title = {
                Text(
                    text = "Yeni Sütun Ekle",
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newColumnName,
                        onValueChange = { newColumnName = it },
                        label = { Text("Sütun Adı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.glassBorder
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newColumnWidth,
                        onValueChange = { newColumnWidth = it.filter { char -> char.isDigit() } },
                        label = { Text("Sütun Genişliği") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        if (newColumnName.isNotBlank()) {
                            val newId = (columns.maxOfOrNull { it.id } ?: 0) + 1
                            val width = newColumnWidth.toIntOrNull() ?: 15
                            val newColumn = TemplateColumn(
                                id = newId,
                                name = newColumnName,
                                width = width.coerceIn(5, 50)
                            )
                            columns = columns + newColumn
                            
                            // Add cell to each row for new column
                            rows = rows.map { row ->
                                row.copy(
                                    cells = row.cells + TemplateCell(
                                        columnId = newId,
                                        value = if (row.isHeader) newColumnName else "",
                                        isEditable = true
                                    )
                                )
                            }
                            
                            showAddColumnDialog = false
                        }
                    },
                    enabled = newColumnName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary
                    )
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddColumnDialog = false }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Edit Column Dialog
    showEditColumnDialog?.let { column ->
        var editColumnName by remember { mutableStateOf(column.name) }
        var editColumnWidth by remember { mutableStateOf(column.width.toString()) }
        
        AlertDialog(
            onDismissRequest = { showEditColumnDialog = null },
            title = {
                Text(
                    text = "Sütunu Düzenle",
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editColumnName,
                        onValueChange = { editColumnName = it },
                        label = { Text("Sütun Adı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.glassBorder
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = editColumnWidth,
                        onValueChange = { editColumnWidth = it.filter { char -> char.isDigit() } },
                        label = { Text("Sütun Genişliği") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primary,
                            unfocusedBorderColor = themeColors.glassBorder
                        )
                    )
                    
                    if (columns.size > 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {
                                columns = columns.filter { it.id != column.id }
                                rows = rows.map { row ->
                                    row.copy(cells = row.cells.filter { it.columnId != column.id })
                                }
                                showEditColumnDialog = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = themeColors.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sütunu Sil")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editColumnName.isNotBlank()) {
                            val width = editColumnWidth.toIntOrNull() ?: 15
                            columns = columns.map {
                                if (it.id == column.id) {
                                    it.copy(name = editColumnName, width = width.coerceIn(5, 50))
                                } else it
                            }
                            showEditColumnDialog = null
                        }
                    },
                    enabled = editColumnName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary
                    )
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditColumnDialog = null }) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
    
    // Save Template Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isSaving) {
                    showSaveDialog = false 
                }
            },
            title = {
                Text(
                    text = "Şablonu Kaydet",
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "\"$templateName\" şablonu kaydedilecek. Bu şablonu daha sonra tekrar kullanabilirsiniz.",
                        color = themeColors.textSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = templateDescription,
                        onValueChange = { templateDescription = it },
                        label = { Text("Açıklama (isteğe bağlı)") },
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
                    onClick = {
                        isSaving = true
                        excelTemplateViewModel.saveTemplateFromBuilder(
                            name = templateName,
                            description = templateDescription,
                            columns = columns,
                            rows = rows
                        )
                    },
                    enabled = !isSaving && templateName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.secondary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = themeColors.background,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Kaydediliyor..." else "Kaydet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false },
                    enabled = !isSaving
                ) {
                    Text("İptal", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

/**
 * Generates an Excel file from the custom template.
 */
private suspend fun generateExcelFromTemplate(
    excelService: ExcelService,
    templateName: String,
    columns: List<TemplateColumn>,
    rows: List<TemplateRow>
): File = withContext(Dispatchers.IO) {
    val workbook = excelService.createWorkbook()
    val sheet = workbook.createSheet(templateName)
    
    // Set column widths
    columns.forEachIndexed { index, column ->
        sheet.setColumnWidth(index, column.width * 256)
    }
    
    // Create styles
    val headerStyle = workbook.createCellStyle().apply {
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 11
        setFont(font)
        
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        
        alignment = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
        
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }
    
    val dataStyle = workbook.createCellStyle().apply {
        verticalAlignment = VerticalAlignment.CENTER
        
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }
    
    // Create rows
    rows.forEachIndexed { rowIndex, templateRow ->
        val excelRow = sheet.createRow(rowIndex)
        excelRow.heightInPoints = templateRow.height
        
        columns.forEachIndexed { colIndex, column ->
            val cell = excelRow.createCell(colIndex)
            val templateCell = templateRow.cells.find { it.columnId == column.id }
            cell.setCellValue(templateCell?.value ?: "")
            cell.cellStyle = if (templateRow.isHeader) headerStyle else dataStyle
        }
    }
    
    // Save file
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "${templateName.replace(" ", "_")}_$timestamp.xlsx"
    val outputFile = File(excelService.getOutputDirectory(), fileName)
    
    excelService.saveWorkbook(workbook, outputFile.absolutePath)
    workbook.close()
    
    outputFile
}
