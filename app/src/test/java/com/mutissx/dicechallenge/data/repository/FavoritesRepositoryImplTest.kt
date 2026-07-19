package com.mutissx.dicechallenge.data.repository

import app.cash.turbine.test
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.data.local.FavoriteArtistDao
import com.mutissx.dicechallenge.data.local.FavoriteArtistEntity
import com.mutissx.dicechallenge.domain.model.Artist
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    private val mockDao: FavoriteArtistDao = mockk()
    private lateinit var repository: FavoritesRepositoryImpl

    private val fixedNow = 1_700_000_000_000L

    @Before
    fun setUp() {
        repository = FavoritesRepositoryImpl(mockDao, clock = { fixedNow })
    }

    @Test
    fun `given dao returns entities, when observeAll is collected, then it emits the mapped domain artists`() =
        runTest {
            // Given
            val entities = listOf(
                FavoriteArtistEntity(mbid = "id-1", name = "Radiohead", country = "GB", disambiguation = null, addedAt = 1L)
            )
            every { mockDao.observeAll() } returns flowOf(entities)

            // When / Then
            repository.observeAll().test {
                val artists = awaitItem()
                assertEquals(1, artists.size)
                assertEquals("id-1", artists.first().mbid)
                assertEquals("Radiohead", artists.first().name)
                assertEquals("GB", artists.first().country)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given dao returns no entities, when observeAll is collected, then it emits an empty list`() =
        runTest {
            // Given
            every { mockDao.observeAll() } returns flowOf(emptyList())

            // When / Then
            repository.observeAll().test {
                assertEquals(emptyList<Artist>(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given dao emits a favorite status, when observeIsFavorite is collected, then it delegates to the dao unchanged`() =
        runTest {
            // Given
            every { mockDao.observeIsFavorite("id-1") } returns flowOf(true)

            // When / Then
            repository.observeIsFavorite("id-1").test {
                assertEquals(true, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an artist, when add is called, then the dao inserts an entity stamped with the clock time and returns Success`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = "GB", disambiguation = "British band", score = null)
            coEvery { mockDao.insert(any()) } returns Unit

            // When
            val result = repository.add(artist)

            // Then
            assertEquals(Result.Success(Unit), result)
            coVerify {
                mockDao.insert(
                    FavoriteArtistEntity(
                        mbid = "id-1",
                        name = "Radiohead",
                        country = "GB",
                        disambiguation = "British band",
                        addedAt = fixedNow
                    )
                )
            }
        }

    @Test
    fun `given an mbid, when remove is called, then the dao deletes that mbid and returns Success`() =
        runTest {
            // Given
            coEvery { mockDao.delete("id-1") } returns Unit

            // When
            val result = repository.remove("id-1")

            // Then
            assertEquals(Result.Success(Unit), result)
            coVerify { mockDao.delete("id-1") }
        }

    @Test
    fun `given the dao throws on insert, when add is called, then it returns Result Error instead of propagating`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            coEvery { mockDao.insert(any()) } throws RuntimeException("disk full")

            // When
            val result = repository.add(artist)

            // Then
            assertTrue(result is Result.Error)
            assertEquals(DataError.Local.UNKNOWN, (result as Result.Error).error)
        }

    @Test
    fun `given the dao throws on delete, when remove is called, then it returns Result Error instead of propagating`() =
        runTest {
            // Given
            coEvery { mockDao.delete("id-1") } throws RuntimeException("disk full")

            // When
            val result = repository.remove("id-1")

            // Then
            assertTrue(result is Result.Error)
            assertEquals(DataError.Local.UNKNOWN, (result as Result.Error).error)
        }
}
