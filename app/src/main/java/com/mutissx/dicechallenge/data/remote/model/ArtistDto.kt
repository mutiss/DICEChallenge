package com.mutissx.dicechallenge.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("country") val country: String? = null,
    @SerialName("disambiguation") val disambiguation: String? = null,
    @SerialName("score") val score: Int? = null
)
