package com.baverika.r_journal.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dbName = "journal_database"
            val dbFolder = applicationContext.getDatabasePath(dbName).parentFile
            
            // Source paths
            val dbFile = File(dbFolder, dbName)
            val walFile = File(dbFolder, "$dbName-wal")
            val shmFile = File(dbFolder, "$dbName-shm")

            if (!dbFile.exists()) {
                Log.e("BackupWorker", "Database file not found: ${dbFile.absolutePath}")
                return Result.failure()
            }

            // Target: Downloads/RJournal_Backups (Visible to user)
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadsDir, "RJournal_Backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create timestamped folder for this backup session
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val sessionBackupDir = File(backupDir, "backup_$timestamp")
            if (!sessionBackupDir.exists()) {
                sessionBackupDir.mkdirs()
            }

            // Copy DB file
            dbFile.copyTo(File(sessionBackupDir, "$dbName.db"), overwrite = true)
            
            // Copy WAL/SHM if they exist (Critical for data integrity in WAL mode)
            if (walFile.exists()) {
                walFile.copyTo(File(sessionBackupDir, "$dbName.db-wal"), overwrite = true)
            }
            if (shmFile.exists()) {
                shmFile.copyTo(File(sessionBackupDir, "$dbName.db-shm"), overwrite = true)
            }

            Log.d("BackupWorker", "Backup created at: ${sessionBackupDir.absolutePath}")

            // Retention Policy: Keep only 5 most recent backup FOLDERS
            cleanOldBackups(backupDir)

            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed", e)
            Result.failure()
        }
    }

    private fun cleanOldBackups(backupRoot: File) {
        // List subdirectories that start with "backup_"
        val backupFolders = backupRoot.listFiles { file -> 
            file.isDirectory && file.name.startsWith("backup_") 
        } ?: return

        // Sort by modification time (oldest first)
        val sortedFolders = backupFolders.sortedBy { it.lastModified() }

        // Keep last 5
        if (sortedFolders.size > 5) {
            val foldersToDelete = sortedFolders.take(sortedFolders.size - 5)
            foldersToDelete.forEach { folder ->
                folder.deleteRecursively()
                Log.d("BackupWorker", "Deleted old backup number: ${folder.name}")
            }
        }
    }
}
