package com.mutissx.dicechallenge.data.remote.api

import com.mutissx.dicechallenge.data.remote.model.ArtistDto
import com.mutissx.dicechallenge.data.remote.model.ArtistSearchResponseDto
import com.mutissx.dicechallenge.data.remote.model.ReleaseGroupsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicBrainzApi {

    @GET("artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("fmt") format: String = JSON_FMT
    ): ArtistSearchResponseDto

    @GET("artist/{mbid}")
    suspend fun getArtist(
        @Path("mbid") mbid: String,
        @Query("fmt") format: String = JSON_FMT
    ): ArtistDto

    @GET("release-group")
    suspend fun getReleaseGroups(
        @Query("artist") artistMbid: String,
        @Query("type") type: String = "album",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fmt") format: String = JSON_FMT
    ): ReleaseGroupsResponseDto

    companion object {
        const val JSON_FMT = "json"
    }
}