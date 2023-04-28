package itmo.ru.puzzle.domain.model

import itmo.ru.puzzle.dto.request.BoxDTO
import itmo.ru.puzzle.dto.request.CursorDTO
import kotlinx.coroutines.Job
import java.util.*


data class Cursor(val x: Float, val y: Float)

fun Cursor.toDTO() = CursorDTO(x, y)
data class Box(val id: Int, val x: Float, val y: Float)

fun Box.toDTO() = BoxDTO(id, x, y)


@JvmInline
value class RoomId(val value: String)

data class Room(
    val id: RoomId,
    val balls: MutableList<Ball> = mutableListOf(),
    val clients: MutableSet<Client> = mutableSetOf(),
    val boxes: MutableSet<Box> = mutableSetOf(),
) {
    lateinit var updateJob: Job
    lateinit var deleteRoomActionTimer: Timer
}