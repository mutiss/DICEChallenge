package com.mutissx.dicechallenge.fake

import androidx.paging.PagingData
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeArtistRepository : ArtistRepository {

    val queriesReceived = mutableListOf<String>()

    var artistResult: Result<Artist, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    var releaseGroupsResult: Result<List<ReleaseGroup>, DataError.Network> = Result.Success(emptyList())

    override fun searchArtists(query: String): Flow<PagingData<Artist>> {
        queriesReceived.add(query)
        return flowOf(PagingData.empty())
    }

    override suspend fun getArtist(mbid: String): Result<Artist, DataError.Network> = artistResult

    override suspend fun getReleaseGroups(mbid: String): Result<List<ReleaseGroup>, DataError.Network> =
        releaseGroupsResult
}
