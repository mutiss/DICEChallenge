package com.mutissx.dicechallenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteArtistEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteArtistDao(): FavoriteArtistDao

    companion object {
        const val DB_NAME = "dicechallenge.db"
    }
}
