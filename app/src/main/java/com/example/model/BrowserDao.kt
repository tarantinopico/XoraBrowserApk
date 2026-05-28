package com.example.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {
    // Identity
    @Query("SELECT * FROM browser_identities")
    fun getAllIdentities(): Flow<List<BrowserIdentity>>

    @Query("SELECT * FROM browser_identities WHERE isActive = 1 LIMIT 1")
    fun getActiveIdentity(): Flow<BrowserIdentity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentity(identity: BrowserIdentity): Long

    @Update
    suspend fun updateIdentity(identity: BrowserIdentity)

    @Query("UPDATE browser_identities SET isActive = (id = :identityId)")
    suspend fun setActiveIdentity(identityId: Long)

    @Delete
    suspend fun deleteIdentity(identity: BrowserIdentity)

    // Tabs
    @Query("SELECT * FROM tabs WHERE identityId = :identityId ORDER BY orderIndex ASC")
    fun getTabsForIdentity(identityId: Long): Flow<List<Tab>>

    @Query("SELECT * FROM tabs WHERE identityId = :identityId AND isActive = 1 LIMIT 1")
    fun getActiveTabForIdentity(identityId: Long): Flow<Tab?>

    @Query("UPDATE tabs SET isActive = (id = :tabId) WHERE identityId = :identityId")
    suspend fun setActiveTab(identityId: Long, tabId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: Tab): Long

    @Update
    suspend fun updateTab(tab: Tab)
    
    @Delete
    suspend fun deleteTab(tab: Tab)
    
    @Query("DELETE FROM tabs WHERE identityId = :identityId")
    suspend fun clearTabsForIdentity(identityId: Long)

    // History
    @Query("SELECT * FROM histories WHERE identityId = :identityId ORDER BY timestamp DESC")
    fun getHistoryForIdentity(identityId: Long): Flow<List<History>>

    @Insert
    suspend fun insertHistory(history: History)

    @Query("DELETE FROM histories WHERE identityId = :identityId")
    suspend fun clearHistory(identityId: Long)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE identityId = :identityId")
    fun getBookmarksForIdentity(identityId: Long): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM histories")
    suspend fun clearHistory()

    @Query("DELETE FROM tabs")
    suspend fun clearTabs()
}
