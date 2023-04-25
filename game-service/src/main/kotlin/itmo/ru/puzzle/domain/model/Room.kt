package itmo.ru.puzzle.domain.model

import kotlinx.coroutines.Job
import java.util.*

@JvmInline
value class RoomId(val value: String)

data class Room(
    val id: RoomId,
    val balls: MutableList<Ball> = mutableListOf(),
    val clients: MutableSet<Client> = mutableSetOf(),
) {
    lateinit var updateJob: Job
    lateinit var deleteRoomActionTimer: Timer
}