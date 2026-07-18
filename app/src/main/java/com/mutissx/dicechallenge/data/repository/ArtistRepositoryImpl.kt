package com.mutissx.dicechallenge.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.dicechallenge.data.mapper.toDomain
import com.mutissx.dicechallenge.data.paging.ArtistSearchPagingSource
import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.core.data.safeApiCall
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import com.mutissx.dicechallenge.core.domain.Result

class ArtistRepositoryImpl(
    private val api: MusicBrainzApi
) : ArtistRepository {

    override fun searchArtists(query: String): Flow<PagingData<Artist>> =
        Pager(
            config = PagingConfig(
                pageSize = ArtistSearchPagingSource.PAGE_SIZE,
                initialLoadSize = ArtistSearchPagingSource.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ArtistSearchPagingSource(api, query) }
        ).flow

    override suspend fun getArtist(mbid: String): Result<Artist, DataError.Network> =
        safeApiCall { api.getArtist(mbid).toDomain() }

    override suspend fun getReleaseGroups(mbid: String): Result<List<ReleaseGroup>, DataError.Network> =
        safeApiCall {
            api.getReleaseGroups(artistMbid = mbid)
                .releaseGroups
                .map { it.toDomain() }
                .sortedByDescending { it.firstReleaseYear.orEmpty() }
        }
}
