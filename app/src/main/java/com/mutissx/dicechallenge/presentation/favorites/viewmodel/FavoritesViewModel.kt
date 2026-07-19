package com.mutissx.dicechallenge.presentation.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.dicechallenge.presentation.favorites.screen.FavoritesUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavorites()
        .map<List<Artist>, FavoritesUiState> { FavoritesUiState.Content(it) }
        .catch { emit(FavoritesUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading
        )
}
