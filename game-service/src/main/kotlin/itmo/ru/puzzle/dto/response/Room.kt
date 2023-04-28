package itmo.ru.puzzle.dto.response

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.dto.request.BoxDTO
import kotlinx.serialization.Serializable

@Serializable
data class CreateResponse(
    val roomId: String,
    val boxes: List<BoxDTO>,
    val clients: List<ClientDTO>,
) : Response(Method.CREATE)

fun Room.toCreateResponse() = CreateResponse(
    id.value,
    boxes.toResponse(),
    clients.map { it.toDTO() }
)

//private fun MutableList<Ball>.toResponse() = this.map { it.toResponse() }.toList()
private fun MutableSet<Box>.toResponse() = this.map { it.toDTO() }.toList()


@Serializable
data class JoinResponse(
    val roomId: String,
    val boxes: List<BoxDTO>,
    val clients: List<ClientDTO>,
) : Response(Method.JOIN)

fun Room.toJoinResponse() = JoinResponse(
    id.value,
    boxes.toResponse(),
    clients.map { it.toDTO() }
)

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
    val boxes: List<BoxDTO>,
    val clients: List<ClientDTO>,
) : Response(Method.UPDATE)

fun Room.toUpdateResponse() = UpdateResponse(
    boxes.toResponse(),
    clients.map { it.toDTO() }
)

private fun MutableSet<Client>.getIds() = this.map { it.id.value }.toList()