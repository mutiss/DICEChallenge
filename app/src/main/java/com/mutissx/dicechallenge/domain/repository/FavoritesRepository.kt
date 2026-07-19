package com.mutissx.dicechallenge.domain.repository

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeAll(): Flow<List<Artist>>
    fun observeIsFavorite(mbid: String): Flow<Boolean>
    suspend fun add(artist: Artist): Result<Unit, DataError.Local>
    suspend fun remove(mbid: String): Result<Unit, DataError.Local>
}
