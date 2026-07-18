package com.mutissx.dicechallenge.data.remote.api

import com.mutissx.dicechallenge.data.remote.model.ArtistSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicBrainzApi {

    @GET("artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("fmt") format: String = JSON_FMT
    ): ArtistSearchResponseDto

    companion object {
        const val JSON_FMT = "json"
    }
}