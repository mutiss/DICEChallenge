package com.mutissx.dicechallenge

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.mutissx.dicechallenge.di.dataModule
import com.mutissx.dicechallenge.di.databaseModule
import com.mutissx.dicechallenge.di.domainModule
import com.mutissx.dicechallenge.di.networkModule
import com.mutissx.dicechallenge.di.presentationModule
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DiceChallengeApp : Application(), ImageLoaderFactory, KoinComponent {

    private val okHttpClient: OkHttpClient by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DiceChallengeApp)
            modules(
                dataModule,
                databaseModule,
                domainModule,
                networkModule,
                presentationModule
            )
        }
    }

    // Reuses the app's OkHttpClient (and its connection pool) for cover art requests,
    // instead of Coil creating its own separate client under the hood.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .build()
}
