package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class Tab(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identityId: Long,
    val title: String,
    val url: String,
    val orderIndex: Int,
    val isActive: Boolean = false
)

@Entity(tableName = "histories")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identityId: Long,
    val title: String,
    val url: String,
    val timestamp: Long
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identityId: Long,
    val title: String,
    val url: String
)
