package com.mutissx.dicechallenge.data.repository

import com.mutissx.dicechallenge.core.data.safeDbCall
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.data.local.FavoriteArtistDao
import com.mutissx.dicechallenge.data.mapper.toDomain
import com.mutissx.dicechallenge.data.mapper.toEntity
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteArtistDao,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoritesRepository {

    override fun observeAll(): Flow<List<Artist>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatcher)

    override fun observeIsFavorite(mbid: String): Flow<Boolean> =
        dao.observeIsFavorite(mbid).flowOn(dispatcher)

    override suspend fun add(artist: Artist): Result<Unit, DataError.Local> =
        safeDbCall(dispatcher) { dao.insert(artist.toEntity(addedAt = clock())) }

    override suspend fun remove(mbid: String): Result<Unit, DataError.Local> =
        safeDbCall(dispatcher) { dao.delete(mbid) }
}
