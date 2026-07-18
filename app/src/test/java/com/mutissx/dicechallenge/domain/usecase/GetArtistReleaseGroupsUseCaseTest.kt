package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetArtistReleaseGroupsUseCaseTest {

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var useCase: GetArtistReleaseGroupsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        useCase = GetArtistReleaseGroupsUseCase(fakeRepository)
    }

    @Test
    fun `given repository returns successful release groups, when invoke is called, then the same Result is returned`() =
        runTest {
            // Given
            val releaseGroups = listOf(
                ReleaseGroup(mbid = "rg-1", title = "OK Computer", firstReleaseYear = "1997", primaryType = "Album", coverArtUrl = "")
            )
            fakeRepository.releaseGroupsResult = Result.Success(releaseGroups)

            // When
            val result = useCase("artist-id")

            // Then
            assertEquals(Result.Success(releaseGroups), result)
        }

    @Test
    fun `given repository returns an error, when invoke is called, then the same Result is returned`() =
        runTest {
            // Given
            fakeRepository.releaseGroupsResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

            // When
            val result = useCase("artist-id")

            // Then
            assertEquals(Result.Error(DataError.Network.SERVICE_UNAVAILABLE), result)
        }
}
