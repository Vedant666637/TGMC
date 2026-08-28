package com.tgm.tgmc.core.di

import com.tgm.tgmc.BuildConfig
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.TgmcApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response as OkHttpResponse
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(dataStore: TgmcDataStore): Interceptor = Interceptor { chain ->
        val token = runBlocking { dataStore.accessToken.firstOrNull() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideAuthenticator(
        dataStore: TgmcDataStore,
        apiService: Lazy<TgmcApiService>
    ): Authenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: OkHttpResponse): Request? {
            // Avoid infinite loops
            if (response.request.header("Authorization") == null) {
                return null
            }

            val refreshToken = runBlocking { dataStore.refreshToken.firstOrNull() }
            if (refreshToken.isNullOrEmpty()) {
                return null
            }

            return runBlocking {
                try {
                    val refreshResponse = apiService.get().refreshToken(
                        com.tgm.tgmc.core.data.remote.RefreshRequest(refreshToken)
                    )
                    if (refreshResponse.isSuccessful) {
                        val newAuth = refreshResponse.body()
                        if (newAuth != null) {
                            dataStore.saveAuthTokens(
                                accessToken = newAuth.accessToken,
                                refreshToken = newAuth.refreshToken,
                                role = newAuth.role,
                                userId = newAuth.userId,
                                email = newAuth.email
                            )
                            response.request.newBuilder()
                                .header("Authorization", "Bearer ${newAuth.accessToken}")
                                .build()
                        } else null
                    } else {
                        // Refresh token also expired or invalid
                        dataStore.clearAll()
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor, authenticator: Authenticator): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .addInterceptor(logging)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTgmcApiService(retrofit: Retrofit): TgmcApiService =
        retrofit.create(TgmcApiService::class.java)
}
