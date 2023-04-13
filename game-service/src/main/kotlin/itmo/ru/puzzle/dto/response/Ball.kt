package itmo.ru.puzzle.dto.response

import itmo.ru.puzzle.domain.model.Ball
import kotlinx.serialization.Serializable

@Serializable
data class BallResponse(val ballId: Int, val color: String)

fun Ball.toResponse() = BallResponse(id.value, color.name.lowercase())