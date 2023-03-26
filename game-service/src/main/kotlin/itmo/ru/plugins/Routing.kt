package itmo.ru.plugins

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

fun getUUID() = java.util.UUID.randomUUID().toString()

fun Application.configureRouting() {
    routing {
        webSocket("/chat") {
            try {
                sendSerialized(getUUID())
                for (frame in incoming) {
                    val data = converter?.deserialize<Message>(frame)!!
                    sendSerialized(data)
                }
            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            }
        }
    }
}
