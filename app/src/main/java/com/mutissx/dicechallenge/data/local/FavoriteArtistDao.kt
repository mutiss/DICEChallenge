package com.mutissx.dicechallenge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteArtistDao {

    @Query("SELECT * FROM favorite_artists ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteArtistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_artists WHERE mbid = :mbid)")
    fun observeIsFavorite(mbid: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteArtistEntity)

    @Query("DELETE FROM favorite_artists WHERE mbid = :mbid")
    suspend fun delete(mbid: String)
}
