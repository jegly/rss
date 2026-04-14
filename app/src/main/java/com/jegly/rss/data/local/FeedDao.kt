package com.jegly.rss.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    fun getAllFeeds(): Flow<List<FeedEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: FeedEntity)
    
    @Update
    suspend fun updateFeed(feed: FeedEntity)
    
    @Delete
    suspend fun deleteFeed(feed: FeedEntity)
}
