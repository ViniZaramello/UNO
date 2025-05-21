package com.example.application.model

import com.example.application.model.vo.PlayerLimit
import com.example.application.model.vo.StackCards
import java.util.UUID

data class Game(
    val id: UUID = UUID.randomUUID(),
    val gameMode: GameMode = GameMode.DEFAULT,
    var status: GameStatus = GameStatus.CREATED,
    val playerLimit: PlayerLimit = PlayerLimit(),
    val players: MutableList<Player> = mutableListOf(),
    var playerTurn: Int = 1,
    var leader: List<Player> = listOf(),
    val stacks: StackCards = StackCards(),
    val firstCard: Card = stacks.cardsInTable.first()
) {

    fun resetGame() {
        stacks.cardsInDeck.clear()
        stacks.cardsInTable.clear()
        stacks.cardsInDeck.addAll(newCardDeck)
        players.forEach { player ->
            player.resetCards()
            player.lastCard = false
            player.statusInGame = PlayerStatus.IN_LOBBY
        }
    }

    fun initialCards() {
        players.forEach { player ->
            player.cards.addAll(stacks.getRandomCards(7))
        }
    }

    fun playerTurn(player: Player) = (playerTurn == player.number)

    fun playerNumber(): Int {
        check(players.size < playerLimit.playerLimit) { "Player limit exceeded for $playerLimit" }
        return players.size + 1
    }

    fun verifyPlayerExists(player: Player): Boolean {
        val playerInGame = players.find { it.name == player.name }
        require(playerInGame != null) { throw IllegalArgumentException("Player ${player.name} already exists in game") }
        return true
    }

    fun findPlayer(playerName: String): Player {
        val player = players.find { it.name == playerName }
            ?: throw IllegalArgumentException("Player $playerName not found in game")
        return player
    }
}
