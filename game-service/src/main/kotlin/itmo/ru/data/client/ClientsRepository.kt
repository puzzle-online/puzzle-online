package itmo.ru.data.client

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