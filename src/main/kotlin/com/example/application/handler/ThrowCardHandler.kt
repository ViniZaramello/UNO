package com.example.application.handler

import com.example.application.command.ThrowCard
import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.Game
import com.example.application.model.Games
import com.example.application.model.Player
import com.example.application.ports.inbound.CommandHandler

class ThrowCardHandler(
    private val games: Games
) : CommandHandler<ThrowCard, Unit> {
    override suspend fun handle(command: ThrowCard) {
        val (gameId, playerName, passphrase, cardId, color) = command
        val game = games.findGameById(gameId.toString())
        val player = game.findPlayer(playerName)

        require(player.passphrase == passphrase) { "Invalid passphrase." }
        game.playerTurn(player)

        lastCardVerification(player, game)

        val card = player.getCardById(cardId)

        require(game.stacks.verifyParity(card)) { "Card ${card.number} does not match the current parity." }
        if (game.blockPending && card.number != "block") {
            game.blockPending = false
            game.passTurn()
            return
        }

        if (game.buyCardQuantity > 0 && card.number == "plusTwo" || game.buyCardQuantity > 0 && card.number == "plusFour") {
            game.discountPendingCard(player)
            game.passTurn()
            return
        }

        game.passTurn()

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

    private fun lastCardVerification(player: Player, game: Game) {
        if (!player.isLastCard()) {
            val penaltyCards: List<Card> = game.stacks.getRandomCards(2)
            player.lastCard = false
            player.cards.addAll(penaltyCards)
        }
    }
}
