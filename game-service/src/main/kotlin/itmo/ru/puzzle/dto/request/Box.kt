package itmo.ru.puzzle.dto.request

import itmo.ru.puzzle.domain.model.Box
import itmo.ru.puzzle.domain.model.State
import kotlinx.serialization.Serializable

@Serializable
data class BoxRequestDTO(val id: Int, val x: Float, val y: Float, val state: String = "released")

fun BoxRequestDTO.toBox() = Box(id, x, y, State.valueOf(state.uppercase()))