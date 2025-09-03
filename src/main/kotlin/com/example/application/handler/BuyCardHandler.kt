package com.example.application.handler

import MyMessages.its_not_your_turn_to_buy
import MyMessages.need_game_started
import MyMessages.passphrase_invalid
import MyMessages.you_have_buy_a_parity_card
import com.example.application.command.BuyCard
import com.example.application.model.Card
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class BuyCardHandler : CommandHandler<BuyCard, Card> {
    override suspend fun handle(command: BuyCard): Card {
        val game = Games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        require(game.playerTurn(player)) { its_not_your_turn_to_buy }
        require(game.status == GameStatus.PLAYING) { need_game_started }

        if (player.buyParityCard) {
            game.passTurn()
            player.buyParityCard = false
            throw IllegalStateException(you_have_buy_a_parity_card.toString())
        }

        val card = game.stacks.getRandomCard()
        player.cards.add(card)

        if (!game.stacks.verifyParity(card)) {
            game.passTurn()
        }

        player.buyParityCard = true
        return card
    }
}
