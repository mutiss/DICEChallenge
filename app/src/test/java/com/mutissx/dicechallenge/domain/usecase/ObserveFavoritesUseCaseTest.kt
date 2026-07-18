package com.mutissx.dicechallenge.domain.usecase

import app.cash.turbine.test
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveFavoritesUseCaseTest {

    private lateinit var fakeRepository: FakeFavoritesRepository
    private lateinit var useCase: ObserveFavoritesUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        useCase = ObserveFavoritesUseCase(fakeRepository)
    }

    @Test
    fun `given no favorites, when invoke is collected, then it emits an empty list`() =
        runTest {
            useCase().test {
                assertEquals(emptyList<Artist>(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an artist is added to favorites, when invoke is collected, then it emits the updated list`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)

            useCase().test {
                assertEquals(emptyList<Artist>(), awaitItem())

                // When
                fakeRepository.add(artist)

                // Then
                assertEquals(listOf(artist), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a favorite is removed, when invoke is collected, then it emits the list without that artist`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)

            useCase().test {
                assertEquals(listOf(artist), awaitItem())

                // When
                fakeRepository.remove(artist.mbid)

                // Then
                assertEquals(emptyList<Artist>(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
