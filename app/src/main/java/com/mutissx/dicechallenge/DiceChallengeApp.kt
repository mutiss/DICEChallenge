package com.mutissx.dicechallenge

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.mutissx.dicechallenge.di.dataModule
import com.mutissx.dicechallenge.di.databaseModule
import com.mutissx.dicechallenge.di.domainModule
import com.mutissx.dicechallenge.di.networkModule
import com.mutissx.dicechallenge.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

private const val IMAGE_CACHE_NAME = "dice_challenge_image_cache"

class DiceChallengeApp : Application(), ImageLoaderFactory, KoinComponent {

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

    /**
     * Coil cache images
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve(IMAGE_CACHE_NAME))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
}
