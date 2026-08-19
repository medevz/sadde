package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionsJson: String? = null,
    val isError: Boolean = false,
    val isOffline: Boolean = false
)
