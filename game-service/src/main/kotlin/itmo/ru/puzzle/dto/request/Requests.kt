package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class LeaveRequest(val clientId: String, val roomId: String)

@Serializable
data class JoinRequest(val clientId: String, val roomId: String)

@Serializable
data class PlayRequest(val clientId: String, val roomId: String, val ball: BallDTO)

@Serializable
data class MoveRequest(val clientId: String, val roomId: String, val cursor: PointDTO, val box: PointDTO?)

@Serializable
data class PointDTO(val x: Float, val y: Float)

fun PlayRequest.toBall() = ball.toBall()

@Serializable
data class BallDTO(val ballId: Int, val color: String)

fun BallDTO.toBall() = Ball(BallId(ballId), Color.valueOf(color.uppercase()))
