package com.mutissx.dicechallenge

import android.app.Application
import com.mutissx.dicechallenge.di.dataModule
import com.mutissx.dicechallenge.di.databaseModule
import com.mutissx.dicechallenge.di.domainModule
import com.mutissx.dicechallenge.di.networkModule
import com.mutissx.dicechallenge.di.presentationlModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DiceChallengeApp : Application() {
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
                presentationlModule
            )
        }
    }
}
