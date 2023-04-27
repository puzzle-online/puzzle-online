package itmo.ru.puzzle.domain.model

import kotlinx.coroutines.Job
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@JvmInline
value class RoomId(val value: String) {
    companion object {
        fun of(value: String) = RoomId(value)
    }
}

data class Room(
    val id: RoomId,
    val balls: MutableList<Ball> = mutableListOf(),
    val clients: MutableSet<Client> = ConcurrentHashMap.newKeySet(),
) {
    lateinit var updateJob: Job
    lateinit var deleteRoomActionTimer: Timer
}
