package com.example.application.handler

import com.example.application.command.ThrowCard
import com.example.application.model.Colors
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class ThrowCardHandler(
    private val games: Games
) : CommandHandler<ThrowCard, Unit> {
    override suspend fun handle(command: ThrowCard) {
        val ( gameId, playerName, passphrase, cardId, color) = command
        val game = games.findGameById(gameId.toString())
        val player = game.findPlayer(playerName)

        require(player.passphrase == passphrase) { "Invalid passphrase." }
        player.isOwner()

        game.passTurn()

        val card = player.getCardById(cardId)

        when (card.number) {
            "block" -> game.blockPlayer(card)
            "changeColor" -> {
                require(color != null) { "Use of the color change chart requires a color specification" }
                card.color = Colors.valueOf(color)
            }
            "reverse" -> game.reverseTurn()
            "plusTwo", "plusFour" -> game.purchasePlayer(card)
        }
        game.stacks.throwCard(card, player)
    }
}
