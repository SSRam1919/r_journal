package com.baverika.r_journal.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.baverika.r_journal.data.local.converters.Converters
import com.baverika.r_journal.data.local.dao.JournalDao
import com.baverika.r_journal.data.local.dao.QuickNoteDao
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.data.local.entity.QuickNote

@Database(
    entities = [JournalEntry::class, QuickNote::class, com.baverika.r_journal.data.local.entity.Event::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun quickNoteDao(): QuickNoteDao
    abstract fun eventDao(): com.baverika.r_journal.data.local.dao.EventDao

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `events` (
                        `id` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `day` INTEGER NOT NULL, 
                        `month` INTEGER NOT NULL, 
                        `year` INTEGER, 
                        `type` TEXT NOT NULL, 
                        `isRecurring` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}