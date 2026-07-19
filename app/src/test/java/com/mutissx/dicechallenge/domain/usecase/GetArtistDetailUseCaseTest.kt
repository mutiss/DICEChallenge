package com.mutissx.dicechallenge.domain.usecase

import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetArtistDetailUseCaseTest {

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var useCase: GetArtistDetailUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        useCase = GetArtistDetailUseCase(fakeRepository)
    }

    @Test
    fun `given repository returns a successful artist, when invoke is called, then the same Result is returned`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = "GB", disambiguation = null, score = 100)
            fakeRepository.artistResult = Result.Success(artist)

            // When
            val result = useCase("id-1")

            // Then
            assertEquals(Result.Success(artist), result)
        }

    @Test
    fun `given repository returns an error, when invoke is called, then the same Result is returned`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Error(DataError.Network.NOT_FOUND)

            // When
            val result = useCase("missing-id")

            // Then
            assertEquals(Result.Error(DataError.Network.NOT_FOUND), result)
        }
}
