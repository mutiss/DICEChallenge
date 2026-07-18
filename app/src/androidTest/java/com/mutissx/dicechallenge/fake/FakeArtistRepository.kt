package com.mutissx.dicechallenge.fake

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

class FakeArtistRepository : ArtistRepository {

    var pagingSourceFactory: () -> FakeArtistPagingSource =
        { FakeArtistPagingSource(Result.success(emptyList())) }

    override fun searchArtists(query: String): Flow<PagingData<Artist>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { pagingSourceFactory() }
        ).flow
}
