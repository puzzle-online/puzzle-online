package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class JoinRequest(val clientId: String, val gameId: String)

fun JoinRequest.toClient() = Client(ClientId(clientId))

fun JoinRequest.toGame() = Game(GameId(gameId))

@Serializable
data class SetRequest(val clientId: String, val gameId: String, val ballRequest: BallRequest)

fun SetRequest.toClient() = Client(ClientId(clientId))

fun SetRequest.toGame() = Game(GameId(gameId))

fun SetRequest.toBall() = ballRequest.toBall()

@Serializable
data class BallRequest(val ballId: Int, val color: String)

fun BallRequest.toBall() = Ball(BallId(ballId), Color.valueOf(color))
