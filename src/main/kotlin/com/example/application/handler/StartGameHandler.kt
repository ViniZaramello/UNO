package com.example.application.handler

import MyMessages.game_in_progress
import MyMessages.min_player_requirement
import MyMessages.passphrase_invalid
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
            PLAYING -> throw IllegalStateException(game_in_progress.toString())
            FINISHED -> game.resetGame()
            else -> game.status = WAITING
        }

        require(player.passphrase == command.passphrase) { passphrase_invalid }

        player.isOwner()
        require(game.players.size > 1) { min_player_requirement }
        game.initialCards()
        game.status = PLAYING
    }
}
