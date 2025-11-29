package com.assanhanil.techassist.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Text Recognition Helper using Google ML Kit.
 * Provides OCR functionality to extract text from images.
 */
class TextRecognitionHelper {
    
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Recognize text from a bitmap image.
     * 
     * @param bitmap The image to analyze
     * @return TextRecognitionResult containing the recognized text and blocks
     */
    suspend fun recognizeText(bitmap: Bitmap): TextRecognitionResult {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val blocks = visionText.textBlocks.map { block ->
                        TextBlock(
                            text = block.text,
                            boundingBox = block.boundingBox?.let { 
                                BoundingBox(it.left, it.top, it.right, it.bottom) 
                            },
                            lines = block.lines.map { line ->
                                TextLine(
                                    text = line.text,
                                    boundingBox = line.boundingBox?.let {
                                        BoundingBox(it.left, it.top, it.right, it.bottom)
                                    },
                                    elements = line.elements.map { element ->
                                        TextElement(
                                            text = element.text,
                                            boundingBox = element.boundingBox?.let {
                                                BoundingBox(it.left, it.top, it.right, it.bottom)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                    
                    continuation.resume(
                        TextRecognitionResult.Success(
                            fullText = visionText.text,
                            blocks = blocks
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        TextRecognitionResult.Error(
                            message = exception.message ?: "Metin tanıma başarısız",
                            exception = exception
                        )
                    )
                }
            
            continuation.invokeOnCancellation {
                // Recognition will complete naturally
            }
        }
    }

    /**
     * Recognize text and extract specific patterns (like bearing codes, serial numbers, etc.)
     */
    suspend fun recognizeAndExtract(bitmap: Bitmap, patterns: List<ExtractionPattern>): List<ExtractedValue> {
        val result = recognizeText(bitmap)
        
        return when (result) {
            is TextRecognitionResult.Success -> {
                val extractedValues = mutableListOf<ExtractedValue>()
                
                patterns.forEach { pattern ->
                    val regex = Regex(pattern.regex, RegexOption.IGNORE_CASE)
                    val matches = regex.findAll(result.fullText)
                    
                    matches.forEach { match ->
                        extractedValues.add(
                            ExtractedValue(
                                patternName = pattern.name,
                                value = match.value,
                                startIndex = match.range.first,
                                endIndex = match.range.last
                            )
                        )
                    }
                }
                
                extractedValues
            }
            is TextRecognitionResult.Error -> emptyList()
        }
    }

    /**
     * Close the recognizer when done.
     */
    fun close() {
        recognizer.close()
    }

    companion object {
        /**
         * Common patterns for industrial applications.
         */
        val COMMON_PATTERNS = listOf(
            ExtractionPattern("bearing_code", "[0-9]{4,5}[-][A-Z]{2,3}"),
            ExtractionPattern("serial_number", "[A-Z]{2,3}[0-9]{6,10}"),
            ExtractionPattern("date", "[0-9]{2}[/.-][0-9]{2}[/.-][0-9]{2,4}"),
            ExtractionPattern("dimension", "[0-9]+([.,][0-9]+)?\\s*(mm|cm|m)"),
            ExtractionPattern("voltage", "[0-9]+([.,][0-9]+)?\\s*(V|kV|mV)"),
            ExtractionPattern("current", "[0-9]+([.,][0-9]+)?\\s*(A|mA|kA)"),
            ExtractionPattern("temperature", "[0-9]+([.,][0-9]+)?\\s*(°C|°F|C|F)"),
            ExtractionPattern("pressure", "[0-9]+([.,][0-9]+)?\\s*(bar|psi|Pa|kPa|MPa)")
        )
    }
}

/**
 * Result of text recognition operation.
 */
sealed class TextRecognitionResult {
    data class Success(
        val fullText: String,
        val blocks: List<TextBlock>
    ) : TextRecognitionResult()
    
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : TextRecognitionResult()
}

/**
 * A block of text recognized in the image.
 */
data class TextBlock(
    val text: String,
    val boundingBox: BoundingBox?,
    val lines: List<TextLine>
)

/**
 * A line of text within a block.
 */
data class TextLine(
    val text: String,
    val boundingBox: BoundingBox?,
    val elements: List<TextElement>
)

/**
 * A single word/element of text.
 */
data class TextElement(
    val text: String,
    val boundingBox: BoundingBox?
)

/**
 * Bounding box coordinates for text location.
 */
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

/**
 * Pattern definition for text extraction.
 */
data class ExtractionPattern(
    val name: String,
    val regex: String
)

/**
 * Value extracted from recognized text.
 */
data class ExtractedValue(
    val patternName: String,
    val value: String,
    val startIndex: Int,
    val endIndex: Int
)
