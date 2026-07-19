package com.mutissx.dicechallenge.domain.repository

import androidx.paging.PagingData
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun searchArtists(query: String): Flow<PagingData<Artist>>
    suspend fun getArtist(mbid: String): Result<Artist, DataError.Network>
    suspend fun getReleaseGroups(mbid: String): Result<List<ReleaseGroup>, DataError.Network>
}
