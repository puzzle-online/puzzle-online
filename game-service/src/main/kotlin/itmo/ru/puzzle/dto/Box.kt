package itmo.ru.puzzle.dto

import itmo.ru.puzzle.domain.model.Box
import itmo.ru.puzzle.domain.model.State
import kotlinx.serialization.Serializable

@Serializable
data class BoxDTO(val id: Int, val x: Float, val y: Float, val z: Int, val state: String)

fun BoxDTO.toBox() = Box(id, x, y, z, State.valueOf(state.uppercase()))

fun Box.toBoxDTO() = BoxDTO(id, x, y, z, state.name.lowercase())

fun MutableSet<Box>.toResponse() = this.map { it.toBoxDTO() }.toList()