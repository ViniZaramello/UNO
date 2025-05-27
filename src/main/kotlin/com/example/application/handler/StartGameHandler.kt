package com.example.application.handler

import com.example.application.command.StartGame
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.GameStatus.WAITING
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class StartGameHandler(
    private val games: Games
) : CommandHandler<StartGame, Unit> {
    override suspend fun handle(command: StartGame) {
        val game = games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        when (game.status) {
            PLAYING -> throw IllegalStateException("Game is already in progress.")
            FINISHED -> game.resetGame()
            else -> game.status = WAITING
        }

        player.isOwner()
        require(game.players.size > 1) { "At least 2 players are required for the game to start." }
        game.initialCards()
        game.status = PLAYING
    }
}
