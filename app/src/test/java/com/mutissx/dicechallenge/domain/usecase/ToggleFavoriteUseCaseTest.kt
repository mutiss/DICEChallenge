package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleFavoriteUseCaseTest {

    private lateinit var fakeRepository: FakeFavoritesRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    private val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        useCase = ToggleFavoriteUseCase(fakeRepository)
    }

    @Test
    fun `given the artist is not currently favorite, when invoke is called, then the repository add is called`() =
        runTest {
            // When
            useCase(artist, isCurrentlyFavorite = false)

            // Then
            assertEquals(listOf(artist), fakeRepository.addedArtists)
            assertTrue(fakeRepository.removedMbids.isEmpty())
        }

    @Test
    fun `given the artist is currently favorite, when invoke is called, then the repository remove is called`() =
        runTest {
            // When
            useCase(artist, isCurrentlyFavorite = true)

            // Then
            assertEquals(listOf(artist.mbid), fakeRepository.removedMbids)
            assertTrue(fakeRepository.addedArtists.isEmpty())
        }

    @Test
    fun `given the repository add fails, when invoke is called, then the failure Result is returned`() =
        runTest {
            // Given
            fakeRepository.writeResult = Result.Error(DataError.Local.DISK_FULL)

            // When
            val result = useCase(artist, isCurrentlyFavorite = false)

            // Then
            assertEquals(Result.Error(DataError.Local.DISK_FULL), result)
            assertTrue(fakeRepository.addedArtists.isEmpty())
        }

    @Test
    fun `given the repository remove fails, when invoke is called, then the failure Result is returned`() =
        runTest {
            // Given
            fakeRepository.writeResult = Result.Error(DataError.Local.UNKNOWN)

            // When
            val result = useCase(artist, isCurrentlyFavorite = true)

            // Then
            assertEquals(Result.Error(DataError.Local.UNKNOWN), result)
            assertTrue(fakeRepository.removedMbids.isEmpty())
        }
}
