package com.example.ui.browser

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import com.example.ui.theme.Spacing
import com.example.ui.theme.Radius
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(modifier: Modifier = Modifier, viewModel: BrowserViewModel = viewModel(factory = BrowserViewModel.Factory)) {
    val tabs by viewModel.tabs.collectAsState()

    val activeTab by viewModel.activeTab.collectAsState()
    val isAddressBarFocused by viewModel.isAddressBarFocused.collectAsState()
    val activeIdentity by viewModel.activeIdentity.collectAsState()
    val identities by viewModel.identities.collectAsState()

    val searchEngine by viewModel.searchEngine.collectAsState()
    val theme by viewModel.theme.collectAsState()

    var webView by remember { mutableStateOf<WebView?>(null) }
    val focusManager = LocalFocusManager.current

    var showSettings by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showCreateIdentity by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    if (showCreateIdentity) {
        CreateIdentityDialog(
            onDismiss = { showCreateIdentity = false },
            onCreate = { name, color, icon, isIncognito ->
                viewModel.createIdentity(name, color, icon, isIncognito)
                showCreateIdentity = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            searchEngine = searchEngine,
            theme = theme,
            onSearchEngineChange = { viewModel.setSearchEngine(it) },
            onThemeChange = { viewModel.setTheme(it) },
            onDismiss = { showSettings = false },
            onClearData = { viewModel.clearBrowsingData() }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        
        // ------------- Main Content (Top Chrome + WebView) -------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (activeIdentity?.isIncognito == true) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.background)
                .blur(if (isMenuExpanded) 16.dp else 0.dp)
        ) {
            // Address Bar
            BrowserAddressBar(
                url = activeTab?.url ?: "",
                isFocused = isAddressBarFocused,
                isIncognito = activeIdentity?.isIncognito ?: false,
                onFocusChanged = { viewModel.setAddressBarFocused(it) },
                onUrlSubmitted = { 
                    viewModel.onUrlSubmitted(it)
                    focusManager.clearFocus()
                },
                onRefresh = { webView?.reload() },
                onNewTab = { viewModel.createNewTab() },
                onNewIncognito = { viewModel.createIdentity("Incognito", isIncognito = true) },
                onOpenSettings = { showSettings = true },
                onOpenMenu = { isMenuExpanded = !isMenuExpanded },
                onBack = { if (webView?.canGoBack() == true) webView?.goBack() }
            )

            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }

            // Web View Area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (activeTab != null) {
                    if (activeTab!!.url == "xora://newtab" || activeTab!!.url == "about:blank") {
                        // Premium New Tab Page
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .padding(Spacing.Large),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val activeColor = activeIdentity?.colorHex?.let { kotlin.runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: MaterialTheme.colorScheme.primary
                            val vector = availableIcons[activeIdentity?.iconName] ?: Icons.Default.Person
                            
                            Icon(
                                imageVector = if (activeIdentity?.isIncognito == true) Icons.Default.Lock else vector,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(activeColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                                    .padding(20.dp)
                            )
                            Spacer(modifier = Modifier.height(Spacing.Large))
                            Text(
                                "Welcome to Xora",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(Spacing.Medium))
                            Text(
                                "Start typing in the address bar to search or navigate.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        androidx.compose.runtime.key(activeTab!!.id) {
                            var lastLoadedUrl by remember { mutableStateOf<String?>(null) }
                            
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webChromeClient = object : android.webkit.WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                super.onProgressChanged(view, newProgress)
                                                loadProgress = newProgress / 100f
                                                isLoading = newProgress < 100
                                            }
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                url?.let { viewModel.onPageStarted(it) }
                                                isLoading = true
                                                loadProgress = 0f
                                            }
                                            
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isLoading = false
                                                loadProgress = 1f
                                            }
                                            
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                if (request?.url?.toString()?.startsWith("http") == true) {
                                                    return false
                                                }
                                                return true
                                            }
                                        }
                                        
                                        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                            val request = android.app.DownloadManager.Request(android.net.Uri.parse(url)).apply {
                                                setMimeType(mimetype)
                                                addRequestHeader("User-Agent", userAgent)
                                                setDescription("Downloading file...")
                                                setTitle(android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype))
                                                setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype))
                                            }
                                            val dm = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                                            dm.enqueue(request)
                                            android.widget.Toast.makeText(context, "Download started", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        
                                        webView = this
                                        lastLoadedUrl = activeTab!!.url
                                        loadUrl(activeTab!!.url)
                                    }
                                },
                                update = { view ->
                                     val targetedUrl = activeTab?.url
                                     if (targetedUrl != null && lastLoadedUrl != targetedUrl && !isAddressBarFocused && targetedUrl != "xora://newtab" && targetedUrl != "about:blank") {
                                         lastLoadedUrl = targetedUrl
                                         view.loadUrl(targetedUrl)
                                     }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        
        // ------------- Menu Overlay -------------
        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { isMenuExpanded = false }
            )
        }

        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            MenuAndTabsPanel(
                identities = identities,
                activeIdentity = activeIdentity,
                tabs = tabs,
                activeTab = activeTab,
                onClose = { isMenuExpanded = false },
                onSwitchIdentity = { viewModel.switchIdentity(it) },
                onAddIdentity = { 
                    showCreateIdentity = true
                    isMenuExpanded = false 
                },
                onNewTab = { 
                    viewModel.createNewTab()
                    isMenuExpanded = false
                },
                onBookmarks = { },
                onHistory = { },
                onSettings = {
                    showSettings = true
                    isMenuExpanded = false
                },
                onSwitchTab = { 
                    viewModel.switchTab(it)
                    isMenuExpanded = false
                },
                onCloseTab = { viewModel.closeTab(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserAddressBar(
    url: String,
    isFocused: Boolean,
    isIncognito: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onUrlSubmitted: (String) -> Unit,
    onRefresh: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognito: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMenu: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember(url, isFocused) { 
        mutableStateOf(if (url.startsWith("xora://") || url == "about:blank") "" else if (isFocused) url else url.removePrefix("https://").removePrefix("http://")) 
    }

    Surface(
        color = if (isIncognito) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        contentColor = if (isIncognito) Color.LightGray else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                IconButton(onClick = onBack) {
                     Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isIncognito) Color.White else LocalContentColor.current)
                }
                
                // Domain input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isIncognito) Color.DarkGray else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = if (isIncognito) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = if (isIncognito) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isIncognito) Color.LightGray else MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    placeholder = { Text("Search or type URL", color = if(isIncognito) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { if(inputText.isNotBlank()) onUrlSubmitted(inputText) }
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            modifier = Modifier.size(16.dp),
                            tint = if (isIncognito) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (isFocused && inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = if (isIncognito) Color.White else LocalContentColor.current)
                            }
                        } else if (!isFocused) {
                            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp), tint = if (isIncognito) Color.White else LocalContentColor.current)
                            }
                        }
                    }
                )

                IconButton(onClick = onOpenMenu) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = if (isIncognito) Color.White else LocalContentColor.current)
                }
            }

            AnimatedVisibility(visible = isFocused && inputText.isNotBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val isUrlRegex = android.util.Patterns.WEB_URL.matcher(inputText).matches()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isIncognito) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onUrlSubmitted(inputText) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrlRegex) Icons.Default.Lock else Icons.Default.Search,
                            contentDescription = null,
                            tint = if(isIncognito) Color.Gray else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (isUrlRegex) "Go to $inputText" else "Search for \"$inputText\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if(isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuAndTabsPanel(
    identities: List<com.example.model.BrowserIdentity>,
    activeIdentity: com.example.model.BrowserIdentity?,
    tabs: List<com.example.model.Tab>,
    activeTab: com.example.model.Tab?,
    onClose: () -> Unit,
    onSwitchIdentity: (Long) -> Unit,
    onAddIdentity: () -> Unit,
    onNewTab: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onSwitchTab: (Long) -> Unit,
    onCloseTab: (com.example.model.Tab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.Small, vertical = Spacing.Small),
        shape = RoundedCornerShape(Radius.ExtraLarge),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.85f),
        tonalElevation = 0.dp,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Medium, vertical = Spacing.Medium)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Menu & Tabs",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Medium))
            
            // Identity Card
            Surface(
                shape = RoundedCornerShape(Radius.Large),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeColor = activeIdentity?.colorHex?.let { kotlin.runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: MaterialTheme.colorScheme.primary
                    val vector = availableIcons[activeIdentity?.iconName] ?: Icons.Default.Person
                    Icon(
                        imageVector = if (activeIdentity?.isIncognito == true) Icons.Default.Lock else vector,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier
                            .background(activeColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    Text(
                        text = activeIdentity?.name ?: "Personal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    var showIdentities by remember { mutableStateOf(false) }
                    
                    Box {
                        TextButton(onClick = { showIdentities = true }) {
                            Text("SWITCH", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        
                        DropdownMenu(
                            expanded = showIdentities,
                            onDismissRequest = { showIdentities = false }
                        ) {
                            val activeId = activeIdentity?.id
                            identities.forEach { id ->
                                val idColor = kotlin.runCatching { Color(android.graphics.Color.parseColor(id.colorHex)) }.getOrNull() ?: MaterialTheme.colorScheme.primary
                                val baseVector = availableIcons[id.iconName] ?: Icons.Default.Person
                                val vector = if (id.isIncognito) Icons.Default.Lock else baseVector
                                DropdownMenuItem(
                                    leadingIcon = {
                                         Icon(
                                             imageVector = vector,
                                             contentDescription = null,
                                             tint = idColor,
                                             modifier = Modifier
                                                 .background(idColor.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                                                 .padding(4.dp)
                                                 .size(20.dp)
                                         )
                                    },
                                    text = { Text(id.name, fontWeight = if (id.id == activeId) androidx.compose.ui.text.font.FontWeight.Bold else null) },
                                    onClick = {
                                        onSwitchIdentity(id.id)
                                        showIdentities = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                },
                                text = { Text("Add Identity") },
                                onClick = {
                                    onAddIdentity()
                                    showIdentities = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))
            
            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionItem(icon = Icons.Default.Add, label = "New Tab", onClick = onNewTab)
                ActionItem(icon = Icons.Default.Star, label = "Bookmarks", onClick = onBookmarks)
                ActionItem(icon = Icons.Default.Refresh, label = "History", onClick = onHistory)
                ActionItem(icon = Icons.Default.Settings, label = "Settings", onClick = onSettings)
            }
            
            Spacer(modifier = Modifier.height(Spacing.Medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(Spacing.Medium))
            
            // Tabs Grid/Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                contentPadding = PaddingValues(bottom = Spacing.Small)
            ) {
                items(tabs) { tab ->
                    TabCard(
                        tab = tab,
                        isActive = activeTab?.id == tab.id,
                        onClick = { onSwitchTab(tab.id) },
                        onClose = { onCloseTab(tab) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(Radius.Medium),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TabCard(tab: com.example.model.Tab, isActive: Boolean, onClick: () -> Unit, onClose: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radius.Medium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .width(110.dp)
            .height(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                    }
                }
            }
            // Tab Content Thumbnail placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                val iconUrl = "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=${tab.url}&size=128"
                coil.compose.AsyncImage(
                    model = iconUrl,
                    contentDescription = "Favicon",
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_crop) // Placeholder
                )
            }
        }
    }
}
