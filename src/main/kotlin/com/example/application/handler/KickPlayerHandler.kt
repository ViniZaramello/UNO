package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.KickPlayer
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class KickPlayerHandler(
    private val games: Games,
) : CommandHandler<KickPlayer, Unit> {
    override suspend fun handle(command: KickPlayer) {
        val game = games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        player.isOwner()

        val targetPlayer = game.findPlayer(command.playerTarget)
        game.transferAllCards(targetPlayer)
        game.removePlayer(targetPlayer)
        game.reorderPlayers()
    }
}
