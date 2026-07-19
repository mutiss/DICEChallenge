package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.dicechallenge.domain.usecase.ObserveIsFavoriteUseCase
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import com.mutissx.dicechallenge.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SearchArtistsUseCase(repository = get()) }

    factory { GetArtistDetailUseCase(repository = get()) }

    factory { GetArtistReleaseGroupsUseCase(repository = get()) }

    factory { ObserveIsFavoriteUseCase(repository = get()) }

    factory { ObserveFavoritesUseCase(repository = get()) }

    factory { ToggleFavoriteUseCase(repository = get()) }
}
