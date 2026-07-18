package com.mutissx.dicechallenge.presentation.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.core.ui.extensions.asUiText
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.domain.usecase.IsFavoriteUseCase
import com.mutissx.dicechallenge.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.dicechallenge.presentation.detail.screen.ArtistDetailUiState
import com.mutissx.dicechallenge.presentation.detail.screen.ReleaseGroupsState
import com.mutissx.dicechallenge.presentation.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArtistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getArtistDetailUseCase: GetArtistDetailUseCase,
    private val getReleaseGroupsUseCase: GetArtistReleaseGroupsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    isFavoriteUseCase: IsFavoriteUseCase
    ) : ViewModel() {

    private val mbid: String = requireNotNull(savedStateHandle[Destination.ArtistDetail.ARG_MBID]) {
        "mbid missing from arguments"
    }

    private val _artistState = MutableStateFlow<ArtistSectionState>(ArtistSectionState.Loading)
    private val _releaseGroupsState = MutableStateFlow<ReleaseGroupsState>(ReleaseGroupsState.Loading)

    // artist and release groups are fetched independently, so the combined state can't be lost
    // regardless of which of the two (or the favorite flow) resolves/emits first.
    val uiState: StateFlow<ArtistDetailUiState> =
        combine(_artistState, _releaseGroupsState, isFavoriteUseCase(mbid)) { artist, releaseGroups, isFavorite ->
            when (artist) {
                ArtistSectionState.Loading -> ArtistDetailUiState.Loading
                is ArtistSectionState.Error -> ArtistDetailUiState.Error(artist.message)
                is ArtistSectionState.Loaded -> ArtistDetailUiState.Content(
                    artist = artist.data,
                    isFavorite = isFavorite,
                    releaseGroups = releaseGroups
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ArtistDetailUiState.Loading)

    private val _favoriteMessages = Channel<UiText>(Channel.BUFFERED)
    val favoriteMessages: Flow<UiText> = _favoriteMessages.receiveAsFlow()

    init {
        loadInfo()
    }

    fun loadInfo() {
        loadArtist()
        loadReleaseGroups()
    }

    fun retryReleaseGroups() = loadReleaseGroups()

    private fun loadArtist() {
        _artistState.value = ArtistSectionState.Loading
        viewModelScope.launch {
            _artistState.value = when (val result = getArtistDetailUseCase(mbid)) {
                is Result.Error -> ArtistSectionState.Error(result.error.asUiText())
                is Result.Success -> ArtistSectionState.Loaded(result.data)
            }
        }
    }

    private fun loadReleaseGroups() {
        _releaseGroupsState.value = ReleaseGroupsState.Loading
        viewModelScope.launch {
            _releaseGroupsState.value = when (val result = getReleaseGroupsUseCase(mbid)) {
                is Result.Error -> ReleaseGroupsState.Error(result.error.asUiText())
                is Result.Success -> ReleaseGroupsState.Loaded(result.data)
            }
        }
    }

    fun onFavoriteToggle() {
        val current = uiState.value as? ArtistDetailUiState.Content ?: return
        viewModelScope.launch {
            val result = toggleFavorite(current.artist, current.isFavorite)
            val message = if (result is Result.Error) {
                result.error.asUiText()
            } else {
                val messageRes = if (current.isFavorite) R.string.favorite_removed else R.string.favorite_added
                UiText.StringResource(messageRes, current.artist.name)
            }
            _favoriteMessages.send(message)
        }
    }

    private sealed interface ArtistSectionState {
        data object Loading : ArtistSectionState
        data class Error(val message: UiText) : ArtistSectionState
        data class Loaded(val data: Artist) : ArtistSectionState
    }
}
