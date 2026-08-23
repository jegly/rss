package com.jegly.rss.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FeedEntity::class, SavedArticleEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun savedArticleDao(): SavedArticleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE feeds ADD COLUMN categoryOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE feeds ADD COLUMN feedType TEXT NOT NULL DEFAULT 'rss'")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE feeds ADD COLUMN accentColor INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_articles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        link TEXT NOT NULL,
                        title TEXT NOT NULL,
                        pubDate TEXT NOT NULL,
                        description TEXT NOT NULL,
                        feedTitle TEXT NOT NULL,
                        savedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_saved_articles_link ON saved_articles(link)")
            }
        }
    }
}
