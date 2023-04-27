package itmo.ru.puzzle.dto.response

import itmo.ru.puzzle.domain.model.Ball
import itmo.ru.puzzle.domain.model.BallId
import itmo.ru.puzzle.domain.model.Color
import kotlinx.serialization.Serializable

@Serializable
data class BallDTO(val ballId: Int, val color: String)

fun BallDTO.toBall() = Ball(BallId(ballId), Color.valueOf(color.uppercase()))

fun Ball.toDTO() = BallDTO(id.value, color.name.lowercase())

fun MutableList<Ball>.toResponse() = this.map { it.toDTO() }
