// app/src/main/java/com/baverika/r_journal/data/local/entity/ChatMessage.kt

package com.baverika.r_journal.data.local.entity

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String = "user",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null // Nullable string for image path/URI
) {
    // ✅ No-arg constructor that calls primary with DEFAULTS
    constructor() : this(
        id = java.util.UUID.randomUUID().toString(),
        role = "user",
        content = "",
        timestamp = System.currentTimeMillis(),
        imageUri = null

    )
}