package com.mutissx.dicechallenge.presentation.favorites

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.dicechallenge.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `given viewModel just created, when uiState is observed before collection starts, then the initial state is loading with no favorites`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.favorites.isEmpty())
    }

    @Test
    fun `given no favorites in the repository, when uiState is collected, then it emits a non-loading state with an empty list`() =
        runTest {
            // Given / When
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.favorites.isEmpty())
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
            assertEquals(listOf(artist), viewModel.uiState.value.favorites)
        }

    @Test
    fun `given uiState is being collected, when an artist is added to the repository, then uiState reflects the new list`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.favorites.isEmpty())

            // When
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)
            advanceUntilIdle()

            // Then
            assertEquals(listOf(artist), viewModel.uiState.value.favorites)
        }

    @Test
    fun `given uiState is being collected, when an artist is removed from the repository, then uiState reflects its removal`() =
        runTest {
            // Given
            val artist = Artist(mbid = "id-1", name = "Radiohead", country = null, disambiguation = null, score = null)
            fakeRepository.add(artist)
            backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertEquals(listOf(artist), viewModel.uiState.value.favorites)

            // When
            fakeRepository.remove(artist.mbid)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value.favorites.isEmpty())
        }

    @Test
    fun `given the repository read fails, when uiState is collected, then it emits a non-loading error state instead of crashing`() =
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
            val state = failingViewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isError)
            assertTrue(state.favorites.isEmpty())
        }
}
