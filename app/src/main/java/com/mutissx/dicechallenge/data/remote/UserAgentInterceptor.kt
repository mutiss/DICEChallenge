package com.mutissx.dicechallenge.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val userAgent: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
