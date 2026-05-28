package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.model.AppDatabase
import com.example.repository.BrowserRepository

interface AppContainer {
    val repository: BrowserRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "xora_browser_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    
    override val repository: BrowserRepository by lazy {
        BrowserRepository(database.browserDao())
    }
}

class BrowserApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
