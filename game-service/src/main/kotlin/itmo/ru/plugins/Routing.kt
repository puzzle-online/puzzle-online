package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.network.sockets.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import itmo.ru.data.Ball
import itmo.ru.data.Color
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule

@JvmInline
@Serializable
value class ClientId(val value: String)

@JvmInline
@Serializable
value class GameId(val value: String)

enum class Method {
    @SerialName("connect")
    CONNECT,

    @SerialName("chat")
    CHAT,

    @SerialName("create")
    CREATE,

    @SerialName("join")
    JOIN,

    @SerialName("play")
    PLAY,

    @SerialName("update")
    UPDATE;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Response(val method: Method)


@Serializable
data class ClientIdResponse(val clientId: ClientId) : Response(Method.CONNECT)

@Serializable
data class MessageResponse(val message: Message) : Response(Method.CHAT)

@Serializable
data class CreateGameResponse(val game: Game) : Response(Method.CREATE)

@Serializable
data class JoinGameResponse(val game: Game) : Response(Method.JOIN)

@Serializable
data class GameUpdateResponse(val game: Game) : Response(Method.UPDATE)


@Serializable
data class Game(
    val gameId: GameId,
    val balls: MutableList<Color> = mutableListOf(),
    val clients: MutableSet<ClientId> = mutableSetOf(),
)

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)

@Serializable
data class JoinRequest(val clientId: ClientId, val gameId: GameId)

@Serializable
data class SetRequest(val clientId: ClientId, val gameId: GameId, val ball: Ball)


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

            val timer = Timer().schedule(5000, 5000) {
                gameMap.values.forEach { game ->
                    game.clients.forEach { clientId ->
                        launch {
                            this@configureRouting.log.info("Sending game update to $clientId for game ${game.gameId}")
                            connections[clientId]?.session?.sendSerialized(GameUpdateResponse(game))
                        }
                    }
                }
            }

            timer.run()

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
                        Method.CHAT -> {
                            val message = converter?.deserialize<Message>(frame)!!
                            sendSerialized(MessageResponse(message))
                        }

                        Method.CREATE -> {
                            val gameId = GameId(getUUID())
                            val game = Game(gameId, MutableList(10) { Color.values().random() })
                            gameMap[gameId] = game

                            sendSerialized(CreateGameResponse(game))
                        }

                        Method.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!
                            gameMap[joinRequest.gameId]!!.clients.add(joinRequest.clientId)

                            connections.forEach { (clientId, connection) ->
                                if (gameMap[joinRequest.gameId]!!.clients.contains(clientId)) {
                                    connection?.session?.sendSerialized(JoinGameResponse(gameMap[joinRequest.gameId]!!))
                                }
                            }
                        }

                        Method.PLAY -> {
                            val setRequest = converter?.deserialize<SetRequest>(frame)!!

                            this@configureRouting.log.info(
                                "Received set request for game ${setRequest.gameId} and ball ${setRequest.ball.ballId} with color ${setRequest.ball.color} from client ${setRequest.clientId}"
                            )

                            gameMap[setRequest.gameId]!!.balls[setRequest.ball.ballId] = setRequest.ball.color
                        }

                        else -> {
                            this@configureRouting.log.error("Unknown method")
                        }
                    }
//                    timer("set", false, 0, 5000) {
//                        gameMap.values.forEach { game ->
//                            game.clients.forEach { clientId ->
//                                connections[clientId]?.session?.sendSerialized(GameResponse(game))
//                            }
//                        }
//                    }
                }
            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            } finally {
                connections.remove(clientId)
                userIds.remove(clientId)
                timer.cancel()
            }
        }
    }
}