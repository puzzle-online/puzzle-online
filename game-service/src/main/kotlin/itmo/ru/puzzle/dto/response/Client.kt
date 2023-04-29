package itmo.ru.puzzle.dto.response

import itmo.ru.puzzle.domain.model.Client
import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import itmo.ru.puzzle.dto.toDTO
import itmo.ru.puzzle.dto.CursorDTO
import kotlinx.serialization.Serializable

@Serializable
data class ConnectResponse(val clientId: String) : Response(Method.CONNECT)

fun Client.toConnectResponse() = ConnectResponse(id.value)

@Serializable
data class ClientCursorUpdateDTO(val id: String, val cursor: CursorDTO?)

fun Client.toDTO() = ClientCursorUpdateDTO(id.value, cursor?.toDTO())

