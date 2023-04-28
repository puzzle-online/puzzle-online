package itmo.ru.puzzle.domain.model

import itmo.ru.puzzle.dto.request.CursorDTO
import kotlinx.serialization.Serializable

@JvmInline
value class ClientId(val value: String)

data class Client(val id: ClientId, var cursor: Cursor?) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Client) return false

        return id == other.id
    }
}

fun Client.toDTO() = ClientDTO(id.value, cursor?.toDTO())

@Serializable
data class ClientDTO(val id: String, val cursor: CursorDTO?)
