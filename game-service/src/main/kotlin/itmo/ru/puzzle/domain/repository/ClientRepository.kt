package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Client
import itmo.ru.puzzle.domain.model.ClientId

class ClientRepository {
    private val clientDatabase = mutableMapOf<ClientId, Client>()

    fun get(id: ClientId): Client? {
        return clientDatabase[id]
    }

    fun register(client: Client) {
        clientDatabase.putIfAbsent(client.id, client)
    }

    fun disconnect(client: Client) {
        clientDatabase.remove(client.id)
    }

    fun getAllClients(): List<Client> {
        return clientDatabase.values.toList()
    }

    fun contains(id: ClientId): Boolean {
        return clientDatabase.containsKey(id)
    }
}
