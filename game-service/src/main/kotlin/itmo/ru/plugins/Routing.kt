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

@JvmInline
@Serializable
value class ClientId(val value: String)
@JvmInline
@Serializable
value class GameId(val value: String)

enum class METHOD {
    @SerialName("connect")
    CONNECT,
    @SerialName("chat")
    CHAT,
    @SerialName("create")
    CREATE,
    @SerialName("join")
    JOIN;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Transfer(val method: METHOD)



@Serializable
data class ClientIdTransfer(val clientId: ClientId) : Transfer(METHOD.CONNECT)

@Serializable
data class MessageTransfer(val message: Message) : Transfer(METHOD.CHAT)

@Serializable
data class GameTransfer(val game: Game) : Transfer(METHOD.CREATE)



@Serializable
data class Game(val gameId: GameId, val balls: Int, val clients: MutableList<ClientId> = mutableListOf())

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

@Serializable
data class JoinRequest(val clientId: ClientId, val gameId: GameId)



fun getUUID() = UUID.randomUUID().toString()


val userIds = mutableListOf<ClientId>()
val gameMap = mutableMapOf<GameId, Game>()

fun Application.configureRouting() {
    routing {
        webSocket("/chat") {
            try {

                val clientID = ClientId(getUUID())
                userIds.add(clientID)

                sendSerialized(ClientIdTransfer(clientID))

                this@configureRouting.log.info("All clients connected: $userIds")

                for (frame in incoming) {
                    val data = converter?.deserialize<Transfer>(frame)!!
                    when (data.method) {
                        METHOD.CHAT -> {
                            val message = converter?.deserialize<Message>(frame)!!
                            sendSerialized(MessageTransfer(message))
                        }
                        METHOD.CREATE -> {
                            val gameId = GameId(getUUID())
                            val game = Game(gameId, 10)
                            gameMap[gameId] = game

                            sendSerialized(GameTransfer(game))
                        }
                        METHOD.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!
                            gameMap[joinRequest.gameId]!!.clients.add(joinRequest.clientId)

                            sendSerialized(GameTransfer(gameMap[joinRequest.gameId]!!))
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
