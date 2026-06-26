package com.eduplatform

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.eduplatform.data.api.PersistentCookieJar
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class EduPlatformApp : Application() {

    // Hilt інжектує той самий OkHttpClient (з PersistentCookieJar + AuthInterceptor),
    // який використовує Retrofit — щоб Coil надсилав cookies при завантаженні зображень
    @Inject lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        Coil.setImageLoader {
            ImageLoader.Builder(this)
                .okHttpClient(okHttpClient)
                .crossfade(true)
                .build()
        }
    }
}