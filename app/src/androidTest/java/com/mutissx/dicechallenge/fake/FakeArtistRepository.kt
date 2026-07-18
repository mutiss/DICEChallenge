package com.mutissx.dicechallenge.fake

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import com.mutissx.dicechallenge.core.domain.Result as DomainResult

class FakeArtistRepository : ArtistRepository {

    var pagingSourceFactory: () -> FakeArtistPagingSource =
        { FakeArtistPagingSource(Result.success(emptyList())) }

    var artistResult: DomainResult<Artist, DataError.Network> =
        DomainResult.Error(DataError.Network.UNKNOWN)
    var releaseGroupsResult: DomainResult<List<ReleaseGroup>, DataError.Network> =
        DomainResult.Success(emptyList())

    override fun searchArtists(query: String): Flow<PagingData<Artist>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { pagingSourceFactory() }
        ).flow

    override suspend fun getArtist(mbid: String): DomainResult<Artist, DataError.Network> = artistResult

    override suspend fun getReleaseGroups(mbid: String): DomainResult<List<ReleaseGroup>, DataError.Network> =
        releaseGroupsResult
}
