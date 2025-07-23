package com.example.application.model

import MyMessages.game_not_found
import com.example.configuration.NotFoundException
import java.util.UUID

object Games {
    val games: MutableList<Game> = mutableListOf()

    fun addGame(game: Game) {
        games.add(game)
    }

    fun removeGame(game: Game) {
        games.remove(game)
    }

    fun findGameById(id: UUID): Game {
        return games.find { it.id == id }
            ?: throw NotFoundException(message = game_not_found(id))
    }
}
