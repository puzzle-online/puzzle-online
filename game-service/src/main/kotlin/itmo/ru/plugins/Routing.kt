package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import itmo.ru.puzzle.domain.model.ClientId
import itmo.ru.puzzle.domain.model.RoomId
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.RoomRepository
import itmo.ru.puzzle.domain.service.GameService
import itmo.ru.puzzle.dto.request.JoinRequest
import itmo.ru.puzzle.dto.request.LeaveRequest
import itmo.ru.puzzle.dto.request.PlayRequest
import itmo.ru.puzzle.dto.request.toBall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*


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
    LEAVE;

    override fun toString() = name.lowercase(Locale.getDefault())
}


// TODO: think about sealed class
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
open class Response(val method: Method)

fun Application.configureRouting() {
    val roomRepository = RoomRepository()
    val clientRepository = ClientRepository()

    val gameService = GameService(roomRepository, clientRepository, log)

    routing {
        webSocket("/game") {
            val session = call.sessions.get<Session>()

            if (session == null) {
                this@configureRouting.log.info("No session")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            gameService.memberJoin(session.id, this)

            try {
                for (frame in incoming) {
                    val data = converter?.deserialize<Response>(frame)!!
                    when (data.method) {
                        Method.CREATE -> gameService.create(session.id, this)

                        // TODO: don't send clientId in JoinRequest
                        Method.JOIN -> {
                            val joinRequest = converter!!.deserialize<JoinRequest>(frame)

                            this@configureRouting.log.info(
                                "Received join request from client $session: room ${joinRequest.roomId}"
                            )

                            gameService.join(session.id, RoomId.of(joinRequest.roomId), this)
                        }

                        Method.PLAY -> {
                            val playRequest = converter!!.deserialize<PlayRequest>(frame)

                            val ball = playRequest.toBall()

                            this@configureRouting.log.info(
                                "Received set request from client $session: room ${playRequest.roomId}, ball index ${ball.id}, color ${ball.color}"
                            )

                            gameService.play(RoomId.of(playRequest.roomId), ball, this)
                        }

                        Method.ROOMS -> {
                            this@configureRouting.log.info("Received rooms request from client $session")

                            gameService.getRooms(this)
                        }

                        Method.LEAVE -> {
                            val leaveRequest = converter!!.deserialize<LeaveRequest>(frame)

                            this@configureRouting.log.info(
                                "Received leave request from client $session: room ${leaveRequest.roomId}"
                            )

                            gameService.leave(session.id, RoomId.of(leaveRequest.roomId), this)
                        }

                        else -> this@configureRouting.log.error("Unknown method ${data.method}")
                    }
                }
            } catch (e: Exception) {
                this@configureRouting.log.error("Error occurred in websocket", e)
            } finally {
                gameService.disconnect(session.id)
            }
        }
    }
}

data class Session(val id: ClientId)
