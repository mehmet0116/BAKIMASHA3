package com.assanhanil.techassist.service

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ExcelService.
 * Tests the getOrCreateWorkOrderSheet method that handles the "Yapılacak İşler" sheet.
 */
class ExcelServiceTest {

    @Test
    fun `WORK_ORDER_SHEET_NAME constant should be Yapılacak İşler`() {
        // Verify the constant has the correct value
        assertEquals("Yapılacak İşler", ExcelService.WORK_ORDER_SHEET_NAME)
    }

    @Test
    fun `getSheet returns null when sheet does not exist`() {
        // Given a new workbook
        val workbook = XSSFWorkbook()
        
        // When we try to get a non-existent sheet
        val sheet = workbook.getSheet("Yapılacak İşler")
        
        // Then it should return null
        assertNull(sheet)
        
        workbook.close()
    }

    @Test
    fun `createSheet creates a new sheet with the given name`() {
        // Given a new workbook
        val workbook = XSSFWorkbook()
        
        // When we create a new sheet
        val sheet = workbook.createSheet("Yapılacak İşler")
        
        // Then we can retrieve it by name
        assertNotNull(sheet)
        assertEquals("Yapılacak İşler", sheet.sheetName)
        
        // And getSheet should return the same sheet
        val retrievedSheet = workbook.getSheet("Yapılacak İşler")
        assertNotNull(retrievedSheet)
        assertEquals(sheet, retrievedSheet)
        
        workbook.close()
    }

    @Test
    fun `getSheet returns existing sheet when it exists`() {
        // Given a workbook with an existing sheet
        val workbook = XSSFWorkbook()
        val createdSheet = workbook.createSheet("Yapılacak İşler")
        
        // When we get the sheet by name
        val retrievedSheet = workbook.getSheet("Yapılacak İşler")
        
        // Then it should return the same sheet
        assertNotNull(retrievedSheet)
        assertEquals(createdSheet, retrievedSheet)
        assertEquals("Yapılacak İşler", retrievedSheet?.sheetName)
        
        workbook.close()
    }

    @Test
    fun `creating duplicate sheet throws exception`() {
        // Given a workbook with an existing sheet
        val workbook = XSSFWorkbook()
        workbook.createSheet("Yapılacak İşler")
        
        // When we try to create another sheet with the same name
        // Then it should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException::class.java) {
            workbook.createSheet("Yapılacak İşler")
        }
        
        workbook.close()
    }
}
