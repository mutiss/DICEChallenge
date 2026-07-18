package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class IsFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(mbid: String): Flow<Boolean> =
        repository.observeIsFavorite(mbid)
}
