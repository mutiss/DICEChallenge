package com.mutissx.dicechallenge.presentation.favorites.screen

import com.mutissx.dicechallenge.domain.model.Artist

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Error : FavoritesUiState
    data class Content(val favorites: List<Artist>) : FavoritesUiState
}
