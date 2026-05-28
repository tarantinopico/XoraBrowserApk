package com.example.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BrowserIdentity::class, Tab::class, History::class, Bookmark::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao
}
