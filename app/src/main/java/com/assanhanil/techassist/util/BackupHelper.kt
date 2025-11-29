package com.assanhanil.techassist.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Backup and Restore utility for application data.
 * Supports exporting and importing reports, recipes, and templates.
 */
class BackupHelper(private val context: Context) {

    companion object {
        private const val BACKUP_VERSION = 1
        private const val MANIFEST_FILE = "manifest.json"
        private const val REPORTS_DIR = "reports/"
        private const val RECIPES_DIR = "recipes/"
        private const val TEMPLATES_DIR = "templates/"
        private const val PHOTOS_DIR = "photos/"
    }

    /**
     * Create a backup file containing all application data.
     * 
     * @param outputFile The file to write the backup to
     * @param includePhotos Whether to include captured photos in the backup
     * @return BackupResult indicating success or failure
     */
    suspend fun createBackup(
        outputFile: File,
        includePhotos: Boolean = true
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zipOut ->
                // Create manifest
                val manifest = JSONObject().apply {
                    put("version", BACKUP_VERSION)
                    put("appVersion", getAppVersion())
                    put("createdAt", System.currentTimeMillis())
                    put("device", android.os.Build.MODEL)
                    put("includesPhotos", includePhotos)
                }
                
                // Write manifest
                writeJsonToZip(zipOut, MANIFEST_FILE, manifest)
                
                // Backup database tables
                val reportsCount = backupReports(zipOut)
                val recipesCount = backupRecipes(zipOut)
                val templatesCount = backupTemplates(zipOut)
                
                manifest.put("reportsCount", reportsCount)
                manifest.put("recipesCount", recipesCount)
                manifest.put("templatesCount", templatesCount)
                
                // Backup photos if requested
                var photosCount = 0
                if (includePhotos) {
                    photosCount = backupPhotos(zipOut)
                    manifest.put("photosCount", photosCount)
                }
                
                // Update manifest with final counts
                writeJsonToZip(zipOut, MANIFEST_FILE, manifest)
            }
            
