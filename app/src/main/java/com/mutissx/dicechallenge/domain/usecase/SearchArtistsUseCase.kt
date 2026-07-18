package com.mutissx.dicechallenge.domain.usecase

import androidx.paging.PagingData
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

class SearchArtistsUseCase(
    private val repository: ArtistRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Artist>> =
        repository.searchArtists(query)
}
