package itmo.ru.puzzle.domain.model

// TODO: serializable?
enum class Color { RED, BLUE, GREEN }

@JvmInline
value class BallId(val value: Int)

// TODO: think about extracting id in value class
data class Ball(val id: BallId, var color: Color)