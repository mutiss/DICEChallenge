package com.mutissx.dicechallenge.fake

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoritesRepository : FavoritesRepository {

    private val favorites = MutableStateFlow<List<Artist>>(emptyList())

    val addedArtists = mutableListOf<Artist>()
    val removedMbids = mutableListOf<String>()

    override fun observeAll(): Flow<List<Artist>> = favorites.asStateFlow()

    override fun observeIsFavorite(mbid: String): Flow<Boolean> =
        favorites.map { list -> list.any { it.mbid == mbid } }

    override suspend fun add(artist: Artist) {
        addedArtists.add(artist)
        favorites.value = favorites.value + artist
    }

    override suspend fun remove(mbid: String) {
        removedMbids.add(mbid)
        favorites.value = favorites.value.filterNot { it.mbid == mbid }
    }
}
