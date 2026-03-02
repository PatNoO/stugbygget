package com.example.stugbygget.data.local

import com.example.stugbygget.domain.model.RoomFurniture
import com.example.stugbygget.domain.repository.RoomLayoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalRoomLayoutRepository(
    private val dataSource: LocalRoomLayoutDataSource,
    private val defaultLayoutProvider: () -> List<RoomFurniture>
) : RoomLayoutRepository {

    private val roomFlows = mutableMapOf<String, MutableStateFlow<List<RoomFurniture>>>()

    override fun observeLayout(roomId: String): Flow<List<RoomFurniture>> {
        return roomFlows.getOrPut(roomId) {
            val persisted = dataSource.read(roomId)
            MutableStateFlow(persisted.ifEmpty { defaultLayoutProvider() })
        }.asStateFlow()
    }

    override suspend fun saveLayout(roomId: String, furniture: List<RoomFurniture>) {
        dataSource.write(roomId, furniture)
        roomFlows.getOrPut(roomId) { MutableStateFlow(furniture) }.value = furniture
    }
}
