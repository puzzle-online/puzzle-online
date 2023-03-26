package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

enum class METHOD {
    @SerialName("connect")
    CONNECT,
    @SerialName("chat")
    CHAT,
    @SerialName("create")
    CREATE;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Transfer(val method: METHOD)



@Serializable
data class ClientIdTransfer(val clientId: String) : Transfer(METHOD.CONNECT)

@Serializable
data class MessageTransfer(val message: Message) : Transfer(METHOD.CHAT)

@Serializable
data class CreateTransfer(val game: Game) : Transfer(METHOD.CREATE)


@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

@Serializable
data class Game(val gameId: String, val balls: Int)

fun getUUID() = UUID.randomUUID().toString()

fun Application.configureRouting() {
    routing {
        webSocket("/chat") {
            try {

                val clientID = getUUID()
                sendSerialized(ClientIdTransfer(clientID))

                for (frame in incoming) {
                    val data = converter?.deserialize<Transfer>(frame)!!
                    when (data.method) {
                        METHOD.CHAT -> {
                            val message = converter?.deserialize<Message>(frame)!!
                            sendSerialized(MessageTransfer(message))
                        }
                        METHOD.CREATE -> {
                            val game = Game(getUUID(), 10)
                            sendSerialized(CreateTransfer(game))
                        }
                        else -> {
                            this@configureRouting.log.error("Unknown method")
                        }
                    }
                }

            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            }
        }
    }
}
