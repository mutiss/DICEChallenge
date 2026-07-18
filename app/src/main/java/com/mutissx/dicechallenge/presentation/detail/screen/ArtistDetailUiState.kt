package com.mutissx.dicechallenge.presentation.detail.screen

import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.model.ReleaseGroup
import com.mutissx.dicechallenge.core.ui.UiText

sealed interface ArtistDetailUiState {
    data object Loading : ArtistDetailUiState
    data class Error(val message: UiText) : ArtistDetailUiState
    data class Content(
        val artist: Artist,
        val isFavorite: Boolean,
        val releaseGroups: ReleaseGroupsState
    ) : ArtistDetailUiState
}

sealed interface ReleaseGroupsState {
    data object Loading : ReleaseGroupsState
    data class Loaded(val items: List<ReleaseGroup>) : ReleaseGroupsState
    data class Error(val message: UiText) : ReleaseGroupsState
}
