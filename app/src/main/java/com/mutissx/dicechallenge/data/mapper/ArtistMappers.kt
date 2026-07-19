package com.mutissx.dicechallenge.data.mapper

import com.mutissx.dicechallenge.data.local.FavoriteArtistEntity
import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.domain.model.Artist

fun ArtistDto.toDomain(): Artist = Artist(
    mbid = id,
    name = name,
    country = country,
    disambiguation = disambiguation?.takeIf { it.isNotBlank() },
    score = score
)

fun Artist.toEntity(addedAt: Long): FavoriteArtistEntity = FavoriteArtistEntity(
    mbid = mbid,
    name = name,
    country = country,
    disambiguation = disambiguation,
    addedAt = addedAt
)

fun FavoriteArtistEntity.toDomain(): Artist = Artist(
    mbid = mbid,
    name = name,
    country = country,
    disambiguation = disambiguation?.takeIf { it.isNotBlank() },
    score = null
)
