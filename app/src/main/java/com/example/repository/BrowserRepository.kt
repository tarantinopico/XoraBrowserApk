package com.example.repository

import com.example.model.BrowserDao
import com.example.model.BrowserIdentity
import com.example.model.Tab
import com.example.model.History
import com.example.model.Bookmark
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val browserDao: BrowserDao,
    private val context: android.content.Context
) {
    private val prefs = context.getSharedPreferences("browser_prefs", android.content.Context.MODE_PRIVATE)

    val identities: Flow<List<BrowserIdentity>> = browserDao.getAllIdentities()
    val activeIdentity: Flow<BrowserIdentity?> = browserDao.getActiveIdentity()

    val searchEngine: Flow<String> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("search_engine", "Google") ?: "Google")
    val theme: Flow<String> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("theme", "Auto") ?: "Auto")

    fun setSearchEngine(engine: String) {
        prefs.edit().putString("search_engine", engine).apply()
        (searchEngine as kotlinx.coroutines.flow.MutableStateFlow).value = engine
    }

    fun setTheme(themeStr: String) {
        prefs.edit().putString("theme", themeStr).apply()
        (theme as kotlinx.coroutines.flow.MutableStateFlow).value = themeStr
    }

    suspend fun createIdentity(name: String, colorHex: String, iconName: String = "Person", isIncognito: Boolean = false): Long {
        return browserDao.insertIdentity(BrowserIdentity(name = name, colorHex = colorHex, iconName = iconName, isIncognito = isIncognito))
    }

    suspend fun setActiveIdentity(id: Long) {
        browserDao.setActiveIdentity(id)
    }

    fun getTabsForIdentity(identityId: Long): Flow<List<Tab>> {
        return browserDao.getTabsForIdentity(identityId)
    }

    fun getActiveTabForIdentity(identityId: Long): Flow<Tab?> {
        return browserDao.getActiveTabForIdentity(identityId)
    }

    suspend fun setActiveTab(identityId: Long, tabId: Long) {
        browserDao.setActiveTab(identityId = identityId, tabId = tabId)
    }

    suspend fun addTab(identityId: Long, title: String, url: String, orderIndex: Int, isActive: Boolean = false): Long {
        val tab = Tab(identityId = identityId, title = title, url = url, orderIndex = orderIndex, isActive = isActive)
        return browserDao.insertTab(tab)
    }
    
    suspend fun updateTab(tab: Tab) {
        browserDao.updateTab(tab)
    }

    suspend fun deleteTab(tab: Tab) {
        browserDao.deleteTab(tab)
    }

    fun getHistory(identityId: Long): Flow<List<History>> {
        return browserDao.getHistoryForIdentity(identityId)
    }

    suspend fun addHistory(identityId: Long, title: String, url: String) {
        val history = History(identityId = identityId, title = title, url = url, timestamp = System.currentTimeMillis())
        browserDao.insertHistory(history)
    }
    
    fun getBookmarks(identityId: Long): Flow<List<Bookmark>> {
        return browserDao.getBookmarksForIdentity(identityId)
    }

    suspend fun addBookmark(identityId: Long, title: String, url: String) {
        val bookmark = Bookmark(identityId = identityId, title = title, url = url)
        browserDao.insertBookmark(bookmark)
    }

    suspend fun clearAllHistoryAndTabs() {
        browserDao.clearHistory()
        browserDao.clearTabs()
    }
}
