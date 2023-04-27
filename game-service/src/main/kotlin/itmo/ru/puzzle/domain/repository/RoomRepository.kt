package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Room
import itmo.ru.puzzle.domain.model.RoomId

class RoomRepository {
    private val roomDatabase = mutableMapOf<RoomId, Room>()

    fun put(room: Room) {
        roomDatabase.putIfAbsent(room.id, room)
    }

    fun remove(room: Room) {
        roomDatabase.remove(room.id)
    }

    fun get(roomId: RoomId): Room? {
        return roomDatabase[roomId]
    }

    fun contains(roomId: RoomId): Boolean {
        return roomDatabase.containsKey(roomId)
    }

    fun getAllRooms(): List<Room> {
        return roomDatabase.values.toList()
    }
}
