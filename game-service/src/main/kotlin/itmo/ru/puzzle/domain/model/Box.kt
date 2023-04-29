package itmo.ru.puzzle.domain.model

import kotlin.math.absoluteValue

// TODO: add state IN_PLACE
enum class State { MOVING, RELEASED, SOLVED }

data class Box(val id: Int, var x: Float, var y: Float, var z: Int, var state: State) {
    // TODO: remove hardcoded values
    var correctX: Float = 400F + (id % 4) * 100
    var correctY: Float = 200F + (id / 4) * 100
    val isCorrectlyPlaced: Boolean
        get() = when (state) {
            State.MOVING -> false
            State.RELEASED -> {
                val dx = x - correctX
                val dy = y - correctY
                dx.absoluteValue < 10 && dy.absoluteValue < 10
            }
            State.SOLVED -> true
        }
}