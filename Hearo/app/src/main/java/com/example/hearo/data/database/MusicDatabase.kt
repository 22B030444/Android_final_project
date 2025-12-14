package com.example.hearo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hearo.data.database.dao.AlbumDao
import com.example.hearo.data.database.dao.ArtistDao
import com.example.hearo.data.database.dao.PlaylistDao
import com.example.hearo.data.database.dao.RecentlyPlayedDao
import com.example.hearo.data.database.dao.TrackDao
import com.example.hearo.data.database.entity.AlbumEntity
import com.example.hearo.data.database.entity.ArtistEntity
import com.example.hearo.data.database.entity.PlaylistEntity
import com.example.hearo.data.database.entity.PlaylistTrackEntity
import com.example.hearo.data.database.entity.RecentlyPlayedEntity
import com.example.hearo.data.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "hearo_music_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}