package com.example.data.model

data class ConnectedWebsite(
    val id: String,
    val title: String,
    val url: String,
    val category: String,
    val iconEmoji: String = "🌐",
    val description: String = "",
    val isOpenInApp: Boolean = true,
    val isPublic: Boolean = true,
    val visitCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
