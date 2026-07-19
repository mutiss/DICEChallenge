package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.data.repository.ArtistRepositoryImpl
import com.mutissx.dicechallenge.data.repository.FavoritesRepositoryImpl
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }

    single<ArtistRepository> { ArtistRepositoryImpl(api = get(), dispatcher = get()) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get(), dispatcher = get()) }

    single<MusicBrainzApi> { get<Retrofit>().create(MusicBrainzApi::class.java) }
}
