package com.eduplatform.di

import com.eduplatform.data.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        sessionExpiredInterceptor: SessionExpiredInterceptor,
        cookieJar: PersistentCookieJar
    ): OkHttpClient =
        buildOkHttpClient(authInterceptor, sessionExpiredInterceptor, cookieJar)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        buildRetrofit(okHttpClient)

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): EduApiService =
        buildApiService(retrofit)
}
