package com.mutissx.dicechallenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.domain.usecase.IsFavoriteUseCase
import com.mutissx.dicechallenge.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import com.mutissx.dicechallenge.presentation.detail.screen.ArtistDetailUiState
import com.mutissx.dicechallenge.presentation.detail.screen.ReleaseGroupsState
import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import com.mutissx.dicechallenge.presentation.navigation.Destination
import com.mutissx.dicechallenge.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var fakeFavoritesRepository: FakeFavoritesRepository
    private lateinit var getArtistDetailUseCase: GetArtistDetailUseCase
    private lateinit var getReleaseGroupsUseCase: GetArtistReleaseGroupsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var isFavoriteUseCase: IsFavoriteUseCase

    private val mbid = "artist-123"

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        fakeFavoritesRepository = FakeFavoritesRepository()
        getArtistDetailUseCase = GetArtistDetailUseCase(fakeRepository)
        getReleaseGroupsUseCase = GetArtistReleaseGroupsUseCase(fakeRepository)
        toggleFavoriteUseCase = ToggleFavoriteUseCase(fakeFavoritesRepository)
        isFavoriteUseCase = IsFavoriteUseCase(fakeFavoritesRepository)
    }

    private fun createViewModel(mbidValue: String? = mbid): ArtistDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(Destination.ArtistDetail.ARG_MBID to mbidValue))
        return ArtistDetailViewModel(
            savedStateHandle,
            getArtistDetailUseCase,
            getReleaseGroupsUseCase,
            toggleFavoriteUseCase,
            isFavoriteUseCase
        )
    }

    @Test
    fun `given viewModel just created, when uiState is observed before the load coroutines run, then state is Loading`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())

            // When
            val viewModel = createViewModel()

            // Then — StandardTestDispatcher hasn't run the load coroutines yet
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Loading)
        }

    @Test
    fun `given successful artist and release groups fetch, when viewModel is created, then uiState becomes Content with releaseGroups Loaded`() =
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
            assertEquals(ReleaseGroupsState.Loaded(releaseGroups), state.releaseGroups)
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
    fun `given artist succeeds but release groups fetch fails, when viewModel is created, then uiState is Content with a releaseGroups Error section`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then — the artist section is unaffected by the release groups failure
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Content)
            assertEquals(artist, (state as ArtistDetailUiState.Content).artist)
            val releaseGroupsState = state.releaseGroups
            assertTrue(releaseGroupsState is ReleaseGroupsState.Error)
            assertEquals(R.string.server_error, ((releaseGroupsState as ReleaseGroupsState.Error).message as UiText.StringResource).resId)
        }

    @Test
    fun `given both artist and release groups fail, when viewModel is created, then uiState becomes Error using the artist error`() =
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
    fun `given a releaseGroups Error section, when retryReleaseGroups is called after the repository recovers, then releaseGroups becomes Loaded without affecting the artist`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue((viewModel.uiState.value as ArtistDetailUiState.Content).releaseGroups is ReleaseGroupsState.Error)

            // When
            val releaseGroups = listOf(
                ReleaseGroup(mbid = "rg-1", title = "OK Computer", firstReleaseYear = "1997", primaryType = "Album", coverArtUrl = "")
            )
            fakeRepository.releaseGroupsResult = Result.Success(releaseGroups)
            viewModel.retryReleaseGroups()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as ArtistDetailUiState.Content
            assertEquals(artist, state.artist)
            assertEquals(ReleaseGroupsState.Loaded(releaseGroups), state.releaseGroups)
        }

    @Test
    fun `given a successful initial load, when loadInfo is called again, then it re-fetches and uiState becomes Content with the refreshed data`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Content)

            // When — repository data changes and loadInfo is called again
            val refreshedArtist = Artist(mbid, "Radiohead", "GB", "British band", 100)
            fakeRepository.artistResult = Result.Success(refreshedArtist)
            viewModel.loadInfo()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Content)
            assertEquals(refreshedArtist, (state as ArtistDetailUiState.Content).artist)
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
        ArtistDetailViewModel(
            savedStateHandle,
            getArtistDetailUseCase,
            getReleaseGroupsUseCase,
            toggleFavoriteUseCase,
            isFavoriteUseCase
        )
    }

    // ---- favorite toggle tests ----

    @Test
    fun `given content loaded and not favorite, when onFavoriteToggle is called, then repository add is invoked and isFavorite becomes true`() =
        runTest {
            // Given
            fakeRepository.artistResult = Result.Success(Artist(mbid, "Radiohead", null, null, null))
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()
            val contentBefore = viewModel.uiState.value as ArtistDetailUiState.Content
            assertFalse(contentBefore.isFavorite)

            // When
            viewModel.onFavoriteToggle()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(mbid), fakeFavoritesRepository.addedArtists.map { it.mbid })
            val contentAfter = viewModel.uiState.value as ArtistDetailUiState.Content
            assertTrue(contentAfter.isFavorite)
        }

    @Test
    fun `given content loaded and already favorite, when onFavoriteToggle is called, then repository remove is invoked and isFavorite becomes false`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            fakeFavoritesRepository.add(artist)
            val viewModel = createViewModel()
            advanceUntilIdle()
            val contentBefore = viewModel.uiState.value as ArtistDetailUiState.Content
            assertTrue(contentBefore.isFavorite)

            // When
            viewModel.onFavoriteToggle()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(mbid), fakeFavoritesRepository.removedMbids)
            val contentAfter = viewModel.uiState.value as ArtistDetailUiState.Content
            assertFalse(contentAfter.isFavorite)
        }

    @Test
    fun `given an artist that is not favorite, when onFavoriteToggle is called, then favoriteMessages emits the added message with the artist name`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.favoriteMessages.test {
                // When
                viewModel.onFavoriteToggle()

                // Then
                val message = awaitItem() as UiText.StringResource
                assertEquals(R.string.favorite_added, message.resId)
                assertEquals("Radiohead", message.args.first())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an artist that is already favorite, when onFavoriteToggle is called, then favoriteMessages emits the removed message with the artist name`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            fakeFavoritesRepository.add(artist)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.favoriteMessages.test {
                // When
                viewModel.onFavoriteToggle()

                // Then
                val message = awaitItem() as UiText.StringResource
                assertEquals(R.string.favorite_removed, message.resId)
                assertEquals("Radiohead", message.args.first())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the favorites repository write fails, when onFavoriteToggle is called, then favoriteMessages emits an error instead of crashing`() =
        runTest {
            // Given
            val artist = Artist(mbid, "Radiohead", null, null, null)
            fakeRepository.artistResult = Result.Success(artist)
            fakeRepository.releaseGroupsResult = Result.Success(emptyList())
            fakeFavoritesRepository.writeResult = Result.Error(DataError.Local.DISK_FULL)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.favoriteMessages.test {
                // When — this must not throw or kill the ViewModel's coroutine scope
                viewModel.onFavoriteToggle()

                // Then
                val message = awaitItem() as UiText.StringResource
                assertEquals(R.string.favorite_toggle_error, message.resId)
                cancelAndIgnoreRemainingEvents()
            }

            // isFavorite is unchanged since the write never actually succeeded
            val content = viewModel.uiState.value as ArtistDetailUiState.Content
            assertFalse(content.isFavorite)
        }
}
