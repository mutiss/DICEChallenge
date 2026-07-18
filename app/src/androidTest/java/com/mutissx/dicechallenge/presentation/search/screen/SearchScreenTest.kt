package com.mutissx.dicechallenge.presentation.search.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import com.mutissx.dicechallenge.fake.FakeArtistPagingSource
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.search.viewmodel.SearchViewModel
import com.mutissx.dicechallenge.util.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeArtistRepository
    private lateinit var viewModel: SearchViewModel
    private var clickedMbid: String? = null

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        viewModel = SearchViewModel(SearchArtistsUseCase(fakeRepository))
        clickedMbid = null

        composeTestRule.setContent {
            SearchScreen(
                innerPadding = PaddingValues(),
                onArtistClick = { clickedMbid = it },
                viewModel = viewModel
            )
        }
    }

    private fun artist(mbid: String, name: String) = Artist(
        mbid = mbid,
        name = name,
        country = "GB",
        disambiguation = null,
        score = 100
    )

    @Test
    fun given_no_query_typed_when_screen_is_shown_then_start_message_is_displayed() {
        composeTestRule.onNodeWithTag(TestTags.EMPTY_VIEW_START).assertIsDisplayed()
    }

    @Test
    fun given_query_shorter_than_min_length_when_typed_then_start_message_is_still_displayed() {
        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("a")

        composeTestRule.onNodeWithTag(TestTags.EMPTY_VIEW_START).assertIsDisplayed()
    }

    @Test
    fun given_query_with_matching_artists_when_debounce_elapses_then_results_list_shows_artist_rows() {
        val artists = listOf(artist("1", "Radiohead"), artist("2", "Coldplay"))
        fakeRepository.pagingSourceFactory = { FakeArtistPagingSource(Result.success(artists)) }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("radio")
        composeTestRule.waitForTag(TestTags.SEARCH_RESULTS_LIST)

        composeTestRule.onAllNodesWithTag(TestTags.ARTIST_ROW).assertCountEquals(2)
        composeTestRule.onNodeWithText("Radiohead").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coldplay").assertIsDisplayed()
    }

    @Test
    fun given_query_with_no_matches_when_debounce_elapses_then_no_results_message_is_displayed() {
        fakeRepository.pagingSourceFactory = { FakeArtistPagingSource(Result.success(emptyList())) }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("zzz")
        composeTestRule.waitForTag(TestTags.EMPTY_VIEW_NO_RESULTS)

        composeTestRule.onNodeWithTag(TestTags.EMPTY_VIEW_NO_RESULTS).assertIsDisplayed()
    }

    @Test
    fun given_repository_error_when_debounce_elapses_then_error_view_with_retry_is_displayed() {
        fakeRepository.pagingSourceFactory = {
            FakeArtistPagingSource(Result.failure(RuntimeException("boom")))
        }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("fail")
        composeTestRule.waitForTag(TestTags.ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.ERROR_VIEW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun given_error_state_when_retry_is_clicked_then_results_list_is_displayed_after_successful_retry() {
        val artists = listOf(artist("1", "Muse"))
        val results = ArrayDeque(
            listOf(
                Result.failure<List<Artist>>(RuntimeException("boom")),
                Result.success(artists)
            )
        )
        fakeRepository.pagingSourceFactory = { FakeArtistPagingSource { results.removeFirst() } }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("muse")
        composeTestRule.waitForTag(TestTags.ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).performClick()
        composeTestRule.waitForTag(TestTags.SEARCH_RESULTS_LIST)

        composeTestRule.onNodeWithText("Muse").assertIsDisplayed()
    }

    @Test
    fun given_results_displayed_when_clear_button_is_clicked_then_screen_resets_to_start_state() {
        val artists = listOf(artist("1", "Oasis"))
        fakeRepository.pagingSourceFactory = { FakeArtistPagingSource(Result.success(artists)) }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("oasis")
        composeTestRule.waitForTag(TestTags.SEARCH_RESULTS_LIST)

        composeTestRule.onNodeWithTag(TestTags.SEARCH_CLEAR_BUTTON).performClick()
        composeTestRule.waitForTag(TestTags.EMPTY_VIEW_START)

        composeTestRule.onNodeWithTag(TestTags.EMPTY_VIEW_START).assertIsDisplayed()
    }

    @Test
    fun given_results_displayed_when_an_artist_row_is_clicked_then_onArtistClick_receives_the_artist_mbid() {
        val target = artist("abc-123", "Blur")
        fakeRepository.pagingSourceFactory = { FakeArtistPagingSource(Result.success(listOf(target))) }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("blur")
        composeTestRule.waitForTag(TestTags.SEARCH_RESULTS_LIST)

        composeTestRule.onNodeWithText("Blur").performClick()

        assertEquals(target.mbid, clickedMbid)
    }
}
