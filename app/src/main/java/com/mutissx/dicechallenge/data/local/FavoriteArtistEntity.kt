package com.mutissx.dicechallenge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_artists")
data class FavoriteArtistEntity(
    @PrimaryKey val mbid: String,
    val name: String,
    val country: String?,
    val disambiguation: String?,
    val addedAt: Long
)
