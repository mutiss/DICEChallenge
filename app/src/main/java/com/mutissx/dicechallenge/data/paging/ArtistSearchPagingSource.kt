package com.mutissx.dicechallenge.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mutissx.dicechallenge.data.mapper.toDomain
import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.core.data.toNetworkError
import com.mutissx.dicechallenge.core.domain.DataException
import com.mutissx.dicechallenge.domain.model.Artist
import kotlinx.coroutines.CancellationException

class ArtistSearchPagingSource(
    private val api: MusicBrainzApi,
    private val query: String
) : PagingSource<Int, Artist>() {

    // MusicBrainz's search API doesn't guarantee a stable sort across requests when
    // results tie on relevance score, so overlapping offsets can return the same
    // mbid on more than one page. Track what we've already emitted for this query
    // session (one instance per query/refresh) and drop repeats.
    private val seenMbids = mutableSetOf<String>()

    override fun getRefreshKey(state: PagingState<Int, Artist>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(PAGE_SIZE) ?: page.nextKey?.minus(PAGE_SIZE)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> {
        val offset = params.key ?: 0
        return try {
            val response = api.searchArtists(
                query = query,
                limit = PAGE_SIZE,
                offset = offset
            )
            val rawItems = response.artists.map { it.toDomain() }
            // If every mbid on this page was already seen on a prior page (see the dedup note
            // above), items can legitimately come back empty while nextKey is still non-null —
            // that's expected, not a bug: Paging will trigger another load for the next offset.
            val items = rawItems.filter { seenMbids.add(it.mbid) }
            val nextOffset = offset + rawItems.size
            LoadResult.Page(
                data = items,
                prevKey = if (offset == 0) null else (offset - PAGE_SIZE).coerceAtLeast(0),
                nextKey = if (rawItems.isEmpty() || nextOffset >= response.count) null else nextOffset
            )
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            LoadResult.Error(DataException(t.toNetworkError()))
        }
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}
