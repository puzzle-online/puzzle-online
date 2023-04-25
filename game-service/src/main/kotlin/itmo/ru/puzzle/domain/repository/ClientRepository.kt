package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Client

class ClientRepository {
    private val clientDatabase = mutableMapOf<String, Client>()

    fun get(id: String): Client? {
        return clientDatabase[id]
    }

    fun register(client: Client) {
        clientDatabase.putIfAbsent(client.id.value, client)
    }

    fun disconnect(client: Client) {
        clientDatabase.remove(client.id.value)
    }

    fun getAllClients(): List<Client> {
        return clientDatabase.values.toList()
    }

    fun contains(id: String): Boolean {
        return clientDatabase.containsKey(id)
    }
}