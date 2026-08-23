package com.jegly.rss.di

import android.content.Context
import androidx.room.Room
import com.jegly.rss.data.local.AppDatabase
import com.jegly.rss.data.local.FeedDao
import com.jegly.rss.data.local.SavedArticleDao
import com.jegly.rss.data.remote.RssApiService
import com.jegly.rss.data.remote.RssParser
import com.jegly.rss.data.repository.FeedRepositoryImpl
import com.jegly.rss.domain.repository.FeedRepository
import com.jegly.rss.network.GuardedDns
import com.jegly.rss.network.ResponseSizeInterceptor
import com.jegly.rss.network.SwitchableDohDns
import com.jegly.rss.security.EncryptionManager
import com.jegly.rss.security.PassphraseGate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Bare-bones OkHttp used ONLY to bootstrap DoH. Must not itself use DoH (would recurse) and
     * shouldn't carry the response-size interceptor (DoH responses can spike).
     */
    @Provides
    @Singleton
    @Named("bootstrap")
    fun provideBootstrapOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        .callTimeout(15, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** Main OkHttp: HTTPS-only, MODERN_TLS, bounded body, explicit timeouts, user-selectable DoH. */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("bootstrap") bootstrapClient: OkHttpClient,
        encryptionManager: EncryptionManager
    ): OkHttpClient {
        val dns = GuardedDns(
            SwitchableDohDns(
                bootstrapClient = bootstrapClient,
                providerKeyLookup = { encryptionManager.getString("doh_provider") }
            )
        )
        return OkHttpClient.Builder()
            // Defense-in-depth: upgrade plain HTTP requests to HTTPS before they leave the process.
            // network-security-config already blocks cleartext at the OS layer.
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url
                if (!url.isHttps) {
                    val upgraded = url.newBuilder().scheme("https").build()
                    chain.proceed(request.newBuilder().url(upgraded).build())
                } else {
                    chain.proceed(request)
                }
            }
            // Cap response bodies to prevent OOM via giant feeds.
            .addNetworkInterceptor(ResponseSizeInterceptor())
            // Only modern TLS (1.2 / 1.3); no cleartext.
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            // Explicit timeouts — callTimeout defaults to 0 (infinite). Hostile servers can hang us forever.
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .dns(dns)
            .build()
    }

    /**
     * Blocks on PassphraseGate.await() — MainActivity opens the gate after auth (plain or biometric).
     * The block is a no-op once the gate is open, so subsequent Hilt graph fan-out is non-blocking.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context,
        gate: PassphraseGate
    ): AppDatabase {
        System.loadLibrary("sqlcipher")
        val passphrase = runBlocking { gate.await() }
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "secure_rss.db")
            .openHelperFactory(factory)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
    }

    @Provides
    @Singleton
    fun provideFeedDao(db: AppDatabase): FeedDao = db.feedDao()

    @Provides
    @Singleton
    fun provideSavedArticleDao(db: AppDatabase): SavedArticleDao = db.savedArticleDao()

    @Provides
    @Singleton
    fun provideRssApiService(okHttpClient: OkHttpClient): RssApiService =
        Retrofit.Builder()
            .baseUrl("https://jegly.xyz/")
            .client(okHttpClient)
            .build()
            .create(RssApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedRepository(
        dao: FeedDao,
        savedArticleDao: SavedArticleDao,
        api: RssApiService,
        parser: RssParser
    ): FeedRepository = FeedRepositoryImpl(dao, savedArticleDao, api, parser)
}
