package com.mutissx.dicechallenge.presentation.search.viewmodel

import app.cash.turbine.test
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.DataException
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        viewModel = SearchViewModel(SearchArtistsUseCase(fakeRepository))
    }

    // ---- query StateFlow tests ----

    @Test
    fun `given initial state, when viewModel is created, then query is empty string`() =
        runTest {
            // Given / When — ViewModel created in setUp

            // Then
            assertEquals(SearchViewModel.EMPTY_VALUE, viewModel.query.value)
        }

    @Test
    fun `given a new query value, when onQueryChange is called, then query StateFlow reflects it`() =
        runTest{
            // Given
            val newQuery = "radiohead"

            // When
            viewModel.onQueryChange(newQuery)

            // Then
            viewModel.query.test {
                assertEquals(newQuery, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given any state, when onClearQuery is called, then query becomes empty string`() =
        runTest {
            // Given
            viewModel.onQueryChange("some text")

            // When
            viewModel.onClearQuery()

            // Then
            assertEquals(SearchViewModel.EMPTY_VALUE, viewModel.query.value)
        }

    // ---- results flow tests ----
    //
    // cachedIn (Paging 3) is lazy: it only starts collecting the upstream when a
    // downstream subscriber is present. Each test starts a background collector on
    // `results` to activate the debounce / flatMapLatest pipeline, then asserts on
    // fakeRepository.queriesReceived to verify which queries were delegated.

    @Test
    fun `given query shorter than MIN_QUERY_LENGTH, when debounce elapses, then repository is never called`() =
        runTest {
            // Given — activate the flow pipeline and stabilize on initial ""
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            val shortQuery = "a"

            // When
            viewModel.onQueryChange(shortQuery)
            advanceUntilIdle() // debounce for short queries is 0L — fires immediately

            // Then — short query routes to PagingData.empty(), not to repository
            assertEquals(0, fakeRepository.queriesReceived.size)
        }

    @Test
    fun `given valid query, when debounce of 400ms expires, then repository receives the query`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            val query = "coldplay"

            // When
            viewModel.onQueryChange(query)
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Then
            assertEquals(listOf(query), fakeRepository.queriesReceived)
        }

    @Test
    fun `given two rapid changes within debounce window, when only last debounce expires, then repository called once with last query`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            val firstQuery = "co"
            val secondQuery = "coldplay"

            // When — emit first query, then second before debounce fires
            viewModel.onQueryChange(firstQuery)
            advanceTimeBy(200L) // first debounce not yet fired
            viewModel.onQueryChange(secondQuery)
            advanceTimeBy(400L) // second debounce fires; first was cancelled
            advanceUntilIdle()

            // Then
            assertEquals(listOf(secondQuery), fakeRepository.queriesReceived)
        }

    @Test
    fun `given query with leading and trailing whitespace, when debounce expires, then repository receives the trimmed query`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            val rawQuery = "  blur  "
            val trimmed = "blur"

            // When
            viewModel.onQueryChange(rawQuery)
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Then
            assertEquals(listOf(trimmed), fakeRepository.queriesReceived)
        }

    @Test
    fun `given same query emitted twice consecutively, when distinctUntilChanged applies, then repository called only once`() =
        runTest {
            // Given
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            val query = "muse"

            // When — emit first time
            viewModel.onQueryChange(query)
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Emit identical query again
            viewModel.onQueryChange(query)
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Then — distinctUntilChanged prevents second delegation
            assertEquals(1, fakeRepository.queriesReceived.size)
        }

    @Test
    fun `given a valid query then clear, when onClearQuery is called, then repository is not called again`() =
        runTest {
            // Given — debounce a valid query first
            backgroundScope.launch { viewModel.results.collect { } }
            advanceUntilIdle()
            viewModel.onQueryChange("oasis")
            advanceTimeBy(400L)
            advanceUntilIdle()
            val callsAfterFirstQuery = fakeRepository.queriesReceived.size

            // When
            viewModel.onClearQuery()
            advanceUntilIdle()

            // Then — clear routes to PagingData.empty(), not to repository
            assertEquals(callsAfterFirstQuery, fakeRepository.queriesReceived.size)
        }

    // ---- errorMessage tests ----

    @Test
    fun `given a DataException wrapping a network error, when errorMessage is called, then the wrapped error's UiText is returned`() {
        // Given
        val throwable = DataException(DataError.Network.NO_INTERNET)

        // When
        val uiText = viewModel.errorMessage(throwable)

        // Then
        assertEquals(R.string.no_internet, (uiText as UiText.StringResource).resId)
    }

    @Test
    fun `given a throwable that is not a DataException, when errorMessage is called, then the generic search error UiText is returned`() {
        // Given
        val throwable = RuntimeException("unexpected")

        // When
        val uiText = viewModel.errorMessage(throwable)

        // Then
        assertEquals(R.string.search_error_generic, (uiText as UiText.StringResource).resId)
    }
}
