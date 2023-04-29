package itmo.ru.puzzle.dto.response

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import itmo.ru.puzzle.domain.model.Room
import itmo.ru.puzzle.dto.BoxDTO
import itmo.ru.puzzle.dto.toResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateResponse(
    val roomId: String,
    val boxes: List<BoxDTO>,
    val clients: List<ClientCursorUpdateDTO>,
) : Response(Method.CREATE)

fun Room.toCreateResponse() = CreateResponse(
    id.value,
    boxes.toResponse(),
    clients.map { it.toDTO() }
)

@Serializable
data class JoinResponse(
    val roomId: String,
    val boxes: List<BoxDTO>,
    val clients: List<ClientCursorUpdateDTO>,
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
    val clients: List<ClientCursorUpdateDTO>,
) : Response(Method.UPDATE)

fun Room.toUpdateResponse() = UpdateResponse(
    boxes.toResponse(),
    clients.map { it.toDTO() }
)
