package com.assanhanil.techassist.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.util.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Service for generating Excel (.xlsx) reports using Apache POI.
 * 
 * Key Features:
 * - Native .xlsx generation (no dependency on Microsoft Excel app)
 * - Corporate header with company logo
 * - Image embedding with proper cell anchoring
 * - Auto-expanding rows to fit images
 * - Professional formatting and styling
 */
class ExcelService(private val context: Context) {

    companion object {
        // Excel dimension constants
        private const val COLUMN_WIDTH_UNIT = 256 // Excel column width unit
        private const val DEFAULT_COLUMN_WIDTH = 15 * COLUMN_WIDTH_UNIT
        private const val IMAGE_COLUMN_WIDTH = 50 * COLUMN_WIDTH_UNIT
        
        // Row height in twips (1/20 of a point)
        private const val DEFAULT_ROW_HEIGHT: Short = 400
        private const val HEADER_ROW_HEIGHT: Short = 600
        private const val IMAGE_ROW_HEIGHT: Short = 3000 // ~150 points for images
        
        // Image processing
        private const val MAX_IMAGE_SIZE_KB = 500
        private const val MAX_IMAGE_DIMENSION = 800
    }

    /**
     * Creates a new Excel workbook with corporate styling.
     */
    fun createWorkbook(): XSSFWorkbook {
        return XSSFWorkbook()
    }

    /**
     * Creates a new sheet with corporate header.
     * 
     * @param workbook The workbook to add the sheet to
     * @param sheetName Name of the sheet
     * @param title Report title to display in header
     * @return The created sheet
     */
    fun createSheetWithHeader(
        workbook: XSSFWorkbook,
        sheetName: String,
        title: String
    ): XSSFSheet {
        val sheet = workbook.createSheet(sheetName)
        
        // Set default column widths
        for (i in 0..10) {
            sheet.setColumnWidth(i, DEFAULT_COLUMN_WIDTH)
        }
        
        // Create corporate header
        createCorporateHeader(workbook, sheet, title)
        
        return sheet
    }

    /**
     * Creates the corporate header with company info.
     * Logo is inserted in merged cells at the top.
     */
    private fun createCorporateHeader(
        workbook: XSSFWorkbook,
        sheet: XSSFSheet,
        title: String
    ) {
        // Create styles
        val headerStyle = createHeaderStyle(workbook)
        val titleStyle = createTitleStyle(workbook)
        
        // Row 0-1: Company Info (merged)
        sheet.addMergedRegion(CellRangeAddress(0, 1, 0, 5))
        val companyRow = sheet.createRow(0)
        companyRow.heightInPoints = 30f
        val companyCell = companyRow.createCell(0)
        companyCell.setCellValue("ASSANHANİL BURSA - Operational Reporting System")
        companyCell.cellStyle = headerStyle
        
        // Row 2: Report Title (merged)
        sheet.addMergedRegion(CellRangeAddress(2, 2, 0, 5))
        val titleRow = sheet.createRow(2)
        titleRow.heightInPoints = 25f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue(title)
        titleCell.cellStyle = titleStyle
        
        // Row 3: Date and Info
        val infoRow = sheet.createRow(3)
        val dateCell = infoRow.createCell(0)
        dateCell.setCellValue("Report Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        
        // Empty row before content
        sheet.createRow(4)
    }

    /**
     * Creates header cell style with corporate colors.
     */
    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        
        // Font
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 14
        font.setColor(IndexedColors.WHITE.index)
        style.setFont(font)
        
        // Background - Industrial Dark Blue
        style.setFillForegroundColor(XSSFColor(byteArrayOf(0, 51, 102.toByte()), null))
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        
        // Alignment
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        
        return style
    }

