package com.mutissx.dicechallenge.presentation.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.Result
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.core.ui.extensions.asUiText
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.domain.usecase.IsFavoriteUseCase
import com.mutissx.dicechallenge.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.dicechallenge.presentation.detail.screen.ArtistDetailUiState
import com.mutissx.dicechallenge.presentation.navigation.Destination
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ArtistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getArtistDetailUseCase: GetArtistDetailUseCase,
    private val getReleaseGroupsUseCase: GetArtistReleaseGroupsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase
    ) : ViewModel() {

    private val mbid: String = requireNotNull(savedStateHandle[Destination.ArtistDetail.ARG_MBID]) {
        "mbid missing from arguments"
    }

    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    private val _favoriteMessages = Channel<UiText>(Channel.BUFFERED)
    val favoriteMessages: Flow<UiText> = _favoriteMessages.receiveAsFlow()

    init {
        loadInfo()
        observeFavoriteStatus()
    }

    fun loadInfo() {
        _uiState.value = ArtistDetailUiState.Loading
        viewModelScope.launch {
            val (artistResult, releasesResult) = coroutineScope {
                val a = async { getArtistDetailUseCase(mbid) }
                val r = async { getReleaseGroupsUseCase(mbid) }
                a.await() to r.await()
            }

            _uiState.value = when {
                artistResult is Result.Error -> ArtistDetailUiState.Error(
                    artistResult.error.asUiText()
                )

                releasesResult is Result.Error -> ArtistDetailUiState.Error(
                    releasesResult.error.asUiText()
                )

                artistResult is Result.Success && releasesResult is Result.Success ->
                    ArtistDetailUiState.Content(
                        artist = artistResult.data,
                        releaseGroups = releasesResult.data,
                        isFavorite = isFavoriteUseCase(mbid).first()
                    )

                else -> ArtistDetailUiState.Error(UiText.StringResource(R.string.unknown_error))
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            isFavoriteUseCase(mbid).collect { isFavorite ->
                val current = _uiState.value
                if (current is ArtistDetailUiState.Content && current.isFavorite != isFavorite) {
                    _uiState.value = current.copy(isFavorite = isFavorite)
                }
            }
        }
    }

    fun onFavoriteToggle() {
        val current = _uiState.value as? ArtistDetailUiState.Content ?: return
        viewModelScope.launch {
            toggleFavorite(current.artist, current.isFavorite)
            val messageRes = if (current.isFavorite) R.string.favorite_removed else R.string.favorite_added
            _favoriteMessages.send(UiText.StringResource(messageRes, current.artist.name))
        }
    }
}
