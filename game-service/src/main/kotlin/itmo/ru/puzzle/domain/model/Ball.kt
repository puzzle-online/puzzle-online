package itmo.ru.puzzle.domain.model

enum class Color { RED, BLUE, GREEN }

@JvmInline
value class BallId(val value: Int)

data class Ball(val id: BallId, var color: Color)
