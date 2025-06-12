package com.example.application.handler

import MyMessages.its_not_your_turn_to_buy
import MyMessages.passphrase_invalid
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

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        require(game.playerTurn(player)) { its_not_your_turn_to_buy }

        val card = game.stacks.getRandomCard()
        player.cards.add(card)

        if (!game.stacks.verifyParity(card))
            game.passTurn()

        return card
    }
}
