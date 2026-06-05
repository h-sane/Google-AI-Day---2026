package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ManhwaEntity::class,
        PanelEntity::class,
        OnomatopoeiaEntity::class,
        VisualFxEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun manhwaDao(): ManhwaDao
    abstract fun panelDao(): PanelDao
    abstract fun onomatopoeiaDao(): OnomatopoeiaDao
    abstract fun visualFxDao(): VisualFxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manhwa_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
