package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppDatabase
import com.example.model.BrowserDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BrowserDao
    private lateinit var repository: BrowserRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.browserDao()
        repository = BrowserRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetIdentity() = runBlocking {
        val id = repository.createIdentity("Work", "#1E88E5", isIncognito = false)
        repository.setActiveIdentity(id)

        val active = repository.activeIdentity.first()
        assertTrue(active != null)
        assertEquals("Work", active?.name)
    }

    @Test
    fun createIncognitoIdentity() = runBlocking {
        val id = repository.createIdentity("Secret", "#000000", isIncognito = true)
        repository.setActiveIdentity(id)
        
        val active = repository.activeIdentity.first()
        assertTrue(active?.isIncognito == true)
    }
}
