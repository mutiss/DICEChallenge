package com.mutissx.dicechallenge.fake

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mutissx.dicechallenge.domain.model.Artist

class FakeArtistPagingSource(
    private val resultProvider: () -> Result<List<Artist>>
) : PagingSource<Int, Artist>() {

    constructor(result: Result<List<Artist>>) : this({ result })

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> =
        resultProvider().fold(
            onSuccess = { artists -> LoadResult.Page(data = artists, prevKey = null, nextKey = null) },
            onFailure = { error -> LoadResult.Error(error) }
        )

    override fun getRefreshKey(state: PagingState<Int, Artist>): Int? = null
}
