package com.example.application.handler

import MyMessages.passphrase_invalid
import com.example.application.command.KickPlayer
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.GameRepository

class KickPlayerHandler(
    private val repository: GameRepository
) : CommandHandler<KickPlayer, Unit> {
    override suspend fun handle(command: KickPlayer) {
        val game = repository.findById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        player.isOwner()

        val targetPlayer = game.findPlayer(command.playerTarget)

        if (game.playerTurn(targetPlayer)) {
            game.passTurn()
        }
        game.transferAllCards(targetPlayer)
        game.removePlayer(targetPlayer)
        game.reorderPlayers()

        repository.save(game)
    }
}
