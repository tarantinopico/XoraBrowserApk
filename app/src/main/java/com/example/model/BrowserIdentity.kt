package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browser_identities")
data class BrowserIdentity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isActive: Boolean = false,
    val isIncognito: Boolean = false
)
