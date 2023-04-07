package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Client
import itmo.ru.puzzle.domain.model.Game
import itmo.ru.puzzle.domain.model.GameId

class GamesRepository {
    private val gameMap = mutableMapOf<GameId, Game>()

    fun put(game: Game) {
        gameMap.putIfAbsent(game.id, game)
    }

    fun addClient(gameId: GameId, client: Client) {
        gameMap[gameId]?.clients?.add(client)
    }
}