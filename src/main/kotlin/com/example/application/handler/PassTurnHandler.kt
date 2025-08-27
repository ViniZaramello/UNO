package com.example.application.handler

import MyMessages.its_not_your_turn_to_buy
import MyMessages.need_game_started
import MyMessages.passphrase_invalid
import MyMessages.require_buy_card
import com.example.application.command.PassTurn
import com.example.application.model.GameStatus
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class PassTurnHandler : CommandHandler<PassTurn, Unit> {
    override suspend fun handle(command: PassTurn) {
        val game = Games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        require(player.passphrase == command.passphrase) { passphrase_invalid }
        require(game.playerTurn(player)) { its_not_your_turn_to_buy }
        require(game.status == GameStatus.PLAYING) { need_game_started }

        require(player.buyParityCard) { require_buy_card }
        player.buyParityCard = false

        game.passTurn()
    }
}