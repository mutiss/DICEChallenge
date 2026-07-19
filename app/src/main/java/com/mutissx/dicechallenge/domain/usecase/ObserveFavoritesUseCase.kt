package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Artist>> = repository.observeAll()
}
