package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.network.sockets.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
open class Response(val method: METHOD)


@Serializable
data class ClientIdResponse(val clientId: ClientId) : Response(METHOD.CONNECT)

@Serializable
data class MessageResponse(val message: Message) : Response(METHOD.CHAT)

@Serializable
data class GameResponse(val game: Game) : Response(METHOD.CREATE)


@Serializable
data class Game(val gameId: GameId, val balls: Int, val clients: MutableSet<ClientId> = mutableSetOf())

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

@Serializable
data class JoinRequest(val clientId: ClientId, val gameId: GameId)


fun getUUID() = UUID.randomUUID().toString()


val userIds = mutableListOf<ClientId>()
val gameMap = mutableMapOf<GameId, Game>()

class Connection(val session: WebSocketServerSession) {
    companion object {
        // TODO: maybe set UUID here
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}

fun Application.configureRouting() {
    routing {
        val connections = Collections.synchronizedMap<ClientId, Connection?>(mutableMapOf())

        webSocket("/chat") {

            val clientId = ClientId(getUUID())
            userIds.add(clientId)

            val connection = Connection(this)
            connections[clientId] = connection

            try {
                sendSerialized(ClientIdResponse(clientId))

                this@configureRouting.log.info("All clients connected: $userIds")

                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        METHOD.CHAT -> {
                            val message = converter?.deserialize<Message>(frame)!!
                            sendSerialized(MessageResponse(message))
                        }

                        METHOD.CREATE -> {
                            val gameId = GameId(getUUID())
                            val game = Game(gameId, 10)
                            gameMap[gameId] = game

                            sendSerialized(GameResponse(game))
                        }

                        METHOD.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!
                            gameMap[joinRequest.gameId]!!.clients.add(joinRequest.clientId)

                            connections.forEach { (clientId, connection) ->
                                if (gameMap[joinRequest.gameId]!!.clients.contains(clientId)) {
                                    connection?.session?.sendSerialized(GameResponse(gameMap[joinRequest.gameId]!!))
                                }
                            }

                            sendSerialized(GameResponse(gameMap[joinRequest.gameId]!!))
                        }

                        else -> {
                            this@configureRouting.log.error("Unknown method")
                        }
                    }
                }

            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            } finally {
                connections.remove(clientId)
                userIds.remove(clientId)
            }
        }
    }
}
