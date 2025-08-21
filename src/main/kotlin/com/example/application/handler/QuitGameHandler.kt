package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.QuitGame
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class QuitGameHandler : CommandHandler<QuitGame, Unit> {
    override suspend fun handle(command: QuitGame) {
        val game = Games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }

        if (game.status == GameStatus.PLAYING) {
            game.transferAllCards(player)
        }

        game.removePlayer(player)
        game.reorderPlayers()

        if(player.owner){
            game.players.first().owner = true
        }
    }
}
