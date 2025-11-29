package com.assanhanil.techassist.presentation.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assanhanil.techassist.presentation.ui.components.GlassCard
import com.assanhanil.techassist.presentation.ui.components.NeonCard
import com.assanhanil.techassist.presentation.ui.theme.TechAssistColors

/**
 * Recipes Screen - Master maintenance recipes/templates.
 * 
 * Features:
 * - View predefined maintenance recipes
 * - Create new recipes
 * - Load recipes for quick form filling
 */
@Composable
fun RecipesScreen(
    modifier: Modifier = Modifier
) {
    var selectedRecipe by remember { mutableStateOf<MaintenanceRecipe?>(null) }
    var showNewRecipeDialog by remember { mutableStateOf(false) }
    
    // Sample recipes (in production, these would come from database)
    val recipes = remember {
        mutableStateListOf(
            MaintenanceRecipe(
                id = 1,
                name = "Günlük Press Kontrolü",
                description = "Pres makinesi günlük kontrol listesi",
                category = "Günlük",
                steps = listOf(
                    RecipeStep(1, "Hidrolik yağ seviyesi kontrolü", "Gösterge yeşil bölgede olmalı"),
                    RecipeStep(2, "Güvenlik sensörleri kontrolü", "Tüm sensörler aktif olmalı"),
                    RecipeStep(3, "Acil durdurma butonu testi", "Çalıştığından emin olun"),
                    RecipeStep(4, "Genel temizlik", "Talaş ve kir temizliği")
                )
            ),
            MaintenanceRecipe(
                id = 2,
                name = "Haftalık Kaynak Makinesi Bakımı",
                description = "Kaynak makinesi haftalık bakım listesi",
                category = "Haftalık",
                steps = listOf(
                    RecipeStep(1, "Elektrot durumu kontrolü", "Aşınmış elektrotları değiştirin"),
                    RecipeStep(2, "Soğutma suyu kontrolü", "Seviye ve sızıntı kontrolü"),
                    RecipeStep(3, "Kablo bağlantıları kontrolü", "Gevşek bağlantı olmamalı"),
                    RecipeStep(4, "Koruyucu cam temizliği", "Temiz ve çiziksiz olmalı"),
                    RecipeStep(5, "Topraklama kontrolü", "Topraklama direnci ölçümü")
                )
            ),
            MaintenanceRecipe(
                id = 3,
                name = "Aylık Konveyör Bakımı",
                description = "Konveyör sistemi aylık bakım prosedürü",
                category = "Aylık",
                steps = listOf(
                    RecipeStep(1, "Bant gerginlik kontrolü", "Standart değerlerde olmalı"),
                    RecipeStep(2, "Rulman yağlama", "Tüm rulmanlar yağlanmalı"),
                    RecipeStep(3, "Motor akım ölçümü", "Nominal değerlerde olmalı"),
                    RecipeStep(4, "Bant aşınma kontrolü", "Aşınma belirtileri not edilmeli"),
                    RecipeStep(5, "Hız sensörü kalibrasyonu", "Kalibre edilmeli"),
                    RecipeStep(6, "Acil durdurma testi", "Tüm noktalardan test")
                )
            ),
            MaintenanceRecipe(
                id = 4,
                name = "CNC Makine Günlük Kontrolü",
                description = "CNC tezgahı günlük kontrol listesi",
                category = "Günlük",
                steps = listOf(
                    RecipeStep(1, "Yağ seviyesi kontrolü", "Min-Max arasında olmalı"),
                    RecipeStep(2, "Soğutma sıvısı kontrolü", "Seviye ve konsantrasyon"),
                    RecipeStep(3, "Talaş temizliği", "Tüm bölgeler temizlenmeli"),
                    RecipeStep(4, "Eksen referans kontrolü", "Home pozisyonu doğrulanmalı"),
                    RecipeStep(5, "Takım durumu kontrolü", "Aşınma kontrolü")
                )
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TechAssistColors.Background)
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
                    text = "Bakım Tarifleri",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TechAssistColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Hazır bakım şablonları ve kontrol listeleri",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TechAssistColors.TextSecondary
                )
            }
            
            FloatingActionButton(
                onClick = { showNewRecipeDialog = true },
                containerColor = TechAssistColors.Primary,
                contentColor = TechAssistColors.Background
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Tarif")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Category filters
        val categories = listOf("Tümü", "Günlük", "Haftalık", "Aylık")
        var selectedCategory by remember { mutableStateOf("Tümü") }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechAssistColors.Primary,
                        selectedLabelColor = TechAssistColors.Background
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recipes List
        val filteredRecipes = if (selectedCategory == "Tümü") {
            recipes
        } else {
            recipes.filter { it.category == selectedCategory }
        }
        
        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = TechAssistColors.TextDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bu kategoride tarif bulunamadı",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechAssistColors.TextDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { selectedRecipe = recipe }
                    )
                }
            }
        }
    }
    
    // Recipe Detail Dialog
    selectedRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onDismiss = { selectedRecipe = null }
        )
    }
    
    // New Recipe Dialog
    if (showNewRecipeDialog) {
        NewRecipeDialog(
            onDismiss = { showNewRecipeDialog = false },
            onSave = { newRecipe ->
                recipes.add(newRecipe.copy(id = recipes.size + 1))
                showNewRecipeDialog = false
            }
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: MaintenanceRecipe,
    onClick: () -> Unit
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechAssistColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null,
                    tint = TechAssistColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TechAssistColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextSecondary,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(recipe.category, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = TechAssistColors.Secondary.copy(alpha = 0.1f),
                            labelColor = TechAssistColors.Secondary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${recipe.steps.size} adım",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechAssistColors.TextDisabled
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TechAssistColors.TextDisabled
            )
        }
    }
}

