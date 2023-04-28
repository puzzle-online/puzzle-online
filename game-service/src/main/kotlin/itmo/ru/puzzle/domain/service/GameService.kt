package itmo.ru.puzzle.domain.service

import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
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

    fun handleMove(
        clientId: String,
        roomId: String,
        cursor: Cursor,
        box: Box?,
    ) {
        val room = roomRepository.get(roomId)
        val client = clientRepository.get(clientId)

        if (room == null || client == null) {
            logger.error("Room or client not found")
            return
        }

        client.cursor = cursor
        if (box != null) {
            room.boxes.find { it.id == box.id }?.let {
                it.x = box.x
                it.y = box.y
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
                delay(500)
            }
        }

        roomRepository.put(room)

        session.sendSerialized(room.toCreateResponse())
    }

    suspend fun join(clientId: String, roomId: String, session: WebSocketServerSession) {
        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
        }

        if (!clientRepository.contains(clientId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client $clientId not found"))
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
        }

        val room = roomRepository.get(roomId)!!

        // TODO: refactor .value call
        room.balls[ball.id.value].color = ball.color
    }

    suspend fun leave(clientId: String, roomId: String, session: WebSocketServerSession) {
        if (!clientRepository.contains(clientId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client $clientId not found"))
        }

        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
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