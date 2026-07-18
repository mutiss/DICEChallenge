package com.mutissx.dicechallenge.domain.repository

import com.mutissx.dicechallenge.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeAll(): Flow<List<Artist>>
    fun observeIsFavorite(mbid: String): Flow<Boolean>
    suspend fun add(artist: Artist)
    suspend fun remove(mbid: String)
}
