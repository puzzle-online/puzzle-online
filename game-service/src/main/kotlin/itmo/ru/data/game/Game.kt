package itmo.ru.data.game

import itmo.ru.data.Ball
import itmo.ru.data.Color
import itmo.ru.data.client.Client
import itmo.ru.data.client.ClientId
import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class GameId(val value: String)

@Serializable
data class Game(
    val gameId: GameId,
    val balls: MutableList<Ball> = mutableListOf(),
    val clients: MutableSet<Client> = mutableSetOf(),
)

@Serializable
data class CreateGameResponse(
    val gameId: GameId,
    val balls: List<Color>,
) : Response(Method.CREATE)

fun Game.toCreateResponse() = CreateGameResponse(gameId, balls.getColors())

private fun MutableList<Ball>.getColors() = this.map { it.color }.toList()

@Serializable
data class JoinGameResponse(
    val gameId: GameId,
    val balls: List<Color>,
    val id: List<ClientId>,
) : Response(Method.JOIN)

fun Game.toJoinResponse() = JoinGameResponse(gameId, balls.getColors(), clients.getIds())

@Serializable
data class GameUpdateResponse(
    val gameId: GameId,
    val balls: List<Color>,
    val id: List<ClientId>,
) : Response(Method.UPDATE)

fun Game.toUpdateResponse() = GameUpdateResponse(gameId, balls.getColors(), clients.getIds())

private fun MutableSet<Client>.getIds() = this.map { it.id }.toList()
