package com.konovus.traintogether.data.local.backup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.PersonalRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Manages backup and restore operations for workout data
 */
class BackupManager(private val context: Context): IBackupManager {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Data class representing the backup format
     */
    data class BackupData(
        val version: Int = 1,
        val timestamp: String = LocalDateTime.now().toString(),
        val userId: String,
        val username: String,
        val workoutLogs: List<WorkoutLogDB>,
        val personalRecords: List<PersonalRecord>
    )

    /**
     * Saves workout logs and personal records to a JSON file
     * @return Result with file path on success, error message on failure
     */
    override suspend fun exportBackup(
        userId: String,
        username: String,
        workoutLogs: List<WorkoutLogDB>,
        personalRecords: List<PersonalRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupData = BackupData(
                userId = userId,
                username = username,
                workoutLogs = workoutLogs,
                personalRecords = personalRecords
            )

            val json = gson.toJson(backupData)
            val fileName = generateBackupFileName()
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(json.toByteArray())
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves backup to external storage (Downloads folder) for user access
     * Requires WRITE_EXTERNAL_STORAGE permission
     */
    override suspend fun exportBackupToDownloads(
        userId: String,
        username: String,
        workoutLogs: List<WorkoutLogDB>,
        personalRecords: List<PersonalRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupData = BackupData(
                userId = userId,
                username = username,
                workoutLogs = workoutLogs,
                personalRecords = personalRecords
            )

            val json = gson.toJson(backupData)
            val fileName = generateBackupFileName()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw Exception("Failed to create file in Downloads")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }

                Result.success("Downloads/$fileName")
            } else {
                // Android 9 and below - Use Environment.getExternalStoragePublicDirectory
                @Suppress("DEPRECATION")
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(json.toByteArray())
                }

                Result.success(file.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Imports backup data from a JSON file
     * @param filePath Path to the backup file
     * @return Result with BackupData on success, error on failure
     */
    override suspend fun importBackup(filePath: String): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Backup file not found"))
            }

            val json = FileInputStream(file).use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }

            val backupData = gson.fromJson(json, BackupData::class.java)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all available backup files
     */
    override suspend fun listBackups(): Result<List<BackupFileInfo>> = withContext(Dispatchers.IO) {
        try {
            val files = context.filesDir.listFiles { file ->
                file.name.startsWith("workout_backup_") && file.name.endsWith(".json")
            } ?: emptyArray()

            val backupInfoList = files.map { file ->
                BackupFileInfo(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    lastModified = file.lastModified()
                )
            }.sortedByDescending { it.lastModified }

            Result.success(backupInfoList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file
     */
    override suspend fun deleteBackup(filePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            val deleted = file.delete()
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the total size of all backups
     */
    override suspend fun getBackupsSize(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val files = context.filesDir.listFiles { file ->
                file.name.startsWith("workout_backup_") && file.name.endsWith(".json")
            } ?: emptyArray()

            val totalSize = files.sumOf { it.length() }
            Result.success(totalSize)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cleans up old backups, keeping only the most recent ones
     * @param keepCount Number of recent backups to keep
     */
    override suspend fun cleanupOldBackups(keepCount: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val files = context.filesDir.listFiles { file ->
                file.name.startsWith("workout_backup_") && file.name.endsWith(".json")
            } ?: emptyArray()

            val sortedFiles = files.sortedByDescending { it.lastModified() }
            val filesToDelete = sortedFiles.drop(keepCount)

            var deletedCount = 0
            filesToDelete.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importBackupFromUri(uriString: String): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(uriString)
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: throw Exception("Failed to read file")

            val backupData = gson.fromJson(json, BackupData::class.java)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a unique backup file name with timestamp
     */
    private fun generateBackupFileName(): String {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "workout_backup_$timestamp.json"
    }

    data class BackupFileInfo(
        val fileName: String,
        val filePath: String,
        val fileSize: Long,
        val lastModified: Long
    )
}

fun BackupManager.BackupFileInfo.getFormattedSize(): String {
    return when {
        fileSize < 1024 -> "$fileSize B"
        fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
        else -> String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
    }
}

fun BackupManager.BackupFileInfo.getFormattedDate(): String {
    val date = java.util.Date(lastModified)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}