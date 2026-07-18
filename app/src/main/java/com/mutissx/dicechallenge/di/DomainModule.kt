package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        SearchArtistsUseCase(repository = get())
    }
    factory {
        GetArtistDetailUseCase(repository = get())
    }
    factory {
        GetArtistReleaseGroupsUseCase(repository = get())
    }
}
