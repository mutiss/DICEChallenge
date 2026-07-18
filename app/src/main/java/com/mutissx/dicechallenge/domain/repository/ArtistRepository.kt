package com.mutissx.dicechallenge.domain.repository

import androidx.paging.PagingData
import com.mutissx.dicechallenge.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun searchArtists(query: String): Flow<PagingData<Artist>>
}
