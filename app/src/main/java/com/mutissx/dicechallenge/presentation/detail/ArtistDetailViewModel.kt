package com.mutissx.dicechallenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.domain.usecase.GetArtistDetailUseCase
import com.mutissx.dicechallenge.domain.usecase.GetArtistReleaseGroupsUseCase
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.core.ui.extensions.asUiText
import com.mutissx.dicechallenge.presentation.navigation.Destination
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mutissx.dicechallenge.core.domain.Result

class ArtistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getArtistDetailUseCase: GetArtistDetailUseCase,
    private val getReleaseGroupsUseCase: GetArtistReleaseGroupsUseCase,
) : ViewModel() {

    private val mbid: String = requireNotNull(savedStateHandle[Destination.ArtistDetail.ARG_MBID]) {
        "mbid missing from arguments"
    }

    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        loadInfo()
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
                        releaseGroups = releasesResult.data
                    )

                else -> ArtistDetailUiState.Error(UiText.StringResource(R.string.unknown_error))
            }
        }
    }
}
