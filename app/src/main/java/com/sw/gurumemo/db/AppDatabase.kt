package com.sw.gurumemo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [BookmarkEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getBookmarkDao(): BookmarkDao

    companion object {
        private const val databaseName = "db_bookmark"
        private var appDatabase: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase =
                    Room.databaseBuilder(context, AppDatabase::class.java, databaseName).fallbackToDestructiveMigration().build()
            }
            return appDatabase as AppDatabase
        }
    }
}