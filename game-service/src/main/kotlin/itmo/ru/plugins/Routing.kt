package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import itmo.ru.data.*
import itmo.ru.data.client.Client
import itmo.ru.data.client.ClientConnectResponse
import itmo.ru.data.client.ClientId
import itmo.ru.data.game.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule


// TODO: make client game message entities

@Serializable
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


fun getUUID() = UUID.randomUUID().toString()


val userMap = mutableMapOf<ClientId, Client>()
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
                    game.clients.forEach { client ->
                        launch {
                            this@configureRouting.log.info("Sending game update to ${client.id} for game ${game.gameId}")
                            connections[client.id]?.session?.sendSerialized(game.toUpdateResponse())
                        }
                    }
                }
            }

            timer.run()

            val clientId = ClientId(getUUID())
            val client = Client(clientId)
            userMap[clientId] = client

            val connection = Connection(this)
            connections[clientId] = connection

            try {
                sendSerialized(ClientConnectResponse(clientId))

                this@configureRouting.log.info("All clients connected: ${userMap.map { it.key }}")

                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        Method.CHAT -> {
                            val message = converter?.deserialize<Message>(frame)!!

                            sendSerialized(MessageResponse(message))
                        }

                        Method.CREATE -> {
                            val gameId = GameId(getUUID())
                            val game = Game(
                                gameId,
                                MutableList(10) { Ball(it, Color.values().random()) }
                            )

                            gameMap[gameId] = game

                            sendSerialized(game.toCreateResponse())
                        }

                        Method.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!

                            userMap[joinRequest.clientId]?.let { gameMap[joinRequest.gameId]!!.clients.add(it) }

                            connections.forEach { (clientId, connection) ->
                                if (gameMap[joinRequest.gameId]!!.clients.contains(userMap[clientId]!!)) {
                                    connection?.session?.sendSerialized(gameMap[joinRequest.gameId]!!.toJoinResponse())
                                }
                            }
                            // TODO: log maps with games and clients
                        }

                        Method.PLAY -> {
                            val setRequest = converter?.deserialize<SetRequest>(frame)!!

                            this@configureRouting.log.info(
                                "Received set request for game ${setRequest.gameId} and ball ${setRequest.ball.ballId} with color ${setRequest.ball.color} from client ${setRequest.clientId}"
                            )

                            gameMap[setRequest.gameId]!!.balls[setRequest.ball.ballId].color = setRequest.ball.color
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
                userMap.remove(clientId)
                timer.cancel()

                gameMap.values.forEach { game ->
                    game.clients.remove(client)
                }
            }
        }
    }
}