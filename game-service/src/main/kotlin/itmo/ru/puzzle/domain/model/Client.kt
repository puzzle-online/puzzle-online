package itmo.ru.puzzle.domain.model

@JvmInline
value class ClientId(val value: String)

data class Client(val id: ClientId) {
    var nickname: String = ""
    var cursor: Cursor? = null

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Client) return false

        return id == other.id
    }
}

