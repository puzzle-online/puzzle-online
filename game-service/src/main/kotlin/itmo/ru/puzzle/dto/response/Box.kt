package itmo.ru.puzzle.dto.response

import itmo.ru.puzzle.domain.model.Box
import kotlinx.serialization.Serializable

@Serializable
data class BoxResponseDTO(val id: Int, val x: Float, val y: Float, val isCorrectlyPlaced: Boolean)

fun Box.toResponseDTO() = BoxResponseDTO(id, x, y, isCorrectlyPlaced)

fun MutableSet<Box>.toResponse() = this.map { it.toResponseDTO() }.toList()
