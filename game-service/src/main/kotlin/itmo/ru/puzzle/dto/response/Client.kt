package itmo.ru.puzzle.dto.response

import itmo.ru.puzzle.domain.model.Client
import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import kotlinx.serialization.Serializable

@Serializable
data class ConnectResponse(val clientId: String) : Response(Method.CONNECT)

fun Client.toConnectResponse() = ConnectResponse(id.value)