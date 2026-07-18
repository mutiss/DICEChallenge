package com.mutissx.dicechallenge.presentation.favorites.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.dicechallenge.fake.FakeFavoritesRepository
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.dicechallenge.util.waitForTag
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeFavoritesRepository
    private var clickedMbid: String? = null

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        clickedMbid = null
    }

    private fun artist(id: String, name: String) =
        Artist(mbid = id, name = name, country = "GB", disambiguation = null, score = null)

    private fun setContent() {
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(fakeRepository))
        composeTestRule.setContent {
            FavoritesScreen(
                innerPadding = PaddingValues(),
                onArtistClick = { clickedMbid = it },
                viewModel = viewModel
            )
        }
    }

    @Test
    fun given_no_favorites_when_screen_is_shown_then_empty_view_is_displayed() {
        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_EMPTY_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_EMPTY_VIEW).assertIsDisplayed()
    }

    @Test
    fun given_favorites_exist_when_screen_is_shown_then_the_list_displays_them() {
        runBlocking {
            fakeRepository.add(artist("id-1", "Radiohead"))
            fakeRepository.add(artist("id-2", "Coldplay"))
        }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_LIST)

        composeTestRule.onAllNodesWithTag(TestTags.FAVORITES_ARTIST_ROW).assertCountEquals(2)
        composeTestRule.onNodeWithText("Radiohead").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coldplay").assertIsDisplayed()
    }

    @Test
    fun given_favorites_exist_when_a_row_is_clicked_then_onArtistClick_receives_the_mbid() {
        runBlocking { fakeRepository.add(artist("id-1", "Radiohead")) }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_LIST)

        composeTestRule.onNodeWithText("Radiohead").performClick()

        assertEquals("id-1", clickedMbid)
    }

    @Test
    fun given_the_screen_is_open_when_an_artist_is_added_to_favorites_then_the_list_updates() {
        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_EMPTY_VIEW)

        runBlocking { fakeRepository.add(artist("id-1", "Radiohead")) }
        composeTestRule.waitForTag(TestTags.FAVORITES_LIST)

        composeTestRule.onNodeWithText("Radiohead").assertIsDisplayed()
    }

    @Test
    fun given_the_screen_is_open_when_an_artist_is_removed_from_favorites_then_the_list_becomes_empty() {
        runBlocking { fakeRepository.add(artist("id-1", "Radiohead")) }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_LIST)

        runBlocking { fakeRepository.remove("id-1") }
        composeTestRule.waitForTag(TestTags.FAVORITES_EMPTY_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_EMPTY_VIEW).assertIsDisplayed()
    }

    @Test
    fun given_the_repository_read_fails_when_screen_is_shown_then_error_view_is_displayed() {
        fakeRepository.readError = RuntimeException("disk read failure")

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_ERROR_VIEW).assertIsDisplayed()
    }
}