@Composable
private fun RecipeDetailDialog(
    recipe: MaintenanceRecipe,
    onDismiss: () -> Unit
) {
    var checkedSteps by remember { mutableStateOf(setOf<Int>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TechAssistColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TechAssistColors.TextSecondary
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipe.steps) { step ->
                    val isChecked = checkedSteps.contains(step.order)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isChecked) TechAssistColors.Secondary.copy(alpha = 0.1f)
                                else TechAssistColors.SurfaceVariant
                            )
                            .clickable {
                                checkedSteps = if (isChecked) {
                                    checkedSteps - step.order
                                } else {
                                    checkedSteps + step.order
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                checkedSteps = if (it) {
                                    checkedSteps + step.order
                                } else {
                                    checkedSteps - step.order
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TechAssistColors.Secondary,
                                uncheckedColor = TechAssistColors.TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "${step.order}. ${step.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isChecked) TechAssistColors.Secondary else TechAssistColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            if (step.description.isNotEmpty()) {
                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TechAssistColors.TextSecondary
                                )
                            }
                        }
                    }
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
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = { checkedSteps = emptySet() }) {
                Text("Sıfırla", color = TechAssistColors.TextSecondary)
            }
        },
        containerColor = TechAssistColors.Surface,
        modifier = Modifier.heightIn(max = 500.dp)
    )
}

@Composable
private fun NewRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (MaintenanceRecipe) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Günlük") }
    var steps by remember { mutableStateOf(listOf<RecipeStep>()) }
    var newStepTitle by remember { mutableStateOf("") }
    var newStepDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni Tarif Oluştur",
                color = TechAssistColors.Primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tarif Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechAssistColors.Primary,
                        unfocusedBorderColor = TechAssistColors.GlassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechAssistColors.Primary,
                        unfocusedBorderColor = TechAssistColors.GlassBorder
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Selection
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium,
                    color = TechAssistColors.TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Günlük", "Haftalık", "Aylık").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechAssistColors.Primary,
                                selectedLabelColor = TechAssistColors.Background
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Steps
                Text(
                    text = "Adımlar (${steps.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TechAssistColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ${step.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechAssistColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                steps = steps.filterIndexed { i, _ -> i != index }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kaldır",
                                tint = TechAssistColors.Error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newStepTitle,
                    onValueChange = { newStepTitle = it },
                    label = { Text("Yeni Adım") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newStepTitle.isNotBlank()) {
                                    steps = steps + RecipeStep(
                                        order = steps.size + 1,
                                        title = newStepTitle,
                                        description = newStepDescription
                                    )
                                    newStepTitle = ""
                                    newStepDescription = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ekle",
                                tint = TechAssistColors.Primary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechAssistColors.Primary,
                        unfocusedBorderColor = TechAssistColors.GlassBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && steps.isNotEmpty()) {
                        onSave(
                            MaintenanceRecipe(
                                id = 0,
                                name = name,
                                description = description,
                                category = category,
                                steps = steps
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && steps.isNotEmpty(),
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
 * Data class representing a maintenance recipe.
 */
data class MaintenanceRecipe(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val steps: List<RecipeStep>
)

/**
 * Data class representing a step in a recipe.
 */
data class RecipeStep(
    val order: Int,
    val title: String,
    val description: String = ""
)
