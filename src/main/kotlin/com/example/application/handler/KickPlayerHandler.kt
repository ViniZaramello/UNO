package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.KickPlayer
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class KickPlayerHandler : CommandHandler<KickPlayer, Unit> {
    override suspend fun handle(command: KickPlayer) {
        val game = Games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        player.isOwner()

        val targetPlayer = game.findPlayer(command.playerTarget)

        if(game.playerTurn(targetPlayer)){
            game.passTurn()
        }
        game.transferAllCards(targetPlayer)
        game.removePlayer(targetPlayer)
        game.reorderPlayers()
    }
}
