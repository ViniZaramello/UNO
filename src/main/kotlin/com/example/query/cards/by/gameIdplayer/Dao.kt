package com.example.query.cards.by.gameIdplayer

import MyMessages.passphrase_invalid
import com.example.application.model.Card
import com.example.application.model.Games

class Dao(
    private val games: Games
) {
    fun getPlayerHandCards(gameId: String, playerName: String, passphrase: String): List<Card> {
        val game = games.findGameById(gameId)
        val player = game.findPlayer(playerName)

        require(player.passphrase == passphrase) { passphrase_invalid }

        return player.cards
    }
}