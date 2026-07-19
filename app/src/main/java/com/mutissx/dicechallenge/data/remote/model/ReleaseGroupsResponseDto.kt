package com.mutissx.dicechallenge.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseGroupsResponseDto(
    @SerialName("release-group-count") val count: Int = 0,
    @SerialName("release-group-offset") val offset: Int = 0,
    @SerialName("release-groups") val releaseGroups: List<ReleaseGroupDto> = emptyList()
)
