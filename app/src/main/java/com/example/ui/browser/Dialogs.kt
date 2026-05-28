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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun CreateIdentityDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, colorHex: String, iconName: String, isIncognito: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(availableColors[0]) }
    var selectedIcon by remember { mutableStateOf("Person") }
    var isIncognito by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.ExtraLarge),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Text(
                    text = "New Identity",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Identity Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Medium),
                    singleLine = true
                )

                Text("Theme Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    items(availableColors) { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
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
                    Text("Incognito Identity", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isIncognito, onCheckedChange = { isIncognito = it })
                }

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
                            if (name.isNotBlank()) {
                                onCreate(name.trim(), selectedColor, selectedIcon, isIncognito)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(Radius.Medium)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    var searchEngine by remember { mutableStateOf("Google") }
    var theme by remember { mutableStateOf("Auto") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.ExtraLarge),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(Spacing.Small))

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear Browsing Data",
                    subtitle = "History & Tabs",
                    onClick = {
                        onClearData()
                        onDismiss()
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Search,
                    title = "Default Search Engine",
                    subtitle = searchEngine,
                    onClick = { 
                        searchEngine = if (searchEngine == "Google") "Bing" else if (searchEngine == "Bing") "DuckDuckGo" else "Google"
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "Theme",
                    subtitle = theme,
                    onClick = { 
                        theme = if (theme == "Auto") "Dark" else if (theme == "Dark") "Light" else "Auto"
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.Medium))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
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
            .clip(RoundedCornerShape(Radius.Medium))
            .clickable(onClick = onClick)
            .padding(Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.Medium))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
