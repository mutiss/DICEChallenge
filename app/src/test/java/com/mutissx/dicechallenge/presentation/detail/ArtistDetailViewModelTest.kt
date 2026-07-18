package com.mutissx.dicechallenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import com.mutissx.dicechallenge.presentation.detail.screen.ArtistDetailUiState
import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import com.mutissx.dicechallenge.presentation.navigation.Destination
import com.mutissx.dicechallenge.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var getArtistDetailUseCase: GetArtistDetailUseCase
    private lateinit var getReleaseGroupsUseCase: GetArtistReleaseGroupsUseCase

    private val mbid = "artist-123"

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        getArtistDetailUseCase = GetArtistDetailUseCase(fakeRepository)
        getReleaseGroupsUseCase = GetArtistReleaseGroupsUseCase(fakeRepository)
    }

    private fun createViewModel(mbidValue: String? = mbid): ArtistDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(Destination.ArtistDetail.ARG_MBID to mbidValue))
        return ArtistDetailViewModel(
            savedStateHandle,
            getArtistDetailUseCase,
            getReleaseGroupsUseCase
        )
    }

    @Test
    fun `given viewModel just created, when uiState is observed before the load coroutine runs, then state is Loading`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())

            // When
            val viewModel = createViewModel()

            // Then — StandardTestDispatcher hasn't run the init coroutine yet
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Loading)
        }

    @Test
    fun `given successful artist and release groups fetch, when viewModel is created, then uiState becomes Content`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", "GB", null, 100)
            val releaseGroups = listOf(
                ReleaseGroup(mbid = "rg-1", title = "OK Computer", firstReleaseYear = "1997", primaryType = "Album", coverArtUrl = "")
            )
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Success(releaseGroups)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Content)
            assertEquals(artist, (state as ArtistDetailUiState.Content).artist)
            assertEquals(releaseGroups, state.releaseGroups)
        }

    @Test
    fun `given artist fetch fails, when viewModel is created, then uiState becomes Error with the mapped UiText`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Error(DataError.Network.NOT_FOUND)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Error)
            val message = (state as ArtistDetailUiState.Error).message
            assertEquals(R.string.not_found, (message as UiText.StringResource).resId)
        }

    @Test
    fun `given release groups fetch fails, when viewModel is created, then uiState becomes Error with the mapped UiText`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Error)
            val message = (state as ArtistDetailUiState.Error).message
            assertEquals(R.string.server_error, (message as UiText.StringResource).resId)
        }

    @Test
    fun `given both artist and release groups fail, when viewModel is created, then the artist error takes priority`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Error(DataError.Network.UNAUTHORIZED)
            fakeRepository.releaseGroupsResult = Result.Error(DataError.Network.NOT_FOUND)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Error)
            val message = (state as ArtistDetailUiState.Error).message
            assertEquals(R.string.unauthorized, (message as UiText.StringResource).resId)
        }

    @Test
    fun `given a successful initial load, when loadInfo is called again, then uiState resets to Loading before resolving`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Content)

            // When
            viewModel.loadInfo()

            // Then — synchronously reset before the relaunched coroutine resolves
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Loading)

            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Content)
        }

    @Test
    fun `given a failed initial load, when loadInfo is retried after the repository recovers, then uiState becomes Content`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Error)

            // When
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            viewModel.loadInfo()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Content)
            assertEquals(artist, (state as ArtistDetailUiState.Content).artist)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `given a SavedStateHandle without the mbid argument, when viewModel is constructed, then it throws`() {
        val savedStateHandle = SavedStateHandle()
        ArtistDetailViewModel(savedStateHandle, getArtistDetailUseCase, getReleaseGroupsUseCase)
    }
}
