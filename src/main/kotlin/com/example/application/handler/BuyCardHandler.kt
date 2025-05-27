package com.example.application.handler

import com.example.application.command.BuyCard
import com.example.application.model.Card
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class BuyCardHandler(
    private val games: Games,
) : CommandHandler<BuyCard, Card> {
    override suspend fun handle(command: BuyCard): Card {
        val game = games.findGameById(command.gameId.toString())
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { "Invalid passphrase." }
        require(game.playerTurn(player)) { "It's not your turn to buy a card."}

        val card = game.stacks.getRandomCard()
        player.cards.add(card)

        if (!game.stacks.verifyParity(card))
            game.passTurn()

        return card
    }
}
