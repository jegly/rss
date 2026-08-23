package com.jegly.rss.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {
    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE link = :link)")
    fun isSaved(link: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE link = :link")
    suspend fun deleteByLink(link: String)
}
