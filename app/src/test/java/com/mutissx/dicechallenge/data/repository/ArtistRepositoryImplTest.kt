package com.mutissx.dicechallenge.data.repository

import app.cash.turbine.test
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.data.remote.api.MusicBrainzApi
import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.data.remote.model.ArtistSearchResponseDto
import com.mutissx.dicechallenge.data.remote.model.ReleaseGroupDto
import com.mutissx.dicechallenge.data.remote.model.ReleaseGroupsResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

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

    // ---- getArtist tests ----

    @Test
    fun `given api returns an artist, when getArtist is called, then Result Success wraps the mapped domain artist`() =
        runTest {
            // Given
            val dto = ArtistDto(id = "id-1", name = "Radiohead", country = "GB")
            coEvery { mockApi.getArtist("id-1") } returns dto

            // When
            val result = repository.getArtist("id-1")

            // Then
            assertTrue(result is Result.Success)
            val artist = (result as Result.Success).data
            assertEquals("id-1", artist.mbid)
            assertEquals("Radiohead", artist.name)
            assertEquals("GB", artist.country)
        }

    @Test
    fun `given api throws a 404 HttpException, when getArtist is called, then Result Error wraps NOT_FOUND`() =
        runTest {
            // Given
            coEvery { mockApi.getArtist("missing-id") } throws
                HttpException(Response.error<Any>(404, "".toResponseBody(null)))

            // When
            val result = repository.getArtist("missing-id")

            // Then
            assertEquals(Result.Error(DataError.Network.NOT_FOUND), result)
        }

    // ---- getReleaseGroups tests ----

    @Test
    fun `given api returns release groups, when getReleaseGroups is called, then Result Success is sorted by year descending`() =
        runTest {
            // Given
            val older = ReleaseGroupDto(id = "rg-old", title = "Pablo Honey", firstReleaseDate = "1993-02-22")
            val newer = ReleaseGroupDto(id = "rg-new", title = "In Rainbows", firstReleaseDate = "2007-10-10")
            coEvery { mockApi.getReleaseGroups(artistMbid = "id-1") } returns
                ReleaseGroupsResponseDto(count = 2, releaseGroups = listOf(older, newer))

            // When
            val result = repository.getReleaseGroups("id-1")

            // Then
            assertTrue(result is Result.Success)
            val titles = (result as Result.Success).data.map { it.title }
            assertEquals(listOf("In Rainbows", "Pablo Honey"), titles)
        }

    @Test
    fun `given api throws IOException, when getReleaseGroups is called, then Result Error wraps NO_INTERNET`() =
        runTest {
            // Given
            coEvery { mockApi.getReleaseGroups(artistMbid = "id-1") } throws IOException("no network")

            // When
            val result = repository.getReleaseGroups("id-1")

            // Then
            assertEquals(Result.Error(DataError.Network.NO_INTERNET), result)
        }
}