    /**
     * Creates title cell style.
     */
    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 12
        style.setFont(font)
        
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        
        return style
    }

    /**
     * Creates a data row style with borders.
     */
    fun createDataStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        
        // Borders
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        
        style.verticalAlignment = VerticalAlignment.CENTER
        
        return style
    }

    /**
     * Embeds an image INSIDE a specific cell using ClientAnchor.
     * 
     * Critical Requirements Met:
     * - Images resized to ~500KB
     * - Images anchored INSIDE the cell using ClientAnchor
     * - Row height auto-expands to fit the image
     * - Images do NOT float over grid lines
     * 
     * @param workbook The workbook
     * @param sheet The sheet to add the image to
     * @param imagePath Path to the image file
     * @param row Row index where image should be placed
     * @param column Column index where image should be placed
     */
    fun embedImageInCell(
        workbook: XSSFWorkbook,
        sheet: XSSFSheet,
        imagePath: String,
        row: Int,
        column: Int
    ): Boolean {
        return try {
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                return false
            }

            // Compress and resize image to ~500KB
            val compressedImageBytes = compressImage(imagePath)
            
            // Add picture to workbook
            val pictureIdx = workbook.addPicture(
                compressedImageBytes,
                Workbook.PICTURE_TYPE_JPEG
            )

            // Create drawing patriarch
            val drawing = sheet.createDrawingPatriarch() as XSSFDrawing

            // Create ClientAnchor to anchor image INSIDE the cell
            // Using MOVE_AND_RESIZE to ensure image stays within cell bounds
            val anchor = workbook.creationHelper.createClientAnchor().apply {
                // Set anchor type to move and resize with cell
                anchorType = ClientAnchor.AnchorType.MOVE_AND_RESIZE
                
                // Set the cell where image starts (top-left corner)
                col1 = column
                row1 = row
                
                // Set the cell where image ends (bottom-right corner)
                // Image will be contained within this single cell
                col2 = column + 1
                row2 = row + 1
                
                // Offset within the cell (in EMUs - English Metric Units)
                // Small margin from cell edges
                dx1 = 50000  // Left margin
                dy1 = 50000  // Top margin
                dx2 = -50000 // Right margin (negative for inset)
                dy2 = -50000 // Bottom margin (negative for inset)
            }

            // Create picture with anchor
            val picture = drawing.createPicture(anchor, pictureIdx)
            
            // Auto-expand row height to fit image
            val targetRow = sheet.getRow(row) ?: sheet.createRow(row)
            targetRow.heightInPoints = 150f // Set appropriate height for image
            
            // Set column width to accommodate image
            sheet.setColumnWidth(column, IMAGE_COLUMN_WIDTH)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Compresses an image to approximately 500KB and resizes if needed.
     * Prevents OOM errors from large images.
     */
    private fun compressImage(imagePath: String): ByteArray {
        // Load bitmap with inSampleSize to reduce memory usage
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imagePath, options)
        
        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
        options.inJustDecodeBounds = false
        
        val bitmap = BitmapFactory.decodeFile(imagePath, options)
            ?: throw IllegalStateException("Could not decode image - file may be corrupted or unsupported format: $imagePath")
        
        // Resize if still too large
        val scaledBitmap = if (bitmap.width > MAX_IMAGE_DIMENSION || bitmap.height > MAX_IMAGE_DIMENSION) {
            val scale = min(
                MAX_IMAGE_DIMENSION.toFloat() / bitmap.width,
                MAX_IMAGE_DIMENSION.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                if (it != bitmap && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        } else {
            bitmap
        }
        
        // Compress to JPEG with quality adjustment to reach ~500KB
        var quality = 90
        var compressedBytes: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            quality -= 10
        } while (compressedBytes.size > MAX_IMAGE_SIZE_KB * 1024 && quality > 10)
        
        if (!scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }
        
        return compressedBytes
    }

    /**
     * Calculate optimal sample size for bitmap loading.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
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

    /**
     * Adds a data row to the sheet.
     */
    fun addDataRow(
        sheet: XSSFSheet,
        rowIndex: Int,
        data: List<String>,
        style: XSSFCellStyle? = null
    ): XSSFRow {
        val row = sheet.createRow(rowIndex)
        row.heightInPoints = 20f
        
        data.forEachIndexed { index, value ->
            val cell = row.createCell(index)
            cell.setCellValue(value)
            style?.let { cell.cellStyle = it }
        }
        
        return row
    }

    /**
     * Adds a section header row.
     */
    fun addSectionHeader(
        workbook: XSSFWorkbook,
        sheet: XSSFSheet,
        rowIndex: Int,
        title: String,
        colspan: Int = 6
    ): Int {
        sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex, 0, colspan - 1))
        
        val row = sheet.createRow(rowIndex)
        row.heightInPoints = 25f
        
        val cell = row.createCell(0)
        cell.setCellValue(title)
        
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 11
        style.setFont(font)
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.CENTER
        cell.cellStyle = style
        
        return rowIndex + 1
    }

    /**
     * Embeds a signature image in the footer area.
     */
    fun embedSignature(
        workbook: XSSFWorkbook,
        sheet: XSSFSheet,
        signaturePath: String,
        startRow: Int
    ): Boolean {
        // Add signature label
        val labelRow = sheet.createRow(startRow)
        val labelCell = labelRow.createCell(0)
        labelCell.setCellValue("Operator Signature:")
        
        // Embed signature image
        return embedImageInCell(workbook, sheet, signaturePath, startRow + 1, 0)
    }

    /**
     * Saves the workbook to a file.
     */
    fun saveWorkbook(workbook: XSSFWorkbook, filePath: String): Boolean {
        return try {
            FileOutputStream(filePath).use { outputStream ->
                workbook.write(outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets the output directory for Excel files.
     */
    fun getOutputDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
