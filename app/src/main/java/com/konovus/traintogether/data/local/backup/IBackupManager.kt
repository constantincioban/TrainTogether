package com.konovus.traintogether.data.local.backup

import com.konovus.traintogether.data.local.backup.BackupManager.BackupData
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.PersonalRecord

interface IBackupManager {

    suspend fun exportBackup(
        userId: String,
        username: String,
        workoutLogs: List<WorkoutLogDB>,
        personalRecords: List<PersonalRecord>
    ): Result<String>

    suspend fun exportBackupToDownloads(
        userId: String,
        username: String,
        workoutLogs: List<WorkoutLogDB>,
        personalRecords: List<PersonalRecord>
    ): Result<String>

    suspend fun importBackup(filePath: String): Result<BackupManager.BackupData>

    suspend fun listBackups(): Result<List<BackupManager.BackupFileInfo>>

    suspend fun deleteBackup(filePath: String): Result<Boolean>

    suspend fun getBackupsSize(): Result<Long>

    suspend fun cleanupOldBackups(keepCount: Int = 5): Result<Int>

    suspend fun importBackupFromUri(uriString: String): Result<BackupData>
}