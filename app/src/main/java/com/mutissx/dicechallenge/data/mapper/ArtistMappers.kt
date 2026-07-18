package com.mutissx.dicechallenge.data.mapper

import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.domain.model.Artist

fun ArtistDto.toDomain(): Artist = Artist(
    mbid = id,
    name = name,
    country = country,
    disambiguation = disambiguation?.takeIf { it.isNotBlank() },
    score = score
)
