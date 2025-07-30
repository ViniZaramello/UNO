package com.example.application.model

import MyMessages.player_limit_exceeded
import MyMessages.player_not_found
import MyMessages.player_turn_not_found
import com.example.application.model.vo.PlayerLimit
import com.example.application.model.vo.StackCards
import com.example.configuration.NotFoundException
import java.util.UUID

data class Game(
    val id: UUID = UUID.randomUUID(),
    val gameMode: GameMode = GameMode.DEFAULT,
    var status: GameStatus = GameStatus.CREATED,
    val playerLimit: PlayerLimit = PlayerLimit(),
    val players: MutableList<Player> = mutableListOf(),
    var playerTurn: Int = 1,
    val stacks: StackCards = StackCards(),
    var firstCard: Card = stacks.cardsInTable.first(),
    var buyCardQuantity: Int = 0,
    var reverse: Boolean = false,
    var blockPending: Boolean = false
) {

    fun resetGame() {
        stacks.cardsInDeck.clear()
        stacks.cardsInTable.clear()
        stacks.cardsInDeck.addAll(newCardDeck)
        stacks.cardsInTable.add(stacks.getFirstCard())
        reverse = false
        blockPending = false
        buyCardQuantity = 0
        firstCard = stacks.cardsInTable.first()

        players.forEach { player ->
            player.resetCards()
            player.lastCard = false
            player.statusInGame = PlayerStatus.IN_LOBBY
            this.status = GameStatus.WAITING
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
        check(players.size < playerLimit.playerLimit) { player_limit_exceeded(playerLimit.playerLimit) }
        return players.size + 1
    }

    fun verifyPlayerExists(player: Player): Boolean {
        val playerInGame = players.find { it.name == player.name }
        return playerInGame == null
    }

    fun findPlayer(playerName: String): Player {
        val player = players.find { it.name == playerName }
            ?: throw NotFoundException(player_not_found(playerName))
        return player
    }

    //TODO: Criar um log de WARN caso essa exception ocorra
    fun findPlayerByTurn(turn: Int): Player {
        return players.find { it.number == turn }
            ?: throw IllegalArgumentException(player_turn_not_found(turn))
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
        nextPlayer.lastCard = false
        buyCardQuantity = 0
    }

    fun reverseTurn() {
        if (reverse) {
            reverse = false
            return
        }
        reverse = true
    }

    fun discountPendingCard(player: Player) {
        player.cards.addAll(stacks.getRandomCards(buyCardQuantity))
        buyCardQuantity = 0
    }

    fun changePlayerStatus(status: PlayerStatus) {
        players.forEach { player ->
            player.statusInGame = status
        }
    }

    fun removePlayer(player: Player) = players.remove(player)
}
