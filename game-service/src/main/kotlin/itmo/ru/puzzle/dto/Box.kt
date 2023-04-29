package itmo.ru.puzzle.dto

import itmo.ru.puzzle.domain.model.Box
import itmo.ru.puzzle.domain.model.State
import kotlinx.serialization.Serializable

@Serializable
data class BoxDTO(val id: Int, val x: Float, val y: Float, val state: String = "released")

fun BoxDTO.toBox() = Box(id, x, y, State.valueOf(state.uppercase()))

fun Box.toBoxDTO() = BoxDTO(id, x, y, state.name.lowercase())

fun MutableSet<Box>.toResponse() = this.map { it.toBoxDTO() }.toList()