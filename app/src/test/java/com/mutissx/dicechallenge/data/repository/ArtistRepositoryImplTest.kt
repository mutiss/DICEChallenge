package com.mutissx.dicechallenge.data.repository

import app.cash.turbine.test
import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.data.remote.model.ArtistSearchResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistRepositoryImplTest {

    private val mockApi: MusicBrainzApi = mockk()
    private lateinit var repository: ArtistRepositoryImpl

    @Before
    fun setUp() {
        repository = ArtistRepositoryImpl(mockApi)
    }

    @Test
    fun `given valid query, when searchArtists is called, then flow emits at least one PagingData`() =
        runTest {
            // Given
            val query = "nirvana"
            coEvery { mockApi.searchArtists(query, any(), any()) } returns
                ArtistSearchResponseDto(count = 1, artists = listOf(ArtistDto("id-1", "Nirvana")))

            // When
            val flow = repository.searchArtists(query)

            // Then
            flow.test {
                assertNotNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the same query called twice, when searchArtists is called, then each call returns a new independent flow`() =
        runTest {
            // Given
            val query = "pink floyd"

            // When
            val flow1 = repository.searchArtists(query)
            val flow2 = repository.searchArtists(query)

            // Then — Pager creates a fresh flow (and PagingSource) per call
            assertNotSame(flow1, flow2)
        }

    @Test
    fun `given two different queries, when searchArtists is called for each, then distinct flow instances are returned`() =
        runTest {
            // Given
            val query1 = "metallica"
            val query2 = "slayer"

            // When
            val flow1 = repository.searchArtists(query1)
            val flow2 = repository.searchArtists(query2)

            // Then — each Pager creates a new flow instance
            assertNotSame(flow1, flow2)
        }
}
