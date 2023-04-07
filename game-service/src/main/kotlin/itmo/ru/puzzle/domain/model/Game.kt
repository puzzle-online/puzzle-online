package itmo.ru.puzzle.domain.model

@JvmInline
value class GameId(val value: String)

data class Game(
    val id: GameId,
    val balls: MutableList<Ball> = mutableListOf(),
    val clients: MutableSet<Client> = mutableSetOf(),
)
