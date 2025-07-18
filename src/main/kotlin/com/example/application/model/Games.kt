package com.example.application.model

import MyMessages.game_not_found
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
            ?: throw IllegalArgumentException(game_not_found(id))
    }
}
