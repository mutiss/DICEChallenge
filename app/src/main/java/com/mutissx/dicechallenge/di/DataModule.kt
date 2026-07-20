package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.data.repository.ArtistRepositoryImpl
import com.mutissx.dicechallenge.data.repository.FavoritesRepositoryImpl
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    single<ArtistRepository> { ArtistRepositoryImpl(api = get()) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get()) }

    single<MusicBrainzApi> { get<Retrofit>().create(MusicBrainzApi::class.java) }
}
