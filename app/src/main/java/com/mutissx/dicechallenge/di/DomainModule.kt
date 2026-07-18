package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.data.repository.ArtistRepositoryImpl
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        SearchArtistsUseCase(repository = get())
    }
}
