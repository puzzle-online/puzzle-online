package itmo.ru.puzzle.domain.service

import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import itmo.ru.puzzle.domain.model.*
import itmo.ru.puzzle.domain.repository.ClientRepository
import itmo.ru.puzzle.domain.repository.RoomRepository
import itmo.ru.puzzle.dto.response.*
import itmo.ru.puzzle.getUUID
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.schedule

// TODO: consider multithreaded data access
class GameService(
    private val roomRepository: RoomRepository,
    private val clientRepository: ClientRepository,
    private val logger: Logger,
) {
    /**
     * Associates a session ID to a set of websockets.
     * Since a browser is able to open several tabs and windows with the same cookies and thus the same session.
     * There might be several opened sockets for the same client.
     */
    private val sessions = ConcurrentHashMap<ClientId, MutableList<WebSocketServerSession>>()

    /**
     * Handles that a member is identified by a session ID and a socket joined.
     */
    suspend fun memberJoin(clientId: ClientId, session: WebSocketServerSession) {
        val client = Client(clientId)

        clientRepository.register(client)

        val connections = sessions.computeIfAbsent(clientId) { CopyOnWriteArrayList() }
        connections.add(session)

        logger.info("Client connected: $client")

        session.sendSerialized(ConnectResponse.of(client))

        logger.info("All connected clients: ${clientRepository.getAllClients()}")
    }

    suspend fun create(clientId: ClientId, session: WebSocketServerSession) {
        val client = clientRepository.get(clientId)

        if (client == null) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Client not found"))
            return
        }

        val roomId = RoomId(getUUID())

        val room = Room(
            roomId,
            MutableList(10) {
                Ball(BallId(it), Color.values().random())
            },
            mutableSetOf(client)
        )

        room.updateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                room.clients.forEach { client ->
                    sessions[client.id]!!.forEach { session ->
                        session.sendSerialized(UpdateResponse.of(room))
                    }
                }
                delay(5000)
            }
        }

        roomRepository.put(room)

        session.sendSerialized(room.toCreateResponse())
    }

    suspend fun join(clientId: ClientId, roomId: RoomId, session: WebSocketServerSession) {
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

        logger.info("Client $clientId joined room $roomId")

        session.sendSerialized(room.toJoinResponse())
    }

    suspend fun play(roomId: RoomId, ball: Ball, session: WebSocketServerSession) {
        if (!roomRepository.contains(roomId)) {
            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room $roomId not found"))
            return
        }

        val room = roomRepository.get(roomId)!!

        // TODO: refactor .value call
        room.balls[ball.id.value].color = ball.color
    }

    suspend fun getRooms(session: WebSocketServerSession) {
        val rooms = roomRepository.getAllRooms().map { it.toGetRoomDescriptionResponse() }

        session.sendSerialized(GetRoomsResponse(rooms))
    }

    suspend fun leave(clientId: ClientId, roomId: RoomId, session: WebSocketServerSession) {
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

    fun disconnect(clientId: ClientId) {
        if (sessions[clientId] == null) {
            return
        }

        sessions.remove(clientId)

        val client = clientRepository.get(clientId)!!

        clientRepository.disconnect(client)

        logger.info("Clients connected on out: ${clientRepository.getAllClients()}")

        roomRepository.getAllRooms().forEach { room ->
            room.removeClient(client)
        }

        logger.info("Rooms after removing client $clientId: ${roomRepository.getAllRooms()}")
    }

    private fun Room.removeClient(client: Client) {
        logger.debug("Clients before removing {}: {}", client.id, this.clients)

        this.clients.remove(client)

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
