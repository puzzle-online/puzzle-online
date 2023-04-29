package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.dto.BallDTO
import itmo.ru.puzzle.dto.BoxDTO
import itmo.ru.puzzle.dto.CursorDTO
import kotlinx.serialization.Serializable

@Serializable
data class LeaveRequest(val clientId: String, val roomId: String)

@Serializable
data class JoinRequest(val clientId: String, val roomId: String)

@Serializable
data class PlayRequest(val clientId: String, val roomId: String, val ball: BallDTO)

@Serializable
data class MoveRequest(
    val clientId: String,
    val roomId: String,
    val cursor: CursorDTO,
    val box: BoxDTO?
)

@Serializable
data class CreateRequest(val clientId: String, val boxes: List<BoxDTO>)
