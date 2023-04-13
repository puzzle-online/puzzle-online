package itmo.ru.puzzle.domain.model

@JvmInline
value class ClientId(val value: String)

data class Client(val id: ClientId)
