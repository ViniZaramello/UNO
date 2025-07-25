package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.EndGame
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.Games
import com.example.application.model.PlayerStatus
import com.example.application.ports.inbound.CommandHandler

class EndGameHandler : CommandHandler<EndGame, Unit> {
    override suspend fun handle(command: EndGame) {
        val game = Games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }

        player.isOwner()
        game.status = FINISHED
        game.changePlayerStatus(PlayerStatus.FINISHED)
    }
}
