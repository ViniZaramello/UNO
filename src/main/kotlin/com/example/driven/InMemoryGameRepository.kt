package com.example.driven

import MyMessages.game_not_found
import com.example.application.model.Game
import com.example.application.ports.outbound.GameRepository
import com.example.configuration.NotFoundException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryGameRepository : GameRepository {

    private val games: MutableMap<UUID, Game> = ConcurrentHashMap()

    override fun save(game: Game): Game {
        games[game.id] = game
        return game
    }

    override fun findById(id: UUID): Game {
        return games[id] ?: throw NotFoundException(message = game_not_found(id).toString())
    }

    override fun delete(game: Game) {
        games.remove(game.id)
    }

    // Helpers for testing and queries
    fun size(): Int = games.size

    fun findAll(): List<Game> = games.values.toList()

    fun clear() {
        games.clear()
    }
}

