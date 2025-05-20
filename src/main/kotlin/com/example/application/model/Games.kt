package com.example.application.model

data class Games(val games: MutableList<Game> = mutableListOf()) {
    fun addGame(game: Game) {
        games.add(game)
    }

    fun removeGame(game: Game) {
        games.remove(game)
    }

    fun findGameById(id: String): Game {
        return games.find { it.id.toString() == id }
            ?: throw IllegalArgumentException("Game $id not found")
    }
}
