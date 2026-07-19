package com.mutissx.dicechallenge.presentation.favorites

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import com.mutissx.dicechallenge.presentation.favorites.screen.FavoritesUiState
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.dicechallenge.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        viewModel = FavoritesViewModel(ObserveFavoritesUseCase(fakeRepository))
    }

    @Test
    fun `given viewModel just created, when uiState is observed before collection starts, then the initial state is Loading`() {
        assertTrue(viewModel.uiState.value is FavoritesUiState.Loading)
    }

    @Test
    fun `given no favorites in the repository, when uiState is collected, then it emits Content with an empty list`() =
        runTest {
            // Given / When
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is FavoritesUiState.Content)
            assertTrue((state as FavoritesUiState.Content).favorites.isEmpty())
        }

    @Test
    fun `given favorites already exist in the repository, when uiState is collected, then it emits them`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)

            // When
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is FavoritesUiState.Content)
            assertEquals(listOf(artist), (state as FavoritesUiState.Content).favorites)
        }

    @Test
    fun `given uiState is being collected, when an artist is added to the repository, then uiState reflects the new list`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertTrue((viewModel.uiState.value as FavoritesUiState.Content).favorites.isEmpty())

            // When
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)
            advanceUntilIdle()

            // Then
            assertEquals(listOf(artist), (viewModel.uiState.value as FavoritesUiState.Content).favorites)
        }

    @Test
    fun `given uiState is being collected, when an artist is removed from the repository, then uiState reflects its removal`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertEquals(listOf(artist), (viewModel.uiState.value as FavoritesUiState.Content).favorites)

            // When
            fakeRepository.remove(artist.mbid)
            advanceUntilIdle()

            // Then
            assertTrue((viewModel.uiState.value as FavoritesUiState.Content).favorites.isEmpty())
        }

    @Test
    fun `given the repository read fails, when uiState is collected, then it emits Error instead of crashing`() =
        runTest {
            // Given — a separate repository/viewModel instance so readError is set before uiState's pipeline is built
            val failingRepository = FakeFavoritesRepository().apply {
                readError = RuntimeException("disk read failure")
            }
            val failingViewModel = FavoritesViewModel(ObserveFavoritesUseCase(failingRepository))

            // When
            backgroundScope.launch { failingViewModel.uiState.collect { } }
            advanceUntilIdle()

            // Then
            assertTrue(failingViewModel.uiState.value is FavoritesUiState.Error)
        }
}
