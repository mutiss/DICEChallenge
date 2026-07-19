package com.mutissx.dicechallenge.domain.usecase

import app.cash.turbine.test
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveIsFavoriteUseCaseTest {

    private lateinit var fakeRepository: FakeFavoritesRepository
    private lateinit var useCase: ObserveIsFavoriteUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        useCase = ObserveIsFavoriteUseCase(fakeRepository)
    }

    @Test
    fun `given the artist is not a favorite, when invoke is collected, then it emits false`() =
        runTest {
            useCase("id-1").test {
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the artist becomes a favorite, when invoke is collected, then it emits true`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)

            useCase("id-1").test {
                assertFalse(awaitItem())

                // When
                fakeRepository.add(artist)

                // Then
                assertTrue(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a different artist is added to favorites, when invoke is collected for another mbid, then it still emits false`() =
        runTest {
            // Given
            val otherArtist = Artist(mbid = "other-id", name = "Coldplay", country = null, disambiguation = null, score = null)

            useCase("id-1").test {
                assertFalse(awaitItem())

                // When
                fakeRepository.add(otherArtist)

                // Then — the underlying list changed, so the mapped flow re-emits, but still false for this mbid
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
