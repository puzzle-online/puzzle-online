package itmo.ru.puzzle.dto

import itmo.ru.puzzle.domain.model.Cursor
import kotlinx.serialization.Serializable

@Serializable
data class CursorDTO(val x: Float, val y: Float)

fun CursorDTO.toCursor() = Cursor(x, y)

fun Cursor.toDTO() = CursorDTO(x, y)
