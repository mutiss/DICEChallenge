package com.mutissx.dicechallenge.domain.usecase

import app.cash.turbine.test
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchArtistsUseCaseTest {

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var useCase: SearchArtistsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        useCase = SearchArtistsUseCase(fakeRepository)
    }

    @Test
    fun `given a valid query, when invoke is called, then repository receives the exact same query`() =
        runTest {
            // Given
            val query = "coldplay"

            // When
            useCase(query)

            // Then
            assertEquals(listOf(query), fakeRepository.queriesReceived)
        }

    @Test
    fun `given a configured repository, when invoke is called, then returned flow emits at least one item`() =
        runTest {
            // Given
            val query = "beatles"

            // When
            val resultFlow = useCase(query)

            // Then — a real Pager-backed flow (unlike a plain flowOf) stays open for
            // further generations (retry/invalidation), so it never completes on its own.
            resultFlow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an empty string query, when invoke is called, then repository still receives the empty string`() =
        runTest {
            // Given
            val query = ""

            // When
            useCase(query)

            // Then
            assertEquals(listOf(""), fakeRepository.queriesReceived)
        }
}
