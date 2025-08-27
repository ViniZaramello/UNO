package com.example.query.status.by.gameId

import com.example.application.model.Games
import com.example.query.response.PlayerStats
import com.example.query.response.GameStats
import java.util.UUID

class Dao {
    fun getGameStats(gameId: UUID): GameStats {
        val game = Games.findGameById(gameId)
        val playerTurn = game.findPlayerByTurn(game.playerTurn)

        val gameStats = GameStats(
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

        return gameStats

    }
}

