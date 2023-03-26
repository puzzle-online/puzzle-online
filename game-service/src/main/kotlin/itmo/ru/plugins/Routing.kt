package itmo.ru.plugins

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

enum class METHOD { CONNECT, CHAT }


// TODO: think about sealed class
@Serializable
open class Transfer(val method: METHOD)

@Serializable
data class UUIDTransfer(val uuid: String) : Transfer(METHOD.CONNECT)

@Serializable
data class MessageTransfer(val message: Message) : Transfer(METHOD.CHAT)

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

fun getUUID() = java.util.UUID.randomUUID().toString()

fun Application.configureRouting() {
    routing {
        webSocket("/chat") {
            try {

                val clientID = getUUID()
                sendSerialized(UUIDTransfer(clientID))

                while (true) {
                    for (frame in incoming) {
                        val data = converter?.deserialize<Message>(frame)!!
                        sendSerialized(MessageTransfer(data))
                    }
                    delay(5000)
                }

            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            }
        }
    }
}
