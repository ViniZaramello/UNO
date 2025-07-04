package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.QuitGame
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class QuitGameHandler(
    private val games: Games,
) : CommandHandler<QuitGame, Unit> {
    override suspend fun handle(command: QuitGame) {
        val game = games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }

        game.removePlayer(player)
    }
}