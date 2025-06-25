package com.example.application.handler

import MyMessages.passphrase_invalid
import MyMessages.require_target_player_is_not_turn
import com.example.application.command.SkipPlayer
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class SkipPlayerHandler(
    private val games: Games
) : CommandHandler<SkipPlayer, Unit> {
    override suspend fun handle(command: SkipPlayer) {
        val game = games.findGameById(command.gameId.toString())
        val player = game.findPlayer(command.playerName)
        val targetPlayer = game.findPlayer(command.targetPlayerName)

        require(game.playerTurn(targetPlayer)) { require_target_player_is_not_turn }

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        player.isOwner()

        val penaltyCards = game.stacks.getRandomCards(2)
        targetPlayer.cards.addAll(penaltyCards)
    }
}
