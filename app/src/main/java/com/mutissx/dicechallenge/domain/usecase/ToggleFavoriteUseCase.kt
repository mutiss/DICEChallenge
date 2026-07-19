package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(artist: Artist, isCurrentlyFavorite: Boolean): Result<Unit, DataError.Local> =
        if (isCurrentlyFavorite) {
            repository.remove(artist.mbid)
        } else {
            repository.add(artist)
        }
}
