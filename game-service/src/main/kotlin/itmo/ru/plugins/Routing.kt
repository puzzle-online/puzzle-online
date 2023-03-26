package itmo.ru.plugins

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Message(val method: METHOD, val data: String)

enum class METHOD { CONNECT }

@Serializable
data class UUID(val uuid: String)

fun getUUID() = java.util.UUID.randomUUID().toString()

fun Application.configureRouting() {
    routing {
        webSocket("/chat") {
            try {

                val clientID = UUID(getUUID())
                val connectData = Json.encodeToString(clientID)
                sendSerialized(Message(METHOD.CONNECT, connectData))

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
