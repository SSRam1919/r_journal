// app/src/main/java/com/baverika/r_journal/data/local/database/Migrations.kt

package com.baverika.r_journal.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE journal_entries ADD COLUMN mood TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE quick_notes (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

// ✅ NEW: Migration from v3 → v4 (even if no schema change)
// In Migrations.kt or wherever you define migrations

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1️⃣ Create a new table with the correct schema
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS quick_notes (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())

        // 2️⃣ Copy existing data from old table, ignoring null IDs
        database.execSQL("""
            INSERT INTO quick_notes (id, title, content, timestamp)
            SELECT id, title, content, timestamp FROM quick_notes
            WHERE id IS NOT NULL
        """.trimIndent())

        // 3️⃣ Drop old table
        //database.execSQL("DROP TABLE IF EXISTS quick_notes")

        // 4️⃣ Rename new table
        //database.execSQL("ALTER TABLE quick_notes_new RENAME TO quick_notes")
    }
}


// Then use it in JournalDatabase.companion.getDatabase:
// .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)