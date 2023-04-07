package itmo.ru.data

import itmo.ru.plugins.Method
import itmo.ru.plugins.Response
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(val message: Message) : Response(Method.CHAT)

@Serializable
data class Message(val sender: String, val content: String, val timestamp: String)