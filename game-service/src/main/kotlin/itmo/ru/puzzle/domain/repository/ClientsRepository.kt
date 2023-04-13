package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Client
import itmo.ru.puzzle.domain.model.ClientId

class ClientsRepository {
    private val client = mutableMapOf<ClientId, Client>()

    fun get(id: ClientId): Client? {
        return client[id]
    }

    fun register(client: Client) {
        this.client.putIfAbsent(client.id, client)
    }

    fun disconnect(id: ClientId) {
        client.remove(id)
    }
}