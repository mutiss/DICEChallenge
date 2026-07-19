package com.mutissx.dicechallenge.fake

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeFavoritesRepository : FavoritesRepository {

    private val favorites = MutableStateFlow<List<Artist>>(emptyList())

    val addedArtists = mutableListOf<Artist>()
    val removedMbids = mutableListOf<String>()

    var writeResult: Result<Unit, DataError.Local> = Result.Success(Unit)
    var readError: Throwable? = null

    override fun observeAll(): Flow<List<Artist>> {
        val error = readError
        return if (error != null) flow { throw error } else favorites.asStateFlow()
    }

    override fun observeIsFavorite(mbid: String): Flow<Boolean> =
        favorites.map { list -> list.any { it.mbid == mbid } }

    override suspend fun add(artist: Artist): Result<Unit, DataError.Local> {
        if (writeResult is Result.Success) {
            addedArtists.add(artist)
            favorites.value = favorites.value + artist
        }
        return writeResult
    }

    override suspend fun remove(mbid: String): Result<Unit, DataError.Local> {
        if (writeResult is Result.Success) {
            removedMbids.add(mbid)
            favorites.value = favorites.value.filterNot { it.mbid == mbid }
        }
        return writeResult
    }
}
