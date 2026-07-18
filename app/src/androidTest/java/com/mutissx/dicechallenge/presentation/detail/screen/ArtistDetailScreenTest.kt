package com.mutissx.dicechallenge.presentation.detail.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.dicechallenge.core.domain.DataError
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.fake.FakeArtistRepository
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import com.mutissx.dicechallenge.presentation.navigation.Destination
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mbid = "artist-123"
    private lateinit var fakeRepository: FakeArtistRepository
    private var backClicked = false

    @Before
    fun setUp() {
        fakeRepository = FakeArtistRepository()
        backClicked = false
    }

    private fun artist(name: String = "Radiohead") =
        Artist(mbid = mbid, name = name, country = "GB", disambiguation = null, score = 100)

    private fun releaseGroup(id: String, title: String) = ReleaseGroup(
        mbid = id,
        title = title,
        firstReleaseYear = "1997",
        primaryType = "Album",
        coverArtUrl = ""
    )

    private fun setContent() {
        val viewModel = ArtistDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Destination.ArtistDetail.ARG_MBID to mbid)),
            getArtistDetailUseCase = GetArtistDetailUseCase(fakeRepository),
            getReleaseGroupsUseCase = GetArtistReleaseGroupsUseCase(fakeRepository)
        )
        composeTestRule.setContent {
            ArtistDetailScreen(onBack = { backClicked = true }, viewModel = viewModel)
        }
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 5_000L) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun given_successful_fetch_with_albums_when_screen_is_shown_then_artist_name_and_albums_are_displayed() {
        fakeRepository.artistResult = Result.Success(artist())
        fakeRepository.releaseGroupsResult = Result.Success(
            listOf(releaseGroup("rg-1", "OK Computer"), releaseGroup("rg-2", "In Rainbows"))
        )

        setContent()
        waitForTag(TestTags.DETAIL_CONTENT_LIST)

        composeTestRule.onNodeWithText("Radiohead").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(TestTags.DETAIL_ALBUM_ROW).assertCountEquals(2)
        composeTestRule.onNodeWithText("OK Computer").assertIsDisplayed()
        composeTestRule.onNodeWithText("In Rainbows").assertIsDisplayed()
    }

    @Test
    fun given_successful_fetch_with_no_albums_when_screen_is_shown_then_empty_albums_message_is_displayed() {
        fakeRepository.artistResult = Result.Success(artist())
        fakeRepository.releaseGroupsResult = Result.Success(emptyList())

        setContent()
        waitForTag(TestTags.DETAIL_EMPTY_ALBUMS)

        composeTestRule.onNodeWithTag(TestTags.DETAIL_EMPTY_ALBUMS).assertIsDisplayed()
    }

    @Test
    fun given_artist_fetch_fails_when_screen_is_shown_then_error_view_with_retry_is_displayed() {
        fakeRepository.artistResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        fakeRepository.releaseGroupsResult = Result.Success(emptyList())

        setContent()
        waitForTag(TestTags.DETAIL_ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.DETAIL_ERROR_VIEW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun given_error_state_when_retry_is_clicked_then_content_is_displayed_after_successful_retry() {
        fakeRepository.artistResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        fakeRepository.releaseGroupsResult = Result.Success(emptyList())

        setContent()
        waitForTag(TestTags.DETAIL_ERROR_VIEW)

        fakeRepository.artistResult = Result.Success(artist())
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).performClick()
        waitForTag(TestTags.DETAIL_EMPTY_ALBUMS)

        composeTestRule.onNodeWithText("Radiohead").assertIsDisplayed()
    }

    @Test
    fun given_content_displayed_when_back_button_is_clicked_then_onBack_is_invoked() {
        fakeRepository.artistResult = Result.Success(artist())
        fakeRepository.releaseGroupsResult = Result.Success(emptyList())

        setContent()
        waitForTag(TestTags.DETAIL_EMPTY_ALBUMS)

        composeTestRule.onNodeWithTag(TestTags.DETAIL_BACK_BUTTON).performClick()

        Assert.assertTrue(backClicked)
    }
}