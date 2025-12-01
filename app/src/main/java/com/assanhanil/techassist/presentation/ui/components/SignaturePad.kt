package com.assanhanil.techassist.presentation.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors

/**
 * A signature pad component that allows operators to sign with their finger.
 * The signature is captured as a Bitmap that can be embedded in Excel reports.
 * 
 * @param modifier Modifier for the component
 * @param operatorName The name of the operator signing
 * @param height The height of the signature area
 * @param strokeWidth The width of the signature stroke
 * @param strokeColor The color of the signature stroke
 * @param backgroundColor The background color of the signature area
 * @param onSignatureChanged Callback when signature changes (bitmap or null if cleared)
 */
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    operatorName: String = "",
    height: Dp = 200.dp,
    strokeWidth: Float = 5f,
    strokeColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    onSignatureChanged: (Bitmap?) -> Unit = {}
) {
    val themeColors = LocalThemeColors.current
    
    // Store the path points
    var pathPoints by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var hasSignature by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Operator name label
        if (operatorName.isNotEmpty()) {
            Text(
                text = operatorName,
                style = MaterialTheme.typography.titleMedium,
                color = themeColors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Signature canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = if (hasSignature) themeColors.primary else themeColors.glassBorder,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Drawing canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath = currentPath + change.position
                            },
                            onDragEnd = {
                                if (currentPath.isNotEmpty()) {
                                    pathPoints = pathPoints + listOf(currentPath)
                                    currentPath = emptyList()
                                    hasSignature = true
                                    
                                    // Generate bitmap and notify
                                    val bitmap = generateSignatureBitmap(
                                        pathPoints = pathPoints,
                                        width = size.width.toInt(),
                                        height = size.height.toInt(),
                                        strokeWidth = strokeWidth,
                                        strokeColor = strokeColor
                                    )
                                    onSignatureChanged(bitmap)
                                }
                            }
                        )
                    }
            ) {
                // Draw all completed paths
                pathPoints.forEach { path ->
                    if (path.size >= 2) {
                        val composePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(path[0].x, path[0].y)
                            for (i in 1 until path.size) {
                                lineTo(path[i].x, path[i].y)
                            }
                        }
                        drawPath(
                            path = composePath,
                            color = strokeColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
                
                // Draw current path being drawn
                if (currentPath.size >= 2) {
                    val composePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(currentPath[0].x, currentPath[0].y)
                        for (i in 1 until currentPath.size) {
                            lineTo(currentPath[i].x, currentPath[i].y)
                        }
                    }
                    drawPath(
                        path = composePath,
                        color = strokeColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
            
            // Placeholder text
            if (!hasSignature) {
                Text(
                    text = "İmzanızı buraya çizin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.textDisabled,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Clear button
            if (hasSignature) {
                IconButton(
                    onClick = {
                        pathPoints = emptyList()
                        currentPath = emptyList()
                        hasSignature = false
                        onSignatureChanged(null)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Temizle",
                        tint = themeColors.error
                    )
                }
            }
        }
        
        // Signature line
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = themeColors.textSecondary,
            thickness = 1.dp
        )
        
        Text(
            text = "İmza",
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.textSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Generates a bitmap from the signature path points.
 */
private fun generateSignatureBitmap(
    pathPoints: List<List<Offset>>,
    width: Int,
    height: Int,
    strokeWidth: Float,
    strokeColor: Color
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Fill with white background
    canvas.drawColor(android.graphics.Color.WHITE)
    
    // Set up paint for signature
    val paint = Paint().apply {
        color = strokeColor.toArgb()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true
        this.strokeWidth = strokeWidth
    }
    
    // Draw all paths
    pathPoints.forEach { path ->
        if (path.size >= 2) {
            val androidPath = Path().apply {
                moveTo(path[0].x, path[0].y)
                for (i in 1 until path.size) {
                    lineTo(path[i].x, path[i].y)
                }
            }
            canvas.drawPath(androidPath, paint)
        }
    }
    
    return bitmap
}

/**
 * Data class to hold an operator's signature.
 */
data class OperatorSignature(
    val operatorId: Long,
    val operatorName: String,
    val signatureBitmap: Bitmap?
)
