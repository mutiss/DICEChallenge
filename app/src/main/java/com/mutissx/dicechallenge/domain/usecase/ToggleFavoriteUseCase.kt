package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(artist: Artist, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            repository.remove(artist.mbid)
        } else {
            repository.add(artist)
        }
    }
}
