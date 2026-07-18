package com.mutissx.dicechallenge.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseGroupDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("first-release-date") val firstReleaseDate: String? = null,
    @SerialName("primary-type") val primaryType: String? = null,
    @SerialName("secondary-types") val secondaryTypes: List<String>? = null
)
