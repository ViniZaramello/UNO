package com.example.application.handler

import MyMessages.card_no_has_parity
import MyMessages.its_not_your_turn_to_play
import MyMessages.need_game_started
import MyMessages.passphrase_invalid
import MyMessages.require_color_specification
import com.example.application.command.ThrowCard
import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.Game
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.model.Player
import com.example.application.model.PlayerStatus
import com.example.application.ports.inbound.CommandHandler

class ThrowCardHandler : CommandHandler<ThrowCard, Unit> {
    override suspend fun handle(command: ThrowCard) {
        val (gameId, playerName, passphrase, cardId, color) = command
        val game = Games.findGameById(gameId)

        require(game.status == GameStatus.PLAYING) { need_game_started }

        val player = game.findPlayer(playerName)

        require(player.passphrase == passphrase) { passphrase_invalid }
        require(game.playerTurn(player)) { its_not_your_turn_to_play }

        lastCardVerification(player, game)

        val card = player.getCardById(cardId)

        require(game.stacks.verifyParity(card)) { card_no_has_parity }
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
                require(color != null) { require_color_specification }
                card.color = findColor(color)
            }

            "reverse" -> {
                game.reverseTurn()
                game.passTurn()
                game.passTurn()
            }

            "plusTwo" -> game.purchasePlayer(card)
            "plusFour" -> {
                require(color != null) { require_color_specification }
                card.color = findColor(color)
                game.purchasePlayer(card)
            }
        }
        game.stacks.throwCard(card, player)
        if(player.cards.isEmpty()){
            player.statusInGame = PlayerStatus.FINISHED
        }
    }

    private fun lastCardVerification(player: Player, game: Game) {
        when (player.cards.size) {
            1 -> {
                if (player.lastCard) {
                    return
                } else {
                    val penaltyCards: List<Card> = game.stacks.getRandomCards(2)
                    player.lastCard = false
                    player.cards.addAll(penaltyCards)
                }
            }

            else -> return
        }
    }

    private fun findColor(color: String) = Colors.entries.find { it.name.equals(color, ignoreCase = true) }
        ?: throw IllegalArgumentException(MyMessages.invalid_color(color))
}
