package itmo.ru.data.game

import itmo.ru.data.client.Client

class GamesRepository {
    private val gameMap = mutableMapOf<GameId, Game>()

    fun put(game: Game) {
        gameMap.putIfAbsent(game.gameId, game)
    }

    fun addClient(gameId: GameId, client: Client) {
        gameMap[gameId]?.clients?.add(client)
    }
}