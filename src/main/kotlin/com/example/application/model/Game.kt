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
    val firstCard: Card = stacks.cardsInTable.first(),
    var buyCardQuantity: Int = 0,
    var reverse: Boolean = false,
    var blockPending: Boolean = false
) {

    fun resetGame() {
        stacks.cardsInDeck.clear()
        stacks.cardsInTable.clear()
        stacks.cardsInDeck.addAll(newCardDeck)
        stacks.cardsInTable.add(stacks.getFirstCard())
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

    fun passTurn() {
        playerTurn = when (reverse) {
            true -> {
                if (playerTurn < players.size) playerTurn - 1 else players.size
            }

            false -> {
                if (playerTurn < players.size) playerTurn + 1 else 1
            }
        }
    }

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

    private fun findPlayerByTurn(turn: Int): Player {
        return players.find { it.number == turn }
            ?: throw IllegalArgumentException("Player with turn $turn not found in game")
    }

    private fun verifyNextPlayerHaveBlockCard(card: Card): Boolean {
        val nextPlayer = findPlayerByTurn(playerTurn)
        val cardNextPlayer = nextPlayer.cards.find { it.name == card.name }
        return cardNextPlayer != null
    }

    private fun verifyNextPlayerHavePurchaseCard(): Boolean {
        val nextPlayer = findPlayerByTurn(playerTurn)
        val cardNextPlayer = nextPlayer.cards.find { it.name == "plusTwo" || it.name == "plusFour" }
        return cardNextPlayer != null
    }

    fun blockPlayer(card: Card) {
        if (!verifyNextPlayerHaveBlockCard(card)) {
            passTurn()
            return
        }
        blockPending = true
    }

    fun purchasePlayer(card: Card) {
        if (!verifyNextPlayerHavePurchaseCard()) {
            when (card.number) {
                "plusTwo" -> buyCardQuantity += 2
                "plusFour" -> buyCardQuantity += 4
            }
            return
        }
        val nextPlayer = findPlayerByTurn(playerTurn)
        nextPlayer.cards.addAll(stacks.getRandomCards(buyCardQuantity))
        buyCardQuantity = 0
    }

    fun reverseTurn() {
        if(reverse) {
            reverse = false
            return
        }
        reverse = true
    }

    fun discountPendingCard(player: Player){
            player.cards.addAll(stacks.getRandomCards(buyCardQuantity))
            buyCardQuantity = 0
    }
}
