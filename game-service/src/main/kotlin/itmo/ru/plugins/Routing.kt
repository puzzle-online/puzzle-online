package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import itmo.ru.puzzle.dto.response.toConnectResponse
import itmo.ru.puzzle.dto.response.toCreateResponse
import itmo.ru.puzzle.dto.response.toJoinResponse
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.dto.request.*
import itmo.ru.puzzle.dto.response.toUpdateResponse
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


// TODO: make client game message entities
// TODO: remove serializable message and ball / separate file into domain
// TODO: set repos in same package

@Serializable
enum class Method {
    @SerialName("connect")
    CONNECT,

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
            val clientId = ClientId(getUUID())
            val client = Client(clientId)
            userMap[clientId] = client

            val connection = Connection(this)
            connections[clientId] = connection

//            val timer = Timer().schedule(5000, 5000) {
//                gameMap[client.gameId]?.let { game ->
//                    launch {
//                        this@configureRouting.log.info("Sending game update to ${client.id} for game ${game.id}")
//                        connections[client.id]?.session?.sendSerialized(game.toUpdateResponse())
//                    }
//                }
//            }
//            timer.run()

            try {
                sendSerialized(client.toConnectResponse())

                this@configureRouting.log.info("Clients connected on in: ${userMap.map { it.key }}")

                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        Method.CREATE -> {
                            val gameId = GameId(getUUID())

                            val game = Game(
                                gameId,
                                MutableList(10) {
                                    Ball(BallId(it), Color.values().random())
                                }
                            )

                            game.updateJob = CoroutineScope(Dispatchers.Default).launch {
                                while (isActive) {
                                    game.clients.forEach { client ->
                                        connections[client.id]?.session?.sendSerialized(game.toUpdateResponse())
                                    }
                                    delay(5000)
                                }
                            }

                            gameMap[gameId] = game

                            sendSerialized(game.toCreateResponse())
                        }

                        // TODO: on join unsubscribe from previous game
                        Method.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!

                            val user = joinRequest.toClient()
                            val game = joinRequest.toGame()

                            // TODO: handle exceptions
                            if (!gameMap.containsKey(game.id)) {
                                this@configureRouting.log.error("Game ${game.id} not found")
                                return@webSocket
                            }

                            userMap[user.id]?.let { gameMap[game.id]!!.clients.add(it) }

                            connections.forEach { (clientId, connection) ->
                                if (gameMap[game.id]!!.clients.contains(userMap[clientId]!!)) {
                                    connection?.session?.sendSerialized(gameMap[game.id]!!.toJoinResponse())
                                }
                            }
                            // TODO: log maps with games and clients
                        }

                        Method.PLAY -> {
                            val setRequest = converter?.deserialize<PlayRequest>(frame)!!

                            val game = setRequest.toGame()
                            val ball = setRequest.toBall()

                            this@configureRouting.log.info(
                                "Received set request for game ${game.id} and ball ${ball.id} with color ${ball.color} from client ${setRequest.toClient().id}"
                            )

                            // TODO: refactor .value call
                            gameMap[game.id]!!.balls[ball.id.value].color = ball.color
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

                this@configureRouting.log.info("Clients connected on out: ${userMap.map { it.key }}")

                gameMap.values.forEach { game ->
                    game.clients.remove(client)
                }
            }
        }
    }
}