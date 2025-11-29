package com.assanhanil.techassist.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Camera Screen - Smart photo capture with annotations.
 * 
 * Features:
 * - Camera permission handling
 * - Photo capture
 * - Photo gallery view
 * - Add notes/annotations to photos
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var capturedPhotos by remember { mutableStateOf<List<CapturedPhoto>>(emptyList()) }
    var selectedPhoto by remember { mutableStateOf<CapturedPhoto?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var photoForNote by remember { mutableStateOf<CapturedPhoto?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Camera launcher (using Intent for simplicity)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val newPhoto = CapturedPhoto(
                id = capturedPhotos.size + 1,
                bitmap = it,
                timestamp = Date(),
                note = ""
            )
            capturedPhotos = capturedPhotos + newPhoto
            Toast.makeText(context, "Fotoğraf kaydedildi", Toast.LENGTH_SHORT).show()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Akıllı Kamera",
            style = MaterialTheme.typography.headlineSmall,
            color = TechAssistColors.Primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Fotoğraf çekin ve not ekleyin",
            style = MaterialTheme.typography.bodyMedium,
            color = TechAssistColors.TextSecondary
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
                        .background(TechAssistColors.Primary.copy(alpha = 0.2f))
                        .border(2.dp, TechAssistColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Fotoğraf Çek",
                        tint = TechAssistColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Fotoğraf Çek",
                        style = MaterialTheme.typography.titleMedium,
                        color = TechAssistColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasCameraPermission) "Dokunarak çekim yapın" else "İzin gerekli",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechAssistColors.TextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Photos Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Çekilen Fotoğraflar",
                style = MaterialTheme.typography.titleMedium,
                color = TechAssistColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            if (capturedPhotos.isNotEmpty()) {
                Text(
                    text = "${capturedPhotos.size} fotoğraf",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextDisabled
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (capturedPhotos.isEmpty()) {
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
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = TechAssistColors.TextDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz fotoğraf çekilmedi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechAssistColors.TextDisabled
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yukarıdaki butona dokunarak fotoğraf çekin",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechAssistColors.TextDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(capturedPhotos) { photo ->
                    PhotoCard(
                        photo = photo,
                        onClick = { selectedPhoto = photo },
                        onAddNote = {
                            photoForNote = photo
                            showAddNoteDialog = true
                        },
                        onDelete = {
                            capturedPhotos = capturedPhotos.filter { it.id != photo.id }
                        }
                    )
                }
            }
        }
    }
    
    // Photo Detail Dialog
    selectedPhoto?.let { photo ->
        PhotoDetailDialog(
            photo = photo,
            onDismiss = { selectedPhoto = null }
        )
    }
    
    // Add Note Dialog
    if (showAddNoteDialog && photoForNote != null) {
        AddNoteDialog(
            currentNote = photoForNote!!.note,
            onDismiss = { 
                showAddNoteDialog = false
                photoForNote = null
            },
            onSave = { note ->
                capturedPhotos = capturedPhotos.map { 
                    if (it.id == photoForNote!!.id) it.copy(note = note) else it
                }
                showAddNoteDialog = false
                photoForNote = null
            }
        )
    }
}

@Composable
private fun PhotoCard(
    photo: CapturedPhoto,
    onClick: () -> Unit,
    onAddNote: () -> Unit,
    onDelete: () -> Unit
) {
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
                bitmap = photo.bitmap.asImageBitmap(),
                contentDescription = "Fotoğraf",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Fotoğraf #${photo.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TechAssistColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(photo.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextSecondary
                )
                
                if (photo.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = TechAssistColors.Secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = photo.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = TechAssistColors.Secondary,
                            maxLines = 1
                        )
                    }
                }
            }
            
            Column {
                IconButton(onClick = onAddNote) {
                    Icon(
                        imageVector = if (photo.note.isEmpty()) Icons.Default.NoteAdd else Icons.Default.Edit,
                        contentDescription = "Not Ekle",
                        tint = TechAssistColors.Primary
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = TechAssistColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoDetailDialog(
    photo: CapturedPhoto,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Fotoğraf #${photo.id}",
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Image(
                    bitmap = photo.bitmap.asImageBitmap(),
                    contentDescription = "Fotoğraf",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Çekim Tarihi: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(photo.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextSecondary
                )
                
                if (photo.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Not:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TechAssistColors.TextSecondary
                    )
                    Text(
                        text = photo.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechAssistColors.TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechAssistColors.Primary
                )
            ) {
                Text("Kapat")
            }
        },
        containerColor = TechAssistColors.Surface
    )
}

@Composable
private fun AddNoteDialog(
    currentNote: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember { mutableStateOf(currentNote) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Not Ekle",
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Not") },
                placeholder = { Text("Fotoğraf hakkında not yazın...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechAssistColors.Primary,
                    unfocusedBorderColor = TechAssistColors.GlassBorder
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(note) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechAssistColors.Primary
                )
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TechAssistColors.TextSecondary)
            }
        },
        containerColor = TechAssistColors.Surface
    )
}

/**
 * Data class representing a captured photo with metadata.
 */
data class CapturedPhoto(
    val id: Int,
    val bitmap: Bitmap,
    val timestamp: Date,
    val note: String
)
