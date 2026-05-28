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
    var showMenu by remember { mutableStateOf(false) }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
                    TextButton(onClick = { 
                        showSettings = false
                        viewModel.clearBrowsingData() 
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Clear Browsing Data (History & Tabs)")
                    }
                    TextButton(onClick = { showSettings = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Default Search Engine: Google")
                    }
                    TextButton(onClick = { showSettings = false }, modifier = Modifier.fillMaxWidth()) {
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

    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Large, start = Spacing.Medium, end = Spacing.Medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                        showMenu = false
                        viewModel.createNewTab()
                    }.padding(Spacing.Small)) {
                        Surface(shape = RoundedCornerShape(Radius.Medium), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "New Tab", modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                        Text("New Tab", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                        showMenu = false
                        viewModel.createIdentity("Incognito", isIncognito = true)
                    }.padding(Spacing.Small)) {
                        Surface(shape = RoundedCornerShape(Radius.Medium), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = "Incognito", modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                        Text("Incognito", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                         showMenu = false
                    }.padding(Spacing.Small)) {
                        Surface(shape = RoundedCornerShape(Radius.Medium), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Star, contentDescription = "Bookmarks", modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                        Text("Bookmarks", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.Medium))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.Small))
                
                ListItem(
                    headlineContent = { Text("Downloads") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable { showMenu = false }
                )
                ListItem(
                    headlineContent = { Text("Settings") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showMenu = false
                        showSettings = true 
                    }
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        
        // ------------- Hidden Panel (Tabs & Identities) -------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(panelMaxHeightDp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Spacing.Medium, vertical = Spacing.Large)
        ) {
             Text("Identities", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
             Spacer(modifier = Modifier.height(Spacing.Small))
             LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                 items(identities) { identity ->
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
                                     Icon(Icons.Default.Lock, contentDescription = "Incognito", modifier = Modifier.size(14.dp))
                                     Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                                 }
                                 Text(identity.name, style = MaterialTheme.typography.labelMedium)
                             }
                         },
                         shape = RoundedCornerShape(Radius.Medium)
                     )
                 }
                 
                 item {
                     // ADD IDENTITY BUTTON
                     FilterChip(
                         selected = false,
                         onClick = { viewModel.createIdentity("Profile ${identities.size + 1}") },
                         label = { Text("+ Add", style = MaterialTheme.typography.labelMedium) },
                         shape = RoundedCornerShape(Radius.Medium)
                     )
                 }
             }

             Spacer(modifier = Modifier.height(Spacing.Large))
             
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                 Text("Open Tabs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                 FilledTonalButton(onClick = { 
                     viewModel.createNewTab()
                     coroutineScope.launch {
                         offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.spring())
                     } 
                 }, shape = RoundedCornerShape(Radius.ExtraLarge)) {
                     Icon(Icons.Default.Add, contentDescription = "New Tab", modifier = Modifier.size(16.dp))
                     Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                     Text("New Tab")
                 }
             }
             
             Spacer(modifier = Modifier.height(Spacing.Medium))
             
             // Tabs list
             LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
                 items(tabs) { tab ->
                     val isSelected = tab.id == activeTab?.id
                     Card(
                         shape = RoundedCornerShape(Radius.Medium),
                         colors = CardDefaults.cardColors(
                             containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                         ),
                         elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                         modifier = Modifier.width(160.dp).height(120.dp),
                         onClick = {
                             viewModel.switchTab(tab.id)
                             coroutineScope.launch {
                                 offsetY.animateTo(0f, animationSpec = androidx.compose.animation.core.spring())
                             }
                         }
                     ) {
                         Box(modifier = Modifier.fillMaxSize().padding(Spacing.Medium)) {
                             Text(
                                 tab.title, 
                                 style = MaterialTheme.typography.bodyMedium, 
                                 maxLines = 2, 
                                 color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                 modifier = Modifier.align(Alignment.TopStart).padding(end = 24.dp)
                             )
                             IconButton(
                                 onClick = { viewModel.closeTab(tab) }, 
                                 modifier = Modifier.align(Alignment.TopEnd).size(24.dp).offset(x = 8.dp, y = (-8).dp)
                             ) {
                                 Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
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
                onOpenMenu = { showMenu = true },
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
    onOpenMenu: () -> Unit,
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

            IconButton(onClick = onOpenMenu) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = if (isIncognito) Color.White else LocalContentColor.current)
            }
        }
    }
}
