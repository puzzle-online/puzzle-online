package itmo.ru.data.client

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ClientId(val value: String)

@Serializable
data class Client(val id: ClientId)

@Serializable
data class ClientConnectResponse(val clientId: ClientId) : Response(Method.CONNECT)

fun Client.toConnectResponse() = ClientConnectResponse(clientId = id)