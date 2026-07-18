package com.mutissx.dicechallenge.domain.model

data class Artist(
    val mbid: String,
    val name: String,
    val country: String?,
    val disambiguation: String?,
    val score: Int?
)
