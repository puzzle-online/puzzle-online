package itmo.ru.puzzle.dto.response

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import itmo.ru.puzzle.domain.model.Client
import itmo.ru.puzzle.domain.model.Room
import kotlinx.serialization.Serializable

@Serializable
data class CreateResponse(
    val roomId: String,
    val balls: List<BallDTO>,
    val clientIds: List<String>,
) : Response(Method.CREATE)

fun Room.toCreateResponse() = CreateResponse(id.value, balls.toResponse(), clients.getIds())

@Serializable
data class JoinResponse(
    val roomId: String,
    val balls: List<BallDTO>,
    val clientIds: List<String>,
) : Response(Method.JOIN)

fun Room.toJoinResponse() = JoinResponse(id.value, balls.toResponse(), clients.getIds())

@Serializable
data class GetRoomDescriptionResponse(
    val roomId: String,
    val clientAmount: Int,
)

fun Room.toGetRoomDescriptionResponse() = GetRoomDescriptionResponse(id.value, clients.size)

@Serializable
data class GetRoomsResponse(
    val rooms: List<GetRoomDescriptionResponse>,
) : Response(Method.ROOMS)

@Serializable
data class UpdateResponse(
    val balls: List<BallDTO>,
    val clientIds: List<String>,
) : Response(Method.UPDATE) {
    companion object {
        fun of(room: Room) = UpdateResponse(room.balls.toResponse(), room.clients.getIds())
    }
}

private fun MutableSet<Client>.getIds() = this.map { it.id.value }