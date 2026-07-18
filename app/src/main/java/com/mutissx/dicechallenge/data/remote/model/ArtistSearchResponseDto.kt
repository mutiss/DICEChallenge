package com.mutissx.dicechallenge.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistSearchResponseDto(
    @SerialName("count") val count: Int = 0,
    @SerialName("offset") val offset: Int = 0,
    @SerialName("artists") val artists: List<ArtistDto> = emptyList()
)
