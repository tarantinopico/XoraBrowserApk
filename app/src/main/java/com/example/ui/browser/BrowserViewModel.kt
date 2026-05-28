package com.example.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.model.BrowserIdentity
import com.example.model.Tab
import com.example.repository.BrowserRepository
import com.example.BrowserApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

class BrowserViewModel(
    private val repository: BrowserRepository
) : ViewModel() {

    val identities = repository.identities.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeIdentity = repository.activeIdentity.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tabs = activeIdentity.filterNotNull().flatMapLatest { identity ->
        repository.getTabsForIdentity(identity.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTab = activeIdentity.filterNotNull().flatMapLatest { identity ->
        repository.getActiveTabForIdentity(identity.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val searchEngine = repository.searchEngine.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Google")
    val theme = repository.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Auto")

    fun setSearchEngine(engine: String) {
        repository.setSearchEngine(engine)
    }

    fun setTheme(themeStr: String) {
        repository.setTheme(themeStr)
    }

    private val _isAddressBarFocused = MutableStateFlow(false)
    val isAddressBarFocused: StateFlow<Boolean> = _isAddressBarFocused.asStateFlow()

    init {
        // Initialize default identity if none exists
        viewModelScope.launch {
            repository.identities.firstOrNull()?.let { identities ->
                if (identities.isEmpty()) {
                    val id = repository.createIdentity("Work", "#1E88E5")
                    repository.setActiveIdentity(id)
                    val personalId = repository.createIdentity("Personal", "#43A047")
                    
                    // add default tabs
                    val tabId1 = repository.addTab(id, "Google", "https://www.google.com", 0, isActive = true)
                    val tabId2 = repository.addTab(personalId, "Google", "https://www.google.com", 0, isActive = true)
                }
            }
        }
    }

    fun createIdentity(name: String, colorHex: String = "#FFFFFF", iconName: String = "Person", isIncognito: Boolean = false) {
        viewModelScope.launch {
            val id = repository.createIdentity(name, colorHex, iconName, isIncognito)
            repository.setActiveIdentity(id)
            repository.addTab(id, "New Tab", "https://www.google.com", 0, isActive = true)
        }
    }

    fun switchIdentity(identityId: Long) {
        viewModelScope.launch {
            repository.setActiveIdentity(identityId)
        }
    }

    fun onUrlSubmitted(url: String) {
        var finalUrl = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "https://$url"
        }
        _isAddressBarFocused.value = false
        
        val tab = activeTab.value
        val identity = activeIdentity.value
        val isIncognito = identity?.isIncognito ?: false
        
        if (tab != null) {
            viewModelScope.launch {
                repository.updateTab(tab.copy(url = finalUrl, title = finalUrl))
                if (!isIncognito) repository.addHistory(tab.identityId, finalUrl, finalUrl)
            }
        } else {
            if (identity != null) {
                viewModelScope.launch {
                    repository.addTab(identity.id, finalUrl, finalUrl, tabs.value.size, isActive = true)
                    if (!isIncognito) repository.addHistory(identity.id, finalUrl, finalUrl)
                }
            }
        }
    }

    fun setAddressBarFocused(focused: Boolean) {
        _isAddressBarFocused.value = focused
    }

    fun onPageStarted(url: String) {
        val tab = activeTab.value
        if (tab != null && tab.url != url) {
            viewModelScope.launch {
                repository.updateTab(tab.copy(url = url, title = url))
            }
        }
    }

    fun createNewTab() {
        val identity = activeIdentity.value ?: return
        viewModelScope.launch {
            repository.addTab(identity.id, "New Tab", "https://www.google.com", tabs.value.size, isActive = true)
        }
    }

    fun switchTab(tabId: Long) {
        val identity = activeIdentity.value ?: return
        viewModelScope.launch {
            repository.setActiveTab(identity.id, tabId = tabId)
        }
    }

    fun closeTab(tab: Tab) {
        viewModelScope.launch {
            repository.deleteTab(tab)
            if (tab.isActive && tabs.value.isNotEmpty()) {
                val nextTab = tabs.value.firstOrNull { it.id != tab.id }
                if (nextTab != null) {
                    repository.setActiveTab(tab.identityId, nextTab.id)
                }
            }
        }
    }

    fun clearBrowsingData() {
        viewModelScope.launch {
            repository.clearAllHistoryAndTabs()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BrowserApplication)
                val repository = application.container.repository
                BrowserViewModel(repository)
            }
        }
    }
}
