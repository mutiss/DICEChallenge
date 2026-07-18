package com.mutissx.dicechallenge.presentation.favorites.screen

import com.mutissx.dicechallenge.domain.model.Artist


data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<Artist> = emptyList()
)