            BackupResult.Success(
                filePath = outputFile.absolutePath,
                fileSize = outputFile.length()
            )
        } catch (e: Exception) {
            BackupResult.Error("Yedekleme başarısız: ${e.message}", e)
        }
    }

    /**
     * Restore data from a backup file.
     * 
     * @param backupFile The backup file to restore from
     * @param overwrite Whether to overwrite existing data
     * @return RestoreResult indicating success or failure
     */
    suspend fun restoreBackup(
        backupFile: File,
        overwrite: Boolean = false
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(backupFile))).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                var manifest: JSONObject? = null
                var reportsRestored = 0
                var recipesRestored = 0
                var templatesRestored = 0
                var photosRestored = 0
                
                while (entry != null) {
                    when {
                        entry.name == MANIFEST_FILE -> {
                            manifest = readJsonFromZip(zipIn)
                        }
                        entry.name.startsWith(REPORTS_DIR) -> {
                            if (restoreReport(zipIn, overwrite)) reportsRestored++
                        }
                        entry.name.startsWith(RECIPES_DIR) -> {
                            if (restoreRecipe(zipIn, overwrite)) recipesRestored++
                        }
                        entry.name.startsWith(TEMPLATES_DIR) -> {
                            if (restoreTemplate(zipIn, overwrite)) templatesRestored++
                        }
                        entry.name.startsWith(PHOTOS_DIR) -> {
                            if (restorePhoto(zipIn, entry.name)) photosRestored++
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
                
                RestoreResult.Success(
                    backupVersion = manifest?.optInt("version") ?: 0,
                    reportsRestored = reportsRestored,
                    recipesRestored = recipesRestored,
                    templatesRestored = templatesRestored,
                    photosRestored = photosRestored
                )
            }
        } catch (e: Exception) {
            RestoreResult.Error("Geri yükleme başarısız: ${e.message}", e)
        }
    }

    /**
     * Get information about a backup file without restoring it.
     */
    suspend fun getBackupInfo(backupFile: File): BackupInfo? = withContext(Dispatchers.IO) {
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(backupFile))).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                
                while (entry != null) {
                    if (entry.name == MANIFEST_FILE) {
                        val manifest = readJsonFromZip(zipIn)
                        return@withContext BackupInfo(
                            version = manifest.optInt("version", 0),
                            appVersion = manifest.optString("appVersion", ""),
                            createdAt = Date(manifest.optLong("createdAt", 0)),
                            device = manifest.optString("device", ""),
                            reportsCount = manifest.optInt("reportsCount", 0),
                            recipesCount = manifest.optInt("recipesCount", 0),
                            templatesCount = manifest.optInt("templatesCount", 0),
                            photosCount = manifest.optInt("photosCount", 0),
                            fileSize = backupFile.length()
                        )
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the default backup directory.
     */
    fun getBackupDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generate a default backup filename with timestamp.
     */
    fun generateBackupFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "techassist_backup_$timestamp.zip"
    }

    // Private helper functions
    
    private fun writeJsonToZip(zipOut: ZipOutputStream, entryName: String, json: JSONObject) {
        val entry = ZipEntry(entryName)
        zipOut.putNextEntry(entry)
        zipOut.write(json.toString(2).toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }

    private fun readJsonFromZip(zipIn: ZipInputStream): JSONObject {
        val content = zipIn.bufferedReader().readText()
        return JSONObject(content)
    }

    private fun backupReports(zipOut: ZipOutputStream): Int {
        // TODO: Implement actual database backup when integrating with ViewModel
        // This should query all reports from ReportDao and write them as JSON
        // For now, returns 0 as this is a framework for the backup system
        return 0
    }

    private fun backupRecipes(zipOut: ZipOutputStream): Int {
        // TODO: Implement actual database backup when integrating with ViewModel
        // This should query all recipes from RecipeDao and write them as JSON
        return 0
    }

    private fun backupTemplates(zipOut: ZipOutputStream): Int {
        // TODO: Implement actual database backup when integrating with ViewModel
        // This should query all templates from ExcelTemplateDao and write them as JSON
        return 0
    }

    private fun backupPhotos(zipOut: ZipOutputStream): Int {
        val photosDir = File(context.getExternalFilesDir(null), "photos")
        if (!photosDir.exists()) return 0
        
        var count = 0
        photosDir.listFiles()?.forEach { file ->
            if (file.isFile && (file.extension == "jpg" || file.extension == "jpeg" || file.extension == "png")) {
                val entry = ZipEntry("$PHOTOS_DIR${file.name}")
                zipOut.putNextEntry(entry)
                FileInputStream(file).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
                count++
            }
        }
        return count
    }

    private fun restoreReport(zipIn: ZipInputStream, overwrite: Boolean): Boolean {
        // TODO: Implement actual database restore when integrating with ViewModel
        // This should parse JSON and insert into ReportDao
        // Returns true as placeholder - actual implementation should return success status
        return true
    }

    private fun restoreRecipe(zipIn: ZipInputStream, overwrite: Boolean): Boolean {
        // TODO: Implement actual database restore when integrating with ViewModel
        // This should parse JSON and insert into RecipeDao
        return true
    }

    private fun restoreTemplate(zipIn: ZipInputStream, overwrite: Boolean): Boolean {
        // TODO: Implement actual database restore when integrating with ViewModel
        // This should parse JSON and insert into ExcelTemplateDao
        return true
    }

    private fun restorePhoto(zipIn: ZipInputStream, entryName: String): Boolean {
        try {
            val photosDir = File(context.getExternalFilesDir(null), "photos")
            if (!photosDir.exists()) photosDir.mkdirs()
            
            val fileName = entryName.removePrefix(PHOTOS_DIR)
            val outputFile = File(photosDir, fileName)
            
            FileOutputStream(outputFile).use { output ->
                zipIn.copyTo(output)
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}

/**
 * Result of a backup operation.
 */
sealed class BackupResult {
    data class Success(
        val filePath: String,
        val fileSize: Long
    ) : BackupResult()
    
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : BackupResult()
}

/**
 * Result of a restore operation.
 */
sealed class RestoreResult {
    data class Success(
        val backupVersion: Int,
        val reportsRestored: Int,
        val recipesRestored: Int,
        val templatesRestored: Int,
        val photosRestored: Int
    ) : RestoreResult()
    
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : RestoreResult()
}

/**
 * Information about a backup file.
 */
data class BackupInfo(
    val version: Int,
    val appVersion: String,
    val createdAt: Date,
    val device: String,
    val reportsCount: Int,
    val recipesCount: Int,
    val templatesCount: Int,
    val photosCount: Int,
    val fileSize: Long
)
