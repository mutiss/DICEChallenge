package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.BuildConfig
import com.mutissx.dicechallenge.data.remote.UserAgentInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private const val USER_AGENT = "DICEChallenge/1.0 ( mutissx@gmail.com )"

val networkModule = module {

    /**
     * Cache for Http Requests to lightweight calls
     */
    class ForceCacheInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            if (
                request.method != "GET" ||
                !response.isSuccessful
            ) {
                return response
            }

            val maxAgeSeconds = when {
                //For artists -> 1 hour
                request.url.encodedPath.contains("/artist/") -> {
                    60 * 60
                }

                //For albums -> 24 hours
                request.url.encodedPath.contains("/release-group") -> {
                    24 * 60 * 60
                }

                else -> {
                    return response
                }
            }

            return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header(
                    "Cache-Control",
                    "public, max-age=$maxAgeSeconds"
                )
                .build()
        }
    }

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    single {
        OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(androidApplication().cacheDir, "http_cache"),
                    maxSize = 20L * 1024L * 1024L
                )
            )
            .addInterceptor(UserAgentInterceptor(USER_AGENT))
            .addNetworkInterceptor(ForceCacheInterceptor())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }


}
