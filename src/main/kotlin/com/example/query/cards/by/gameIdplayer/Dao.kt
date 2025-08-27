package com.example.query.cards.by.gameIdplayer

import MyMessages.passphrase_invalid
import com.example.application.model.Games
import com.example.query.cards.by.gameIdplayer.response.PlayerDetails
import com.example.query.cards.by.gameIdplayer.response.Response
import com.example.query.response.GameStats
import com.example.query.response.PlayerStats
import java.util.UUID

class Dao(
    private val games: Games
) {
    fun getPlayerHandCards(gameId: UUID, playerName: String, passphrase: String): Response {
        val game = games.findGameById(gameId)
        val player = game.findPlayer(playerName)
        val playerTurn = game.findPlayerByTurn(game.playerTurn)

        val response = Response(
            player = PlayerDetails(
                name = player.name,
                number = player.number,
                isYourTurn = game.playerTurn(player),
                lastCard = player.lastCard,
                totalCards = player.cards.size,
                cards = player.cards,
                playerStatus = player.statusInGame,
            ),
            gameStats = GameStats(
                gameId = game.id.toString(),
                playerTurn = playerTurn.name,
                cardsInStackQuantity = game.stacks.cardsInDeck.size,
                cardsInTableQuantity = game.stacks.cardsInTable.size,
                cardsInHandQuantity = game.players.sumOf { it.cards.size },
                lastCardInTable = game.stacks.cardsInTable.lastOrNull()!!,
                playerStats = game.players.map { player ->
                    PlayerStats(
                        playerName = player.name,
                        cardsQuantity = player.cards.size,
                        lastCard = player.lastCard
                    )
                },
                reverse = game.reverse,
                status = game.status
            )

        )
        require(player.passphrase == passphrase) { passphrase_invalid }

        return response
    }
}