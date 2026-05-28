package com.example.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

val availableColors = listOf(
    "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0",
    "#42A5F5", "#26C6DA", "#26A69A", "#66BB6A", "#FFA726",
    "#FF7043", "#8D6E63", "#78909C", "#455A64"
)

val availableIcons = mapOf(
    "Person" to Icons.Default.Person,
    "Face" to Icons.Default.Face,
    "Home" to Icons.Default.Home,
    "Work" to Icons.Default.Build, // Replacing work with Build since we don't have work vector instantly here
    "Favorite" to Icons.Default.Favorite,
    "Star" to Icons.Default.Star,
    "Mail" to Icons.Default.Email
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityDialog(
    initialIdentity: com.example.model.BrowserIdentity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, iconName: String, isIncognito: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialIdentity?.name ?: "") }
    var selectedColor by remember { mutableStateOf(initialIdentity?.colorHex ?: availableColors[0]) }
    var selectedIcon by remember { mutableStateOf(initialIdentity?.iconName ?: "Person") }
    var isIncognito by remember { mutableStateOf(initialIdentity?.isIncognito ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.ExtraLarge),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.8f),
            tonalElevation = 0.dp,
            shadowElevation = 24.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.ExtraLarge)).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Text(
                    text = if (initialIdentity == null) "New Profile" else "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Medium),
                    singleLine = true
                )

                Text("Theme Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    items(availableColors) { colorHex ->
                        val color = kotlin.runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrElse { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 0.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                Text("Icon", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    items(availableIcons.keys.toList()) { iconName ->
                        val vector = availableIcons[iconName]!!
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(Radius.Medium))
                                .background(
                                    if (selectedIcon == iconName) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconName }
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconName,
                                tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onPrimaryContainer 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().clickable { isIncognito = !isIncognito }.padding(vertical = Spacing.Small)
                ) {
                    Text("Incognito Profile", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isIncognito, onCheckedChange = { isIncognito = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(name.trim(), selectedColor, selectedIcon, isIncognito)
                                }
                            },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(Radius.Medium)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkDialog(
    initialTitle: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var url by remember { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.ExtraLarge),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.8f),
            tonalElevation = 0.dp,
            shadowElevation = 24.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.ExtraLarge)).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Text(
                    text = "Add Bookmark",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Medium),
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Medium),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(Spacing.Small))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && url.isNotBlank()) {
                                onSave(title.trim(), url.trim())
                            }
                        },
                        enabled = title.isNotBlank() && url.isNotBlank(),
                        shape = RoundedCornerShape(Radius.Medium)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    identities: List<com.example.model.BrowserIdentity>,
    searchEngine: String,
    theme: String,
    tabLayoutStyle: String,
    showSearchSuggestions: Boolean,
    blockThirdPartyCookies: Boolean,
    autoSelectUrlOnClick: Boolean,
    enableJavaScript: Boolean,
    onSearchEngineChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onTabLayoutStyleChange: (String) -> Unit,
    onShowSearchSuggestionsChange: (Boolean) -> Unit,
    onBlockThirdPartyCookiesChange: (Boolean) -> Unit,
    onAutoSelectUrlOnClickChange: (Boolean) -> Unit,
    onEnableJavaScriptChange: (Boolean) -> Unit,
    onEditIdentity: (com.example.model.BrowserIdentity) -> Unit,
    onCreateIdentity: () -> Unit,
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
        ) {
            Box(modifier = Modifier.fillMaxSize().blur(20.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.White.copy(0.1f), Color.Transparent))))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                // Top App Bar
                TopAppBar(
                    title = { Text("Settings", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.Large)
                ) {
                    item { SettingsCategoryHeader("Profiles & Identities") }
                    items(identities) { identity ->
                        SettingsItem(
                            icon = availableIcons[identity.iconName] ?: Icons.Default.Person,
                            title = identity.name,
                            subtitle = if (identity.isIncognito) "Incognito profile" else "Standard profile",
                            onClick = { onEditIdentity(identity) }
                        )
                    }
                    item {
                        SettingsItem(
                            icon = Icons.Default.Add,
                            title = "Add Profile",
                            subtitle = "Create a new browsing identity",
                            onClick = onCreateIdentity
                        )
                    }

                    item { SettingsCategoryHeader("Appearance") }
                    item {
                        SettingsItem(
                            icon = Icons.Default.Settings,
                            title = "Theme",
                            subtitle = theme,
                            onClick = { 
                                val next = if (theme == "Auto") "Dark" else if (theme == "Dark") "Light" else "Auto"
                                onThemeChange(next)
                            }
                        )
                    }
                    item {
                        SettingsItem(
                            icon = Icons.Default.Menu,
                            title = "Tab Layout Style",
                            subtitle = tabLayoutStyle,
                            onClick = {
                                val next = if (tabLayoutStyle == "Row") "Grid" else "Row"
                                onTabLayoutStyleChange(next)
                            }
                        )
                    }

                    item { SettingsCategoryHeader("Search") }
                    item {
                        SettingsItem(
                            icon = Icons.Default.Search,
                            title = "Default Search Engine",
                            subtitle = searchEngine,
                            onClick = { 
                                val next = if (searchEngine == "Google") "Bing" else if (searchEngine == "Bing") "DuckDuckGo" else "Google"
                                onSearchEngineChange(next)
                            }
                        )
                    }
                    item {
                        SettingsSwitchItem(
                            icon = Icons.Default.List,
                            title = "Show Search Suggestions",
                            subtitle = "Display predictive matches while typing",
                            checked = showSearchSuggestions,
                            onCheckedChange = onShowSearchSuggestionsChange
                        )
                    }

                    item { SettingsCategoryHeader("Privacy & Data") }
                    item {
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = "Clear Browsing Data",
                            subtitle = "History & Tabs",
                            onClick = {
                                onClearData()
                            }
                        )
                    }
                    item {
                        SettingsSwitchItem(
                            icon = Icons.Default.Warning,
                            title = "Block Third-Party Cookies",
                            subtitle = "Prevent cross-site tracking",
                            checked = blockThirdPartyCookies,
                            onCheckedChange = onBlockThirdPartyCookiesChange
                        )
                    }

                    item { SettingsCategoryHeader("Advanced & UX") }
                    item {
                        SettingsSwitchItem(
                            icon = Icons.Default.Edit,
                            title = "Auto-select URL on click",
                            subtitle = "Entire URL is selected when tapped",
                            checked = autoSelectUrlOnClick,
                            onCheckedChange = onAutoSelectUrlOnClickChange
                        )
                    }
                    item {
                        SettingsSwitchItem(
                            icon = Icons.Default.Build,
                            title = "Enable JavaScript",
                            subtitle = "Allow sites to run interactive scripts",
                            checked = enableJavaScript,
                            onCheckedChange = onEnableJavaScriptChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 72.dp, top = Spacing.Large, bottom = Spacing.Small)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp).padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp).padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
