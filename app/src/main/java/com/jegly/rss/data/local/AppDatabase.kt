package com.jegly.rss.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Re-indexing trigger comment
@Database(entities = [FeedEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE feeds ADD COLUMN categoryOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
