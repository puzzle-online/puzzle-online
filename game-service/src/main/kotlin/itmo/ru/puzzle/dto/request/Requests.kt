package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.dto.response.BallDTO
import itmo.ru.puzzle.dto.response.toBall
import kotlinx.serialization.Serializable

@Serializable
data class LeaveRequest(val roomId: String)

@Serializable
data class JoinRequest(val roomId: String)

@Serializable
data class PlayRequest(val roomId: String, val ballDTO: BallDTO)

fun PlayRequest.toBall() = ballDTO.toBall()
