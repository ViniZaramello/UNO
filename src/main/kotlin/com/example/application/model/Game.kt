package com.example.application.model

import com.example.application.model.vo.PlayerLimit
import com.example.application.model.vo.StackCards
import java.util.UUID

data class Game(
    val id: UUID = UUID.randomUUID(),
    val gameMode: GameMode = GameMode.DEFAULT,
    val status: GameStatus = GameStatus.CREATED,
    val playerLimit: PlayerLimit,
    val players: MutableList<Player> = mutableListOf(),
    val playerTurn: Int = 1,
    var leader: List<Player> = listOf() ,
    val stacks: StackCards = StackCards(),
    val firstCard: Card = stacks.getFirstCard()
){

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

    fun initialCards(){
        players.forEach { player ->
            player.cards.addAll(stacks.getRandomCards(7))
        }
    }
}
