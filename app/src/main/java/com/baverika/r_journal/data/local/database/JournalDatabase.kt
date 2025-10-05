// app/src/main/java/com/baverika/r_journal/data/local/database/JournalDatabase.kt

package com.baverika.r_journal.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.baverika.r_journal.data.local.converters.Converters // Make sure this import is present
import com.baverika.r_journal.data.local.dao.JournalDao
import com.baverika.r_journal.data.local.dao.QuickNoteDao // Make sure this import is present
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.data.local.entity.QuickNote // Make sure this import is present

// Define your migrations here (e.g., MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
// ... (include the migration that creates quick_notes if necessary based on your schema history)

@Database(
    entities = [JournalEntry::class, QuickNote::class], // <--- Ensure QuickNote::class is included
    version = 4, // <--- Ensure version is 4
    exportSchema = true // Recommended
)
@TypeConverters(Converters::class) // <--- Ensure TypeConverters are applied if needed
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun quickNoteDao(): QuickNoteDao // <--- Ensure this method exists

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_db" // Ensure database name is consistent
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    // Apply your migrations here
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Include all necessary migrations
                    // .fallbackToDestructiveMigration() // Remove this in production, use proper migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}