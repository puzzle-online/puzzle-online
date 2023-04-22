package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class LeaveRequest(val clientId: String, val gameId: String)

fun LeaveRequest.toClient() = Client(ClientId(clientId))

fun LeaveRequest.toGame() = Game(GameId(gameId))

@Serializable
data class JoinRequest(val clientId: String, val gameId: String)

fun JoinRequest.toClient() = Client(ClientId(clientId))

fun JoinRequest.toGame() = Game(GameId(gameId))

@Serializable
data class PlayRequest(val clientId: String, val gameId: String, val ball: BallDTO)

fun PlayRequest.toClient() = Client(ClientId(clientId))

fun PlayRequest.toGame() = Game(GameId(gameId))

fun PlayRequest.toBall() = ball.toBall()

@Serializable
data class BallDTO(val ballId: Int, val color: String)

fun BallDTO.toBall() = Ball(BallId(ballId), Color.valueOf(color.uppercase()))
