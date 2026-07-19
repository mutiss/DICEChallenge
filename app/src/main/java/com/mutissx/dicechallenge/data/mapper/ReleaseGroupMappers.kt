package com.mutissx.dicechallenge.data.mapper

import com.mutissx.dicechallenge.data.remote.model.ReleaseGroupDto
import com.mutissx.dicechallenge.domain.model.ReleaseGroup

private const val COVER_ART_BASE = "https://coverartarchive.org/release-group"

fun ReleaseGroupDto.toDomain(): ReleaseGroup = ReleaseGroup(
    mbid = id,
    title = title,
    firstReleaseYear = firstReleaseDate?.take(4)?.takeIf { it.length == 4 },
    primaryType = primaryType,
    coverArtUrl = "$COVER_ART_BASE/$id/front-250"
)
