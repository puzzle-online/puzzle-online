package itmo.ru.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Color {
    @SerialName("red")
    RED,
    @SerialName("blue")
    BLUE,
    @SerialName("green")
    GREEN;
}

@Serializable
data class Ball(val ballId: Int, val color: Color)