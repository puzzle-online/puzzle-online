package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.RoomRepository
import itmo.ru.puzzle.domain.service.GameService
import itmo.ru.puzzle.dto.request.*
import itmo.ru.puzzle.dto.toBall
import itmo.ru.puzzle.dto.toBox
import itmo.ru.puzzle.dto.toCursor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


// TODO: make client game message entities

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
    LEAVE,

    @SerialName("move")
    MOVE;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Response(val method: Method)


fun getUUID() = UUID.randomUUID().toString()


class Connection(val session: WebSocketServerSession) {
    companion object {
        // TODO: maybe set UUID here
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}

fun Application.configureRouting() {
    val roomRepository = RoomRepository()
    val clientRepository = ClientRepository()

    val gameService = GameService(roomRepository, clientRepository, log)

    routing {
        webSocket("/game") {
            val client = gameService.connect(this)

            try {
                gameService.sendConnectResponse(client, this)

                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        Method.CREATE -> {
                            val createRequest = converter!!.deserialize<CreateRequest>(frame)

                            val boxes = createRequest.boxes.map { it.toBox() }

                            gameService.create(client, boxes, this)
                        }

                        // TODO: on join unsubscribe from previous game
                        // TODO: don't send clientId in JoinRequest
                        Method.JOIN -> {
                            val joinRequest = converter!!.deserialize<JoinRequest>(frame)

                            gameService.join(joinRequest.clientId, joinRequest.roomId, this)
                        }

                        Method.PLAY -> {
                            val playRequest = converter!!.deserialize<PlayRequest>(frame)

                            // TODO: maybe remove this
                            val ball = playRequest.ball.toBall()

                            this@configureRouting.log.info(
                                "Received set request for room ${playRequest.roomId} and ball ${ball.id} with color ${ball.color} from client ${playRequest.clientId}"
                            )

                            gameService.play(playRequest.roomId, ball, this)
                        }

                        Method.ROOMS -> {
                            gameService.getRooms(this)
                        }

                        Method.LEAVE -> {
                            val leaveRequest = converter!!.deserialize<LeaveRequest>(frame)

                            this@configureRouting.log.info(
                                "Received leave request for room ${leaveRequest.roomId} from client ${leaveRequest.clientId}"
                            )

                            gameService.leave(leaveRequest.clientId, leaveRequest.roomId, this)
                        }

                        Method.MOVE -> {
                            val moveRequest = converter!!.deserialize<MoveRequest>(frame)

                            val box = moveRequest.box?.toBox()
                            val cursor = moveRequest.cursor.toCursor()

                            this@configureRouting.log.info(
                                "Received move request $moveRequest"
                            )

                            gameService.handleMove(moveRequest.clientId, moveRequest.roomId, cursor, box, this)
                        }

                        else -> this@configureRouting.log.error("Unknown method ${data.method}")
                    }
                }
            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            } finally {
                gameService.disconnect(client)
            }
        }
    }
}
