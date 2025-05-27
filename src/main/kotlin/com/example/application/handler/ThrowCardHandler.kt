package com.example.application.handler

import com.example.application.command.ThrowCard
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class ThrowCardHandler(
    private val games: Games
) : CommandHandler<ThrowCard, Unit> {
    override suspend fun handle(command: ThrowCard) {
        val game = games.findGameById(command.gameId.toString())
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { "Invalid passphrase." }
        player.isOwner()

        game.passTurn()

        val card = player.getCardById(command.cardId)

        when(card.number)
        {
            "block" -> game.blockPlayer(card)
            "changeColor" -> game.changeColor(command.color, card)
            "reverse" -> game.reverseTurn()
            "plusTwo", "plusFour" -> game.purchasePlayer(card)
        }
        game.stacks.throwCard(card, player)
    }
}