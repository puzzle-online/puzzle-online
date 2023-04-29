package itmo.ru.puzzle.dto

import itmo.ru.puzzle.domain.model.Ball
import itmo.ru.puzzle.domain.model.BallId
import itmo.ru.puzzle.domain.model.Color
import kotlinx.serialization.Serializable

@Serializable
data class BallDTO(val ballId: Int, val color: String)

fun BallDTO.toBall() = Ball(BallId(ballId), Color.valueOf(color.uppercase()))