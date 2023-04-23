package itmo.ru.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.RoomRepository
import itmo.ru.puzzle.dto.request.*
import itmo.ru.puzzle.dto.response.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule


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


fun getUUID() = UUID.randomUUID().toString()


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
        val roomRepository = RoomRepository()
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
                            val roomId = RoomId(getUUID())

                            val room = Room(
                                roomId,
                                MutableList(10) {
                                    Ball(BallId(it), Color.values().random())
                                },
                                mutableSetOf(currentSessionClient)
                            )

                            room.updateJob = CoroutineScope(Dispatchers.Default).launch {
                                while (isActive) {
                                    room.clients.forEach { client ->
                                        connections[client.id]?.session?.sendSerialized(room.toUpdateResponse())
                                    }
                                    delay(5000)
                                }
                            }

                            roomRepository.put(room)

                            sendSerialized(room.toCreateResponse())
                        }

                        // TODO: on join unsubscribe from previous game
                        // TODO: don't send clientId in JoinRequest
                        Method.JOIN -> {
                            val joinRequest = converter?.deserialize<JoinRequest>(frame)!!

                            if (!roomRepository.contains(joinRequest.roomId)) {
                                this@configureRouting.log.error("Room ${joinRequest.roomId} not found")
                                return@webSocket
                            }

                            if (!clientRepository.contains(joinRequest.clientId)) {
                                this@configureRouting.log.error("Client ${joinRequest.clientId} not found")
                                return@webSocket
                            }

                            val room = roomRepository.get(joinRequest.roomId)!!
                            val client = clientRepository.get(joinRequest.clientId)!!

                            if (room.clients.isEmpty()) {
                                room.deleteRoomActionTimer.cancel()
                            }
                            room.clients.add(client)

                            sendSerialized(room.toJoinResponse())
                        }

                        Method.PLAY -> {
                            val playRequest = converter?.deserialize<PlayRequest>(frame)!!

                            // TODO: maybe remove this
                            val ball = playRequest.toBall()

                            this@configureRouting.log.info(
                                "Received set request for room ${playRequest.roomId} and ball ${ball.id} with color ${ball.color} from client ${playRequest.clientId}"
                            )

                            if (!roomRepository.contains(playRequest.roomId)) {
                                this@configureRouting.log.error("Room ${playRequest.roomId} not found")
                                return@webSocket
                            }

                            val room = roomRepository.get(playRequest.roomId)!!

                            // TODO: refactor .value call
                            room.balls[ball.id.value].color = ball.color
                        }

                        Method.ROOMS -> {
                            val rooms = roomRepository.getAllRooms().map { it.toGetRoomDescriptionResponse() }.toList()
                            sendSerialized(GetRoomsResponse(rooms))
                        }

                        Method.LEAVE -> {
                            val leaveRequest = converter?.deserialize<LeaveRequest>(frame)!!

                            this@configureRouting.log.info(
                                "Received leave request for room ${leaveRequest.roomId} from client ${leaveRequest.clientId}"
                            )

                            if (!clientRepository.contains(leaveRequest.clientId)) {
                                this@configureRouting.log.error("Client ${leaveRequest.clientId} not found")
                                return@webSocket
                            }

                            if (!roomRepository.contains(leaveRequest.roomId)) {
                                this@configureRouting.log.error("Room ${leaveRequest.roomId} not found")
                                return@webSocket
                            }

                            val room = roomRepository.get(leaveRequest.roomId)!!
                            val client = clientRepository.get(leaveRequest.clientId)!!

                            this@configureRouting.log.debug("Clients before removing {}: {}", client.id, room.clients)

                            room.clients.remove(client)

                            this@configureRouting.log.debug("Clients after removing {}: {}", client.id, room.clients)

                            if (room.clients.isEmpty()) {

                                this@configureRouting.log.info("No clients for ${room.id}. Prepare to set timer...")

                                room.deleteRoomActionTimer = Timer()
                                room.deleteRoomActionTimer.schedule(5000) {

                                    this@configureRouting.log.info("Performing delete room action for room ${room.id}")

                                    room.updateJob.cancel()
                                    roomRepository.remove(room)
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

                roomRepository.getAllRooms().forEach { room ->

                    room.clients.remove(currentSessionClient)
                    if (room.clients.isEmpty()) {

                        this@configureRouting.log.info("No clients for ${room.id}. Prepare to set timer...")

                        room.deleteRoomActionTimer = Timer()
                        room.deleteRoomActionTimer.schedule(5000) {

                            this@configureRouting.log.info("Performing delete room action for room ${room.id}")

                            room.updateJob.cancel()
                            roomRepository.remove(room)
                        }
                    }
                }
            }
        }
    }
}
