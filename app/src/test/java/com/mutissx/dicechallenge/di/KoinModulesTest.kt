package com.mutissx.dicechallenge.di

import androidx.lifecycle.SavedStateHandle
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Statically verifies that every constructor dependency declared across the app's Koin
 * modules resolves to a binding somewhere in the combined graph, without instantiating
 * anything (so it needs no Android Context). Catches DI wiring mistakes — like a class
 * moved between modules, or a forgotten binding — at test time instead of an app-launch
 * crash.
 *
 * Whitelisted extra types, both false positives from verify()'s "check every public
 * constructor" reflection rather than real gaps in our bindings:
 * - `SavedStateHandle` is supplied at runtime via Koin's parametersOf mechanism
 *   (`it.get()` in ArtistDetailViewModel's `viewModel { }` block), not a module binding.
 * - `HttpLoggingInterceptor.Logger` is an optional parameter on a constructor overload of
 *   OkHttp's HttpLoggingInterceptor that we never call (we use the no-arg constructor).
 */
class KoinModulesTest {

    @Test
    fun `given all app Koin modules combined, when verified, then every dependency resolves`() {
        module {
            includes(dataModule, databaseModule, domainModule, networkModule, presentationModule)
        }.verify(extraTypes = listOf(SavedStateHandle::class, HttpLoggingInterceptor.Logger::class))
    }
}
