package dev.mjamalidev.tandemcommunity.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mjamalidev.tandemcommunity.BuildConfig
import dev.mjamalidev.tandemcommunity.data.local.DataStoreLikedMembersStore
import dev.mjamalidev.tandemcommunity.data.local.LikedMembersStore
import dev.mjamalidev.tandemcommunity.data.remote.CommunityApi
import dev.mjamalidev.tandemcommunity.data.repository.CommunityRepositoryImpl
import dev.mjamalidev.tandemcommunity.domain.repository.CommunityRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

private val Context.communityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "community_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    @Singleton
    abstract fun bindCommunityRepository(implementation: CommunityRepositoryImpl): CommunityRepository

    @Binds
    @Singleton
    abstract fun bindLikedMembersStore(implementation: DataStoreLikedMembersStore): LikedMembersStore
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.communityDataStore

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideCommunityApi(client: OkHttpClient, json: Json): CommunityApi =
        Retrofit.Builder()
            .baseUrl("https://tandem2019.web.app/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CommunityApi::class.java)
}
