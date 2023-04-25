package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Room

class RoomRepository {
    private val roomDatabase = mutableMapOf<String, Room>()

    fun put(room: Room) {
        roomDatabase.putIfAbsent(room.id.value, room)
    }

    fun remove(room: Room) {
        roomDatabase.remove(room.id.value)
    }

    fun get(roomId: String): Room? {
        return roomDatabase[roomId]
    }

    fun contains(roomId: String): Boolean {
        return roomDatabase.containsKey(roomId)
    }

    fun getAllRooms(): List<Room> {
        return roomDatabase.values.toList()
    }
}