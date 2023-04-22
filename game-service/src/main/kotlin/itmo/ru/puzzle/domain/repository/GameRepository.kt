package itmo.ru.puzzle.domain.repository

import itmo.ru.puzzle.domain.model.Game

class GameRepository {
    private val gameDatabase = mutableMapOf<String, Game>()

    fun put(game: Game) {
        gameDatabase.putIfAbsent(game.id.value, game)
    }

    fun remove(game: Game) {
        gameDatabase.remove(game.id.value)
    }

    fun get(gameId: String): Game? {
        return gameDatabase[gameId]
    }

    fun contains(gameId: String): Boolean {
        return gameDatabase.containsKey(gameId)
    }

    fun getAllGames(): List<Game> {
        return gameDatabase.values.toList()
    }
}