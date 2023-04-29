package itmo.ru.puzzle.domain.service

import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import itmo.ru.ROOM_UPDATE_RESPONSE_INTERVAL
import itmo.ru.plugins.Connection
import itmo.ru.plugins.getUUID
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.RoomRepository
import itmo.ru.puzzle.dto.response.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule

class GameService(
    private val roomRepository: RoomRepository,
    private val clientRepository: ClientRepository,
    private val logger: Logger,
) {
    private val connections = Collections.synchronizedMap<ClientId, Connection?>(mutableMapOf())

    fun connect(session: WebSocketServerSession): Client {
        val client = Client(ClientId(getUUID()), null)
        clientRepository.register(client)
        connections[client.id] = Connection(session)
        return client
    }

    suspend fun sendConnectResponse(client: Client, session: WebSocketServerSession) {
        logger.info("Client connected: $client")

        session.sendSerialized(client.toConnectResponse())

        logger.info("All connected clients: ${clientRepository.getAllClients()}")
    }

    suspend fun handleMove(
        clientId: String,
        roomId: String,
        updateCursor: Cursor,
        updateBox: Box?,
        session: WebSocketServerSession
    ) {
        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
            return
        }

        if (!clientRepository.contains(clientId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client $clientId not found"))
            return
        }

        val room = roomRepository.get(roomId)!!
        val client = clientRepository.get(clientId)!!

        client.cursor = updateCursor
        if (updateBox != null) {
            room.boxes.find { it.id == updateBox.id }?.let { serverBox ->
                when (updateBox.state) {
                    State.MOVING -> {
                        serverBox.x = updateBox.x
                        serverBox.y = updateBox.y
                        serverBox.state = updateBox.state
                    }

                    State.RELEASED -> {
                        if (updateBox.isCorrectlyPlaced) {
                            serverBox.x = serverBox.correctX
                            serverBox.y = serverBox.correctY
                            serverBox.state = State.SOLVED
                        } else {
                            serverBox.x = updateBox.x
                            serverBox.y = updateBox.y
                            serverBox.state = updateBox.state
                        }
                    }

                    State.SOLVED -> {
                        // do nothing
                    }
                }
            }
        }
    }

    suspend fun create(client: Client, boxes: List<Box>, session: WebSocketServerSession) {
        val roomId = RoomId(getUUID())

        client.cursor = Cursor(0f, 0f)

        val room = Room(
            roomId,
            MutableList(10) {
                Ball(BallId(it), Color.values().random())
            },
            mutableSetOf(client),
            boxes.toMutableSet(),
        )

        room.updateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                room.clients.forEach { client ->
                    connections[client.id]?.session?.sendSerialized(room.toUpdateResponse())
                }
                delay(ROOM_UPDATE_RESPONSE_INTERVAL)
            }
        }

        roomRepository.put(room)

        session.sendSerialized(room.toCreateResponse())
    }

    suspend fun join(clientId: String, roomId: String, session: WebSocketServerSession) {
        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
            return
        }

        if (!clientRepository.contains(clientId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client $clientId not found"))
            return
        }

        val room = roomRepository.get(roomId)!!
        val client = clientRepository.get(clientId)!!

        if (room.clients.isEmpty()) {
            room.deleteRoomActionTimer.cancel()
        }
        room.clients.add(client)
        client.cursor = Cursor(0f, 0f)

        session.sendSerialized(room.toJoinResponse())
    }

    suspend fun play(roomId: String, ball: Ball, session: WebSocketServerSession) {
        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
            return
        }

        val room = roomRepository.get(roomId)!!

        // TODO: refactor .value call
        room.balls[ball.id.value].color = ball.color
    }

    suspend fun leave(clientId: String, roomId: String, session: WebSocketServerSession) {
        if (!clientRepository.contains(clientId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client $clientId not found"))
            return
        }

        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
            return
        }

        val room = roomRepository.get(roomId)!!
        val client = clientRepository.get(clientId)!!

        room.removeClient(client)
    }

    suspend fun getRooms(session: WebSocketServerSession) {
        val rooms = roomRepository.getAllRooms().map { it.toGetRoomDescriptionResponse() }.toList()
        session.sendSerialized(GetRoomsResponse(rooms))
    }

    fun disconnect(client: Client) {
        logger.info("Client ${client.id} disconnected")

        connections.remove(client.id)
        clientRepository.disconnect(client)

        logger.info("Clients connected on out: ${clientRepository.getAllClients()}")

        roomRepository.getAllRooms().forEach { room ->
            room.removeClient(client)
        }
    }

    private fun Room.removeClient(client: Client) {
        logger.debug("Clients before removing {}: {}", client.id, this.clients)

        this.clients.remove(client)
        client.cursor = null

        logger.debug("Clients after removing {}: {}", client.id, this.clients)

        if (this.clients.isEmpty()) {
            logger.info("No clients for ${this.id}. Prepare to set timer...")

            this.deleteRoomActionTimer = Timer()
            this.deleteRoomActionTimer.schedule(5000) {

                logger.info("Performing delete room action for room ${this@removeClient.id}")

                this@removeClient.updateJob.cancel()
                roomRepository.remove(this@removeClient)
            }
        }
    }
}