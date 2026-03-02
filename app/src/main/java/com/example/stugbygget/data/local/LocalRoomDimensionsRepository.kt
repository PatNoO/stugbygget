package com.example.stugbygget.data.local

import android.content.Context
import com.example.stugbygget.domain.repository.RoomDimensions
import com.example.stugbygget.domain.repository.RoomDimensionsRepository

class LocalRoomDimensionsRepository(
    context: Context
) : RoomDimensionsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(roomId: String): RoomDimensions {
        val width = prefs.getInt("${roomId}_width_cm", DEFAULT_WIDTH_CM)
        val height = prefs.getInt("${roomId}_height_cm", DEFAULT_HEIGHT_CM)
        return RoomDimensions(widthCm = width, heightCm = height)
    }

    override fun save(roomId: String, dimensions: RoomDimensions) {
        prefs.edit()
            .putInt("${roomId}_width_cm", dimensions.widthCm)
            .putInt("${roomId}_height_cm", dimensions.heightCm)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "room_dimensions"
        private const val DEFAULT_WIDTH_CM = 1200
        private const val DEFAULT_HEIGHT_CM = 800
    }
}
