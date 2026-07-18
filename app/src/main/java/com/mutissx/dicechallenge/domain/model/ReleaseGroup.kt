package com.mutissx.dicechallenge.domain.model

data class ReleaseGroup(
    val mbid: String,
    val title: String,
    val firstReleaseYear: String?,
    val primaryType: String?,
    val coverArtUrl: String
)
