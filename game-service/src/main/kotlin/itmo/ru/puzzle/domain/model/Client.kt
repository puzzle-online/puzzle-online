package itmo.ru.puzzle.domain.model

import itmo.ru.puzzle.dto.request.CursorDTO
import kotlinx.serialization.Serializable

@JvmInline
value class ClientId(val value: String)

data class Client(val id: ClientId, var cursor: Cursor?)

fun Client.toDTO() = ClientDTO(id.value, cursor?.toDTO())

@Serializable
data class ClientDTO(val id: String, val cursor: CursorDTO?)
