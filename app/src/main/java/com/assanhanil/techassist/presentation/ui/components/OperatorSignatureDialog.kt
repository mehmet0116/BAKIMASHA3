package com.assanhanil.techassist.presentation.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.presentation.ui.theme.LocalThemeColors

/**
 * Dialog that collects signatures from all selected operators before exporting/sending Excel.
 * Each operator gets a large signature area to sign with their finger.
 * 
 * @param operators List of operators who need to sign
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback with list of operator signatures when confirmed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorSignatureDialog(
    operators: List<Operator>,
    onDismiss: () -> Unit,
    onConfirm: (List<OperatorSignature>) -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    // Track signatures for each operator
    val signatures = remember { mutableStateMapOf<Long, Bitmap?>() }
    
    // Initialize signatures map
    LaunchedEffect(operators) {
        operators.forEach { operator ->
            if (!signatures.containsKey(operator.id)) {
                signatures[operator.id] = null
            }
        }
    }
    
    // Check if all operators have signed
    val allSigned = operators.all { signatures[it.id] != null }
    val signedCount = operators.count { signatures[it.id] != null }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            color = themeColors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = null,
                                tint = themeColors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Operatör İmzaları",
                                style = MaterialTheme.typography.headlineSmall,
                                color = themeColors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Raporu göndermeden önce tüm operatörlerin imzalaması gerekiyor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.textSecondary
                        )
                    }
                    
                    // Progress indicator
                    Badge(
                        containerColor = if (allSigned) themeColors.success else themeColors.warning,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "$signedCount/${operators.size}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Signature areas for each operator
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    operators.forEachIndexed { index, operator ->
                        val hasSigned = signatures[operator.id] != null
                        
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Operator header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = themeColors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = operator.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = themeColors.textPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (operator.department.isNotEmpty()) {
                                                Text(
                                                    text = operator.department,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = themeColors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Signed indicator
                                    if (hasSigned) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "İmzalandı",
                                                tint = themeColors.success,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "İmzalandı",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = themeColors.success,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Signature pad
                                SignaturePad(
                                    modifier = Modifier.fillMaxWidth(),
                                    operatorName = "",
                                    height = 180.dp,
                                    strokeWidth = 4f,
                                    strokeColor = Color.Black,
                                    backgroundColor = Color.White,
                                    onSignatureChanged = { bitmap ->
                                        signatures[operator.id] = bitmap
                                    }
                                )
                            }
                        }
                        
                        // Divider between operators
                        if (index < operators.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themeColors.textSecondary
                        )
                    ) {
                        Text("İptal")
                    }
                    
                    // Confirm button
                    Button(
                        onClick = {
                            val operatorSignatures = operators.map { operator ->
                                OperatorSignature(
                                    operatorId = operator.id,
                                    operatorName = operator.name,
                                    signatureBitmap = signatures[operator.id]
                                )
                            }
                            onConfirm(operatorSignatures)
                        },
                        enabled = allSigned,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primary,
                            disabledContainerColor = themeColors.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (allSigned) "Onayla ve Gönder" else "Tüm imzalar gerekli")
                    }
                }
            }
        }
    }
}

/**
 * Empty state dialog when no operators are selected.
 */
@Composable
fun NoOperatorsSignatureDialog(
    onDismiss: () -> Unit,
    onContinueWithoutSignature: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Operatör Seçilmedi",
                color = themeColors.warning,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Henüz operatör seçilmedi. İmza olmadan devam etmek istiyor musunuz?",
                color = themeColors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onContinueWithoutSignature,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                )
            ) {
                Text("Devam Et")
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
