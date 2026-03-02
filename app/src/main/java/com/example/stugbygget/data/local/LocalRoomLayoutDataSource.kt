package com.example.stugbygget.data.local

import android.content.Context
import com.example.stugbygget.domain.model.RoomFurniture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalRoomLayoutDataSource(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun read(roomId: String): List<RoomFurniture> {
        val raw = prefs.getString(layoutKey(roomId), null) ?: return emptyList()
        val type = object : TypeToken<List<RoomFurniture>>() {}.type
        return runCatching { gson.fromJson<List<RoomFurniture>>(raw, type) }.getOrDefault(emptyList())
    }

    fun write(roomId: String, furniture: List<RoomFurniture>) {
        prefs.edit().putString(layoutKey(roomId), gson.toJson(furniture)).apply()
    }

    private fun layoutKey(roomId: String): String = "layout_$roomId"

    companion object {
        private const val PREFS_NAME = "room_layouts"
    }
}
