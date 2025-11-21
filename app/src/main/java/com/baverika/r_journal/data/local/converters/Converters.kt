package com.baverika.r_journal.data.local.converters

import android.net.Uri
import androidx.room.TypeConverter
import com.baverika.r_journal.data.local.entity.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    // -------------------------------
    // ChatMessage converters
    // -------------------------------
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
            obj.put("imageUri", msg.imageUri) // nullable
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
            messages.add(
                ChatMessage(
                    id = obj.getString("id"),
                    role = obj.getString("role"),
                    content = obj.getString("content"),
                    timestamp = obj.getLong("timestamp"),
                    imageUri = obj.optString("imageUri").takeIf { it.isNotBlank() }
                )
            )
        }
        return messages
    }

    // -------------------------------
    // Tags converters (List<String>)
    // -------------------------------
    @TypeConverter
    fun fromTags(tags: List<String>?): String {
        if (tags.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        tags.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toTags(json: String?): List<String> {
        if (json.isNullOrEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { i -> array.getString(i) }
    }

    // -------------------------------
    // ImageUris converters (List<Uri>)
    // -------------------------------
    @TypeConverter
    fun fromImageUris(uris: List<Uri>?): String {
        if (uris.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        uris.forEach { array.put(it.toString()) }
        return array.toString()
    }

    @TypeConverter
    fun toImageUris(json: String?): List<Uri> {
        if (json.isNullOrEmpty() || json == "[]") return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { i -> Uri.parse(array.getString(i)) }
    }
}
