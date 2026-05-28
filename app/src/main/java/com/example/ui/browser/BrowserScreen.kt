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
import androidx.compose.material.icons.filled.ArrowBack
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

@Composable
fun BrowserScreen(modifier: Modifier = Modifier) {
    val viewModel: BrowserViewModel = viewModel(factory = BrowserViewModel.Factory)
    val tabs by viewModel.tabs.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isAddressBarFocused by viewModel.isAddressBarFocused.collectAsState()
    val activeIdentity by viewModel.activeIdentity.collectAsState()
    val identities by viewModel.identities.collectAsState()

    var webView by remember { mutableStateOf<WebView?>(null) }
    val focusManager = LocalFocusManager.current

    val panelMaxHeightDp = 400.dp
    val density = LocalDensity.current
    val panelMaxHeightPx = with(density) { panelMaxHeightDp.toPx() }
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val newOffset = (offsetY.value + delta).coerceIn(0f, panelMaxHeightPx)
            offsetY.snapTo(newOffset)
        }
    }

    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                Column {
                    TextButton(onClick = { 
                        showSettings = false
                        viewModel.clearBrowsingData() 
                    }) {
                        Text("Clear Browsing Data (History & Tabs)")
                    }
                    TextButton(onClick = { showSettings = false }) {
                        Text("Default Search Engine: Google")
                    }
                    TextButton(onClick = { showSettings = false }) {
                        Text("Theme: Auto")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Done")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        
        // ------------- Hidden Panel (Tabs & Identities) -------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { offsetY.value.toDp() } + 64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
             Text("Identities", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
             Spacer(modifier = Modifier.height(8.dp))
             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 identities.forEach { identity ->
                     val selected = identity.id == activeIdentity?.id
                     FilterChip(
                         selected = selected,
                         onClick = { 
                             viewModel.switchIdentity(identity.id) 
                             coroutineScope.launch {
                                 offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.spring())
                             }
                         },
                         label = { 
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 if (identity.isIncognito) {
                                     Icon(Icons.Default.Lock, contentDescription = "Incognito", modifier = Modifier.size(16.dp))
                                     Spacer(modifier = Modifier.width(4.dp))
                                 }
                                 Text(identity.name)
                             }
                         }
                     )
                 }
                 
                 // ADD IDENTITY BUTTON
                 FilterChip(
                     selected = false,
                     onClick = { viewModel.createIdentity("Profile ${identities.size + 1}") },
                     label = { Text("+ Add") }
                 )
             }

             Spacer(modifier = Modifier.height(16.dp))
             
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                 Text("Open Tabs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                 TextButton(onClick = { 
                     viewModel.createNewTab()
                     coroutineScope.launch {
                         offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.spring())
                     } 
                 }) {
                     Text("New Tab")
                 }
             }
             
             Spacer(modifier = Modifier.height(8.dp))
             
             // Tabs list
             LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 items(tabs) { tab ->
                     val isSelected = tab.id == activeTab?.id
                     Surface(
                         shape = RoundedCornerShape(12.dp),
                         color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                         modifier = Modifier.width(140.dp).height(100.dp),
                         onClick = {
                             viewModel.switchTab(tab.id)
                             coroutineScope.launch {
                                 offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.spring())
                             }
                         }
                     ) {
                         Box(modifier = Modifier.padding(8.dp)) {
                             Text(tab.title, style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.align(Alignment.TopStart))
                             IconButton(onClick = { viewModel.closeTab(tab) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                                 Icon(Icons.Default.Refresh, contentDescription = "Close", modifier = Modifier.size(16.dp))
                             }
                         }
                     }
                 }
             }
        }
        
        // ------------- Main Content (Top Chrome + WebView) -------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { androidx.compose.ui.unit.IntOffset(0, offsetY.value.roundToInt()) }
                .background(if (activeIdentity?.isIncognito == true) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.background)
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
                onBack = { if (webView?.canGoBack() == true) webView?.goBack() },
                modifier = Modifier
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity -> 
                             val target = if (offsetY.value > panelMaxHeightPx / 2 || velocity > 500f) panelMaxHeightPx else 0f
                             coroutineScope.launch {
                                 offsetY.animateTo(target, initialVelocity = velocity, animationSpec = androidx.compose.animation.core.spring())
                             }
                        }
                    )
            )

            // Web View Area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (activeTab != null) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        url?.let { viewModel.onPageStarted(it) }
                                    }
                                    
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        return false
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
                                loadUrl(activeTab!!.url)
                            }
                        },
                        update = { view ->
                             val currentViewUrl = view.url
                             val targetedUrl = activeTab?.url
                             if (targetedUrl != null && currentViewUrl != targetedUrl && !isAddressBarFocused) {
                                 // When switching tabs or submitting a new url, load it.
                                 // But avoid reloading during typing or redirect.
                                 if (currentViewUrl == null || !currentViewUrl.contains(targetedUrl) && !targetedUrl.contains(currentViewUrl)) {
                                    view.loadUrl(targetedUrl)
                                 }
                             }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember(url, isFocused) { mutableStateOf(if (isFocused) url else url.removePrefix("https://").removePrefix("http://")) }

    Surface(
        color = if (isIncognito) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        contentColor = if (isIncognito) Color.LightGray else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            IconButton(onClick = { 
                /* Emit back event, handled below, or just pass back action */
                onBack()
            }) {
                 Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = if (isIncognito) Color.White else LocalContentColor.current)
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onUrlSubmitted(inputText) }
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
                    if (isFocused) {
                        IconButton(onClick = { inputText = "" }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = if (isIncognito) Color.White else LocalContentColor.current)
                        }
                    } else {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp), tint = if (isIncognito) Color.White else LocalContentColor.current)
                        }
                    }
                }
            )

            var showMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = if (isIncognito) Color.White else LocalContentColor.current)
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.width(220.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Tab") },
                        onClick = { 
                            showMenu = false
                            onNewTab()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Incognito Tab") },
                        onClick = { 
                            showMenu = false
                            onNewIncognito()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Bookmarks") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("History") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Downloads") },
                        onClick = { showMenu = false }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Desktop site") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { 
                            showMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }
}
