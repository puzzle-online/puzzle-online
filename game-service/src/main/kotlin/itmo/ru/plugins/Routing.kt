package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.GameRepository
import itmo.ru.puzzle.dto.request.*
import itmo.ru.puzzle.dto.response.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule


// TODO: make client game message entities
// TODO: set repos in same package

// TODO: divide into request and response
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
    UPDATE,

    @SerialName("rooms")
    ROOMS,

    @SerialName("leave")
    LEAVE;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Response(val method: Method)


fun getUUID() = UUID.randomUUID().toString()


//val userMap = mutableMapOf<ClientId, Client>()
//val gameMap = mutableMapOf<GameId, Game>()

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
        val gameRepository = GameRepository()
        val clientRepository = ClientRepository()

        webSocket("/chat") {
            val clientId = ClientId(getUUID())
            val currentSessionClient = Client(clientId)
            clientRepository.register(currentSessionClient)

            val connection = Connection(this)
            connections[clientId] = connection

            try {
                sendSerialized(currentSessionClient.toConnectResponse())

                this@configureRouting.log.info("Clients connected on in: ${clientRepository.getAllClients()}")

                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        Method.CREATE -> {
                            val gameId = GameId(getUUID())

                            val game = Game(
                                gameId,
                                MutableList(10) {
                                    Ball(BallId(it), Color.values().random())
                                },
                                mutableSetOf(currentSessionClient)
                            )

                            game.updateJob = CoroutineScope(Dispatchers.Default).launch {
                                while (isActive) {
                                    game.clients.forEach { client ->
                                        connections[client.id]?.session?.sendSerialized(game.toUpdateResponse())
                                    }
                                    delay(5000)
                                }
                            }

                            gameRepository.put(game)

                            sendSerialized(game.toCreateResponse())
                        }

                        // TODO: on join unsubscribe from previous game
                        // TODO: don't send clientId in JoinRequest
                        Method.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!

                            if (!gameRepository.contains(joinRequest.gameId)) {
                                this@configureRouting.log.error("Game ${joinRequest.gameId} not found")
                                return@webSocket
                            }

                            if (!clientRepository.contains(joinRequest.clientId)) {
                                this@configureRouting.log.error("Client ${joinRequest.clientId} not found")
                                return@webSocket
                            }

                            val gameModel = gameRepository.get(joinRequest.gameId)!!
                            val clientModel = clientRepository.get(joinRequest.clientId)!!

                            if (gameModel.clients.isEmpty()) {
                                gameModel.deleteGameActionTimer.cancel()
                            }
                            gameModel.clients.add(clientModel)

                            sendSerialized(gameModel.toJoinResponse())
                        }

                        Method.PLAY -> {
                            val playRequest = converter?.deserialize<PlayRequest>(frame)!!

                            // TODO: maybe remove this
                            val ball = playRequest.toBall()

                            this@configureRouting.log.info(
                                "Received set request for game ${playRequest.gameId} and ball ${ball.id} with color ${ball.color} from client ${playRequest.clientId}"
                            )

                            if (!gameRepository.contains(playRequest.gameId)) {
                                this@configureRouting.log.error("Game ${playRequest.gameId} not found")
                                return@webSocket
                            }

                            val gameModel = gameRepository.get(playRequest.gameId)!!

                            // TODO: refactor .value call
                            gameModel.balls[ball.id.value].color = ball.color
                        }

                        Method.ROOMS -> {
                            val rooms = gameRepository.getAllGames().map { it.toGetGameDescriptionResponse() }.toList()
                            sendSerialized(GetGamesResponse(rooms))
                        }

                        Method.LEAVE -> {
                            val leaveRequest = converter?.deserialize<LeaveRequest>(frame)!!

                            this@configureRouting.log.info(
                                "Received leave request for game ${leaveRequest.gameId} from client ${leaveRequest.clientId}"
                            )

                            if (!clientRepository.contains(leaveRequest.clientId)) {
                                this@configureRouting.log.error("Client ${leaveRequest.clientId} not found")
                                return@webSocket
                            }

                            if (!gameRepository.contains(leaveRequest.gameId)) {
                                this@configureRouting.log.error("Game ${leaveRequest.gameId} not found")
                                return@webSocket
                            }

                            val game = gameRepository.get(leaveRequest.gameId)!!
                            val client = clientRepository.get(leaveRequest.clientId)!!

                            this@configureRouting.log.debug("Clients before removing {}: {}", client.id, game.clients)

                            game.clients.remove(client)

                            this@configureRouting.log.debug("Clients after removing {}: {}", client.id, game.clients)

                            if (game.clients.isEmpty()) {

                                this@configureRouting.log.info("No clients for ${game.id}. Prepare to set timer...")

                                game.deleteGameActionTimer = Timer()
                                game.deleteGameActionTimer.schedule(5000) {

                                    this@configureRouting.log.info("Performing delete game action for game ${game.id}")

                                    game.updateJob.cancel()
                                    gameRepository.remove(game)
                                }
                            }
                        }

                        else -> this@configureRouting.log.error("Unknown method ${data.method}")
                    }
                }
            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            } finally {
                this@configureRouting.log.info("Client $clientId disconnected")

                connections.remove(clientId)
                clientRepository.disconnect(currentSessionClient)

                this@configureRouting.log.info("Clients connected on out: ${clientRepository.getAllClients()}")

                gameRepository.getAllGames().forEach { gameModel ->

                    gameModel.clients.remove(currentSessionClient)
                    if (gameModel.clients.isEmpty()) {

                        this@configureRouting.log.info("No clients for ${gameModel.id}. Prepare to set timer...")

                        gameModel.deleteGameActionTimer = Timer()
                        gameModel.deleteGameActionTimer.schedule(5000) {

                            this@configureRouting.log.info("Performing delete game action for game ${gameModel.id}")

                            gameModel.updateJob.cancel()
                            gameRepository.remove(gameModel)
                        }
                    }
                }
            }
        }
    }
}