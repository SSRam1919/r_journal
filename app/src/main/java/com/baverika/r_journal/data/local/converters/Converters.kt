// app/src/main/java/com/baverika/r_journal/data/local/converters/Converters.kt

package com.baverika.r_journal.data.local.converters

import androidx.room.TypeConverter
import com.baverika.r_journal.data.local.entity.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromMessages(messages: List<ChatMessage>?): String {
        if (messages.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("role", msg.role)
            obj.put("content", msg.content)
            obj.put("timestamp", msg.timestamp)
            obj.put("imageUri", msg.imageUri ?: "")
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toMessages(json: String?): List<ChatMessage> {
        if (json.isNullOrEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        val messages = mutableListOf<ChatMessage>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val imageUri = obj.optString("imageUri", "")
            messages.add(
                ChatMessage(
                    id = obj.getString("id"),
                    role = obj.getString("role"),
                    content = obj.getString("content"),
                    timestamp = obj.getLong("timestamp"),
                    imageUri = if (imageUri.isBlank()) null else imageUri
                )
            )
        }
        return messages
    }

    @TypeConverter
    fun fromTags(tags: List<String>?): String {
        if (tags.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (tag in tags) {
            array.put(tag)
        }
        return array.toString()
    }

    @TypeConverter
    fun toTags(json: String?): List<String> {
        if (json.isNullOrEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        val tags = mutableListOf<String>()
        for (i in 0 until array.length()) {
            tags.add(array.getString(i))
        }
        return tags
    }

    // ✅ NEW: TypeConverter for imageUris (List<String>)
    @TypeConverter
    fun fromImageUris(uris: List<String>?): String {
        if (uris.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (uri in uris) {
            array.put(uri)
        }
        return array.toString()
    }

    @TypeConverter
    fun toImageUris(json: String?): List<String> {
        if (json.isNullOrEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        val uris = mutableListOf<String>()
        for (i in 0 until array.length()) {
            uris.add(array.getString(i))
        }
        return uris
    }
}