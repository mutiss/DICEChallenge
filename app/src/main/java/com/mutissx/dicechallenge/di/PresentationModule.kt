package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import com.mutissx.dicechallenge.presentation.search.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationlModule = module {

    viewModel {
        SearchViewModel(searchArtistsUseCase = get())
    }

    viewModel {
        ArtistDetailViewModel(
            savedStateHandle = it.get(),
            getArtistDetailUseCase = get(),
            getReleaseGroupsUseCase = get()
        )
    }
}
