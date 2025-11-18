package com.example.hearo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hearo.data.database.dao.TrackDao
import com.example.hearo.data.database.entity.TrackEntity

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

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