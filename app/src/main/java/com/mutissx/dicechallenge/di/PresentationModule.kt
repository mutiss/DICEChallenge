package com.mutissx.dicechallenge.di

import com.mutissx.dicechallenge.presentation.search.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationlModule = module {

    viewModel {
        SearchViewModel(searchArtists = get())
    }
}
