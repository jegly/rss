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
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.security.SecureRandom
import javax.inject.Singleton
import java.nio.charset.StandardCharsets

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context,
        encryptionManager: EncryptionManager
    ): AppDatabase {
        System.loadLibrary("sqlcipher")
        val dbKeyString: String = encryptionManager.getString("db_key") ?: run {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val key = bytes.joinToString("") { "%02x".format(it) }
            encryptionManager.saveString("db_key", key)
            key
        }
        val passphrase = dbKeyString.toByteArray(StandardCharsets.UTF_8)
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "secure_rss.db")
            .openHelperFactory(factory)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideFeedDao(db: AppDatabase): FeedDao {
        return db.feedDao()
    }

    @Provides
    @Singleton
    fun provideRssApiService(okHttpClient: OkHttpClient): RssApiService {
        return Retrofit.Builder()
            .baseUrl("https://jegly.xyz/")
            .client(okHttpClient)
            .build()
            .create(RssApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        dao: FeedDao,
        api: RssApiService,
        parser: RssParser
    ): FeedRepository {
        return FeedRepositoryImpl(dao, api, parser)
    }
}
