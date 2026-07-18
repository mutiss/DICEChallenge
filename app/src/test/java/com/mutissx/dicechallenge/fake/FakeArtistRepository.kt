package com.mutissx.dicechallenge.fake

import androidx.paging.PagingData
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeArtistRepository : ArtistRepository {

    val queriesReceived = mutableListOf<String>()

    override fun searchArtists(query: String): Flow<PagingData<Artist>> {
        queriesReceived.add(query)
        return flowOf(PagingData.empty())
    }
}
