package itmo.ru.puzzle.dto.response

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import itmo.ru.puzzle.domain.model.Ball
import itmo.ru.puzzle.domain.model.Client
import itmo.ru.puzzle.domain.model.Game
import kotlinx.serialization.Serializable

@Serializable
data class CreateResponse(
    val gameId: String,
    val balls: List<BallResponse>,
    val clientIds: List<String>,
) : Response(Method.CREATE)

fun Game.toCreateResponse() = CreateResponse(id.value, balls.toResponse(), clients.getIds())

private fun MutableList<Ball>.toResponse() = this.map { it.toResponse() }.toList()

@Serializable
data class JoinResponse(
    val gameId: String,
    val balls: List<BallResponse>,
    val clientIds: List<String>,
) : Response(Method.JOIN)

fun Game.toJoinResponse() = JoinResponse(id.value, balls.toResponse(), clients.getIds())

@Serializable
data class GetGameDescriptionResponse(
    val gameId: String,
    val clientAmount: Int,
)

fun Game.toGetGameDescriptionResponse() = GetGameDescriptionResponse(id.value, clients.size)

@Serializable
data class GetGamesResponse(
    val rooms: List<GetGameDescriptionResponse>,
) : Response(Method.ROOMS)

@Serializable
data class UpdateResponse(
    val balls: List<BallResponse>,
    val clientIds: List<String>,
) : Response(Method.UPDATE)

fun Game.toUpdateResponse() = UpdateResponse(balls.toResponse(), clients.getIds())

private fun MutableSet<Client>.getIds() = this.map { it.id.value }.toList()