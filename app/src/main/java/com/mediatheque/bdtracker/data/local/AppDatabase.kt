package com.mediatheque.bdtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mediatheque.bdtracker.data.local.dao.SerieDao
import com.mediatheque.bdtracker.data.local.dao.TomeDao
import com.mediatheque.bdtracker.data.local.entity.SerieEntity
import com.mediatheque.bdtracker.data.local.entity.TomeEntity

/**
 * Base de données Room locale. Utilise le pattern Singleton pour n'avoir
 * qu'une seule instance ouverte pendant toute la vie de l'application.
 */
@Database(
    entities = [SerieEntity::class, TomeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serieDao(): SerieDao
    abstract fun tomeDao(): TomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bdtracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
