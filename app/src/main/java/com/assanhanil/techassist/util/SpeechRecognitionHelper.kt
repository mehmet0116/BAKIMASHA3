package com.assanhanil.techassist.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

/**
 * Speech Recognition Helper for AI Voice Builder feature.
 * Provides voice-to-text functionality using Android's SpeechRecognizer.
 */
class SpeechRecognitionHelper(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    /**
     * Check if speech recognition is available on this device.
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for speech input.
     * Returns a Flow that emits speech recognition results.
     */
    fun startListening(languageCode: String = "tr-TR"): Flow<SpeechResult> = callbackFlow {
        if (!isAvailable()) {
            trySend(SpeechResult.Error("Konuşma tanıma bu cihazda kullanılamıyor"))
            close()
            return@callbackFlow
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechResult.Ready)
            }

            override fun onBeginningOfSpeech() {
                trySend(SpeechResult.Listening)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - can be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Sound buffer received
            }

            override fun onEndOfSpeech() {
                trySend(SpeechResult.Processing)
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Ses kaydı hatası"
                    SpeechRecognizer.ERROR_CLIENT -> "İstemci hatası"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "İzin gerekli"
                    SpeechRecognizer.ERROR_NETWORK -> "Ağ hatası"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Eşleşme bulunamadı"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Tanıyıcı meşgul"
                    SpeechRecognizer.ERROR_SERVER -> "Sunucu hatası"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Konuşma zaman aşımı"
                    else -> "Bilinmeyen hata: $error"
                }
                trySend(SpeechResult.Error(errorMessage))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    trySend(SpeechResult.Success(matches[0], matches))
                } else {
                    trySend(SpeechResult.Error("Sonuç alınamadı"))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    trySend(SpeechResult.PartialResult(matches[0]))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Speech recognition event
            }
        })

        speechRecognizer?.startListening(recognizerIntent)

        awaitClose {
            stopListening()
        }
    }

    /**
     * Stop listening for speech.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    /**
     * Cancel current recognition.
     */
    fun cancel() {
        speechRecognizer?.cancel()
    }
}

/**
 * Result states from speech recognition.
 */
sealed class SpeechResult {
    data object Ready : SpeechResult()
    data object Listening : SpeechResult()
    data object Processing : SpeechResult()
    data class PartialResult(val text: String) : SpeechResult()
    data class Success(val text: String, val alternatives: List<String> = emptyList()) : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}

/**
 * Voice command parser for common maintenance operations.
 * Parses voice commands into structured data.
 */
object VoiceCommandParser {
    
    private val numberWords = mapOf(
        "sıfır" to 0, "bir" to 1, "iki" to 2, "üç" to 3, "dört" to 4,
        "beş" to 5, "altı" to 6, "yedi" to 7, "sekiz" to 8, "dokuz" to 9,
        "on" to 10, "yirmi" to 20, "otuz" to 30, "kırk" to 40, "elli" to 50,
        "altmış" to 60, "yetmiş" to 70, "seksen" to 80, "doksan" to 90, "yüz" to 100
    )

    /**
     * Parse a voice command into a VoiceCommand object.
     */
    fun parse(text: String): VoiceCommand {
        val lowerText = text.lowercase(Locale.getDefault())
        
        return when {
            lowerText.contains("fotoğraf çek") || lowerText.contains("resim çek") -> 
                VoiceCommand.TakePhoto
            
            lowerText.contains("kaydet") -> 
                VoiceCommand.Save
            
            lowerText.contains("iptal") || lowerText.contains("vazgeç") -> 
                VoiceCommand.Cancel
            
            lowerText.contains("sil") -> 
                VoiceCommand.Delete
            
            lowerText.contains("yeni") && (lowerText.contains("rapor") || lowerText.contains("form")) -> 
                VoiceCommand.NewReport
            
            lowerText.contains("not ekle") || lowerText.contains("not yaz") -> {
                val noteText = extractTextAfter(lowerText, listOf("not ekle", "not yaz"))
                VoiceCommand.AddNote(noteText)
            }
            
            lowerText.contains("değer") || lowerText.contains("yaz") -> {
                val value = extractValue(lowerText)
                VoiceCommand.SetValue(value)
            }
            
            else -> VoiceCommand.Unknown(text)
        }
    }

    /**
     * Extract text after trigger phrases.
     */
    private fun extractTextAfter(text: String, triggers: List<String>): String {
        for (trigger in triggers) {
            val index = text.indexOf(trigger)
            if (index >= 0) {
                return text.substring(index + trigger.length).trim()
            }
        }
        return ""
    }

    /**
     * Extract numeric value from text.
     */
    private fun extractValue(text: String): String {
        // Try to find numeric value
        val numericPattern = Regex("\\d+([.,]\\d+)?")
        val match = numericPattern.find(text)
        if (match != null) {
            return match.value.replace(",", ".")
        }
        
        // Try to convert word numbers
        var total = 0
        val words = text.split(" ")
        for (word in words) {
            numberWords[word]?.let { total += it }
        }
        
        return if (total > 0) total.toString() else text
    }
}

/**
 * Voice command types that can be recognized.
 */
sealed class VoiceCommand {
    data object TakePhoto : VoiceCommand()
    data object Save : VoiceCommand()
    data object Cancel : VoiceCommand()
    data object Delete : VoiceCommand()
    data object NewReport : VoiceCommand()
    data class AddNote(val text: String) : VoiceCommand()
    data class SetValue(val value: String) : VoiceCommand()
    data class Unknown(val text: String) : VoiceCommand()
}
