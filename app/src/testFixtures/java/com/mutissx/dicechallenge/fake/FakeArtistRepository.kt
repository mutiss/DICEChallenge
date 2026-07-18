package com.mutissx.dicechallenge.fake

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

class FakeArtistRepository : ArtistRepository {

    val queriesReceived = mutableListOf<String>()

    var pagingSourceFactory: () -> FakeArtistPagingSource =
        { FakeArtistPagingSource(kotlin.Result.success(emptyList())) }

    var artistResult: Result<Artist, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    var releaseGroupsResult: Result<List<ReleaseGroup>, DataError.Network> = Result.Success(emptyList())

    override fun searchArtists(query: String): Flow<PagingData<Artist>> {
        queriesReceived.add(query)
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { pagingSourceFactory() }
        ).flow
    }

    override suspend fun getArtist(mbid: String): Result<Artist, DataError.Network> = artistResult

    override suspend fun getReleaseGroups(mbid: String): Result<List<ReleaseGroup>, DataError.Network> =
        releaseGroupsResult
}
