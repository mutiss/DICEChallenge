package com.mutissx.dicechallenge.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.data.remote.model.ArtistSearchResponseDto
import com.mutissx.dicechallenge.domain.model.Artist
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ArtistSearchPagingSourceTest {

    private val mockApi: MusicBrainzApi = mockk()
    private val query = "radiohead"
    private lateinit var pagingSource: ArtistSearchPagingSource

    @Before
    fun setUp() {
        pagingSource = ArtistSearchPagingSource(mockApi, query)
    }

    private fun makeDto(id: String, name: String = "Artist $id") = ArtistDto(id = id, name = name)

    private fun makeResponse(count: Int, artists: List<ArtistDto> = emptyList()) =
        ArtistSearchResponseDto(count = count, artists = artists)

    private fun refreshParams(key: Int? = null) =
        LoadParams.Refresh(key = key, loadSize = ArtistSearchPagingSource.PAGE_SIZE, placeholdersEnabled = false)

    @Test
    fun `given first page load, when load is called with null key, then prevKey is null`() = runTest {
        // Given
        val dtos = (1..25).map { makeDto("id-$it") }
        coEvery { mockApi.searchArtists(query, any(), 0) } returns makeResponse(count = 100, artists = dtos)

        // When
        val result = pagingSource.load(refreshParams()) as LoadResult.Page

        // Then
        assertNull(result.prevKey)
    }

    @Test
    fun `given full page returned with more results available, when load is called, then nextKey equals page size`() = runTest {
        // Given
        val pageSize = ArtistSearchPagingSource.PAGE_SIZE
        val dtos = (1..pageSize).map { makeDto("id-$it") }
        coEvery { mockApi.searchArtists(query, any(), 0) } returns makeResponse(count = 100, artists = dtos)

        // When
        val result = pagingSource.load(refreshParams()) as LoadResult.Page

        // Then
        assertEquals(pageSize, result.nextKey)
    }

    @Test
    fun `given partial last page returned, when load is called, then nextKey is null`() = runTest {
        // Given
        val dtos = (1..10).map { makeDto("id-$it") }
        coEvery { mockApi.searchArtists(query, any(), 0) } returns makeResponse(count = 10, artists = dtos)

        // When
        val result = pagingSource.load(refreshParams()) as LoadResult.Page

        // Then
        assertNull(result.nextKey)
    }

    @Test
    fun `given empty response, when load is called, then nextKey is null and data is empty`() = runTest {
        // Given
        coEvery { mockApi.searchArtists(query, any(), 0) } returns makeResponse(count = 0)

        // When
        val result = pagingSource.load(refreshParams()) as LoadResult.Page

        // Then
        assertNull(result.nextKey)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `given second page load with offset 25, when load is called, then prevKey is 0`() = runTest {
        // Given
        val pageSize = ArtistSearchPagingSource.PAGE_SIZE
        val dtos = (26..50).map { makeDto("id-$it") }
        coEvery { mockApi.searchArtists(query, any(), pageSize) } returns makeResponse(count = 100, artists = dtos)

        // When
        val result = pagingSource.load(refreshParams(key = pageSize)) as LoadResult.Page

        // Then
        assertEquals(0, result.prevKey)
    }

    @Test
    fun `given duplicate mbid on second page, when load is called, then duplicate is filtered out`() = runTest {
        // Given
        val pageSize = ArtistSearchPagingSource.PAGE_SIZE
        val sharedDto = makeDto("dup-id", "Duplicate Artist")
        val firstPageDtos = listOf(sharedDto) + (2..pageSize).map { makeDto("id-$it") }
        val secondPageDtos = listOf(sharedDto) + (pageSize + 2..pageSize * 2).map { makeDto("id-$it") }

        coEvery { mockApi.searchArtists(query, any(), 0) } returns makeResponse(count = 100, artists = firstPageDtos)
        coEvery { mockApi.searchArtists(query, any(), pageSize) } returns makeResponse(count = 100, artists = secondPageDtos)

        // When — first load primes seenMbids
        pagingSource.load(refreshParams())
        val secondPage = pagingSource.load(refreshParams(key = pageSize)) as LoadResult.Page

        // Then
        assertTrue(secondPage.data.none { it.mbid == "dup-id" })
    }

    @Test
    fun `given api throws IOException, when load is called, then returns LoadResult Error with the throwable`() = runTest {
        // Given
        val error = IOException("Network failure")
        coEvery { mockApi.searchArtists(query, any(), 0) } throws error

        // When
        val result = pagingSource.load(refreshParams())

        // Then
        assertTrue(result is LoadResult.Error)
        assertEquals(error, (result as LoadResult.Error).throwable)
    }

    @Test
    fun `given api throws CancellationException, when load is called, then exception is rethrown instead of wrapped`() = runTest {
        // Given
        coEvery { mockApi.searchArtists(query, any(), 0) } throws CancellationException("Cancelled")

        // When
        var wasCancellationThrown = false
        try {
            pagingSource.load(refreshParams())
        } catch (e: CancellationException) {
            wasCancellationThrown = true
        }

        // Then
        assertTrue(wasCancellationThrown)
    }

    @Test
    fun `given anchor at first position and page has nextKey, when getRefreshKey is called, then returns nextKey minus page size`() {
        // Given
        val pageSize = ArtistSearchPagingSource.PAGE_SIZE
        val page = LoadResult.Page(
            data = (1..pageSize).map { Artist("id-$it", "Artist $it", null, null, null) },
            prevKey = null,
            nextKey = pageSize
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 0,
            config = PagingConfig(pageSize = pageSize),
            leadingPlaceholderCount = 0
        )

        // When
        val refreshKey = pagingSource.getRefreshKey(state)

        // Then — nextKey(25) - PAGE_SIZE(25) = 0
        assertEquals(0, refreshKey)
    }

    @Test
    fun `given no anchor position in paging state, when getRefreshKey is called, then returns null`() {
        // Given
        val state = PagingState<Int, Artist>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = ArtistSearchPagingSource.PAGE_SIZE),
            leadingPlaceholderCount = 0
        )

        // When
        val refreshKey = pagingSource.getRefreshKey(state)

        // Then
        assertNull(refreshKey)
    }
}
