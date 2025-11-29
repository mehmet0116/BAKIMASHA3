package com.assanhanil.techassist.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for VoiceCommandParser.
 */
class VoiceCommandParserTest {

    @Test
    fun `parse photo command returns TakePhoto`() {
        // When
        val result = VoiceCommandParser.parse("fotoğraf çek")
        
        // Then
        assertTrue(result is VoiceCommand.TakePhoto)
    }

    @Test
    fun `parse resim command returns TakePhoto`() {
        // When
        val result = VoiceCommandParser.parse("resim çek")
        
        // Then
        assertTrue(result is VoiceCommand.TakePhoto)
    }

    @Test
    fun `parse save command returns Save`() {
        // When
        val result = VoiceCommandParser.parse("kaydet")
        
        // Then
        assertTrue(result is VoiceCommand.Save)
    }

    @Test
    fun `parse cancel command returns Cancel`() {
        // When
        val result = VoiceCommandParser.parse("iptal")
        
        // Then
        assertTrue(result is VoiceCommand.Cancel)
    }

    @Test
    fun `parse vazgeç command returns Cancel`() {
        // When
        val result = VoiceCommandParser.parse("vazgeç")
        
        // Then
        assertTrue(result is VoiceCommand.Cancel)
    }

    @Test
    fun `parse delete command returns Delete`() {
        // When
        val result = VoiceCommandParser.parse("sil")
        
        // Then
        assertTrue(result is VoiceCommand.Delete)
    }

    @Test
    fun `parse new report command returns NewReport`() {
        // When
        val result = VoiceCommandParser.parse("yeni rapor oluştur")
        
        // Then
        assertTrue(result is VoiceCommand.NewReport)
    }

    @Test
    fun `parse new form command returns NewReport`() {
        // When
        val result = VoiceCommandParser.parse("yeni form aç")
        
        // Then
        assertTrue(result is VoiceCommand.NewReport)
    }

    @Test
    fun `parse add note command returns AddNote with text`() {
        // When
        val result = VoiceCommandParser.parse("not ekle bu bir test notudur")
        
        // Then
        assertTrue(result is VoiceCommand.AddNote)
        val addNote = result as VoiceCommand.AddNote
        assertEquals("bu bir test notudur", addNote.text)
    }

    @Test
    fun `parse set value with number returns SetValue`() {
        // When
        val result = VoiceCommandParser.parse("değer 123.5")
        
        // Then
        assertTrue(result is VoiceCommand.SetValue)
        val setValue = result as VoiceCommand.SetValue
        assertEquals("123.5", setValue.value)
    }

    @Test
    fun `parse unknown command returns Unknown with original text`() {
        // When
        val originalText = "bilinmeyen bir komut"
        val result = VoiceCommandParser.parse(originalText)
        
        // Then
        assertTrue(result is VoiceCommand.Unknown)
        val unknown = result as VoiceCommand.Unknown
        assertEquals(originalText, unknown.text)
    }

    @Test
    fun `parse command is case insensitive`() {
        // When
        val result = VoiceCommandParser.parse("KAYDET")
        
        // Then
        assertTrue(result is VoiceCommand.Save)
    }

    @Test
    fun `parse command with mixed case`() {
        // When
        val result = VoiceCommandParser.parse("Fotoğraf Çek")
        
        // Then
        assertTrue(result is VoiceCommand.TakePhoto)
    }
}
