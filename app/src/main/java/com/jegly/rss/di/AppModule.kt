package com.jegly.rss.di

import android.content.Context
import androidx.room.Room
import com.jegly.rss.data.local.AppDatabase
import com.jegly.rss.data.local.FeedDao
import com.jegly.rss.data.remote.RssApiService
import com.jegly.rss.data.remote.RssParser
import com.jegly.rss.data.repository.FeedRepositoryImpl
import com.jegly.rss.domain.repository.FeedRepository
import com.jegly.rss.security.EncryptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                // HTTPS Enforcement: Redirect HTTP to HTTPS where possible
                val url = request.url
                if (url.scheme == "http") {
                    val newUrl = url.newBuilder().scheme("https").build()
                    val newRequest = request.newBuilder().url(newUrl).build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(request)
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, encryptionManager: EncryptionManager): AppDatabase {
        var dbKey = encryptionManager.getString("db_key")
        if (dbKey == null) {
            // High Entropy Security: Replace UUID with a cryptographically secure 256-bit key
            val secureRandom = SecureRandom()
            val keyBytes = ByteArray(32) // 256 bits
            secureRandom.nextBytes(keyBytes)
            dbKey = keyBytes.joinToString("") { "%02x".format(it) }
            encryptionManager.saveString("db_key", dbKey)
        }
        val supportFactory = SupportFactory(dbKey.toByteArray())
        return Room.databaseBuilder(context, AppDatabase::class.java, "secure_rss.db")
            .openHelperFactory(supportFactory)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideFeedDao(db: AppDatabase): FeedDao = db.feedDao()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): RssApiService = Retrofit.Builder()
        .baseUrl("https://dummy.com/")
        .client(okHttpClient)
        .build()
        .create(RssApiService::class.java)

    @Provides
    @Singleton
    fun provideRepository(dao: FeedDao, api: RssApiService, parser: RssParser): FeedRepository = 
        FeedRepositoryImpl(dao, api, parser)
}
