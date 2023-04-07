package itmo.ru.data

import itmo.ru.data.client.ClientId
import itmo.ru.data.game.GameId
import kotlinx.serialization.Serializable

@Serializable
data class JoinRequest(val clientId: ClientId, val gameId: GameId)

@Serializable
data class SetRequest(val clientId: ClientId, val gameId: GameId, val ball: Ball)