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
    val tabLayoutStyle: Flow<String> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("tab_layout_style", "Row") ?: "Row")
    val showSearchSuggestions: Flow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("show_search_suggestions", true))
    val blockThirdPartyCookies: Flow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("block_third_party_cookies", false))
    val autoSelectUrlOnClick: Flow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("auto_select_url_on_click", true))
    val enableJavaScript: Flow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("enable_javascript", true))

    fun setSearchEngine(engine: String) {
        prefs.edit().putString("search_engine", engine).apply()
        (searchEngine as kotlinx.coroutines.flow.MutableStateFlow).value = engine
    }

    fun setTheme(themeStr: String) {
        prefs.edit().putString("theme", themeStr).apply()
        (theme as kotlinx.coroutines.flow.MutableStateFlow).value = themeStr
    }

    fun setTabLayoutStyle(style: String) {
        prefs.edit().putString("tab_layout_style", style).apply()
        (tabLayoutStyle as kotlinx.coroutines.flow.MutableStateFlow).value = style
    }

    fun setShowSearchSuggestions(show: Boolean) {
        prefs.edit().putBoolean("show_search_suggestions", show).apply()
        (showSearchSuggestions as kotlinx.coroutines.flow.MutableStateFlow).value = show
    }

    fun setBlockThirdPartyCookies(block: Boolean) {
        prefs.edit().putBoolean("block_third_party_cookies", block).apply()
        (blockThirdPartyCookies as kotlinx.coroutines.flow.MutableStateFlow).value = block
    }

    fun setAutoSelectUrlOnClick(autoSelect: Boolean) {
        prefs.edit().putBoolean("auto_select_url_on_click", autoSelect).apply()
        (autoSelectUrlOnClick as kotlinx.coroutines.flow.MutableStateFlow).value = autoSelect
    }

    fun setEnableJavaScript(enable: Boolean) {
        prefs.edit().putBoolean("enable_javascript", enable).apply()
        (enableJavaScript as kotlinx.coroutines.flow.MutableStateFlow).value = enable
    }

    suspend fun createIdentity(name: String, colorHex: String, iconName: String = "Person", isIncognito: Boolean = false): Long {
        return browserDao.insertIdentity(BrowserIdentity(name = name, colorHex = colorHex, iconName = iconName, isIncognito = isIncognito))
    }

    suspend fun updateIdentity(identity: BrowserIdentity) {
        browserDao.updateIdentity(identity)
    }

    suspend fun deleteIdentity(identity: BrowserIdentity) {
        browserDao.clearTabsForIdentity(identity.id)
        browserDao.clearHistory(identity.id)
        browserDao.deleteIdentity(identity)
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

    suspend fun deleteBookmark(bookmark: Bookmark) {
        browserDao.deleteBookmark(bookmark)
    }

    suspend fun clearAllHistoryAndTabs() {
        browserDao.clearHistory()
        browserDao.clearTabs()
    }
}
