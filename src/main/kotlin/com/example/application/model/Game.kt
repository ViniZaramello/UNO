package com.example.application.model

import com.example.application.model.vo.PlayerLimit
import com.example.application.model.vo.StackCards
import java.util.*

data class Game(
    val id: UUID = UUID.randomUUID(),
    val gameMode: GameMode = GameMode.DEFAULT,
    val status: GameStatus = GameStatus.CREATED,
    val playerLimit: PlayerLimit,
    val players: List<Player>? = null,
    val playerTurn: Int = 1,
    val leader: Player,
    val stacks: StackCards
){
    fun resetGame() {
        stacks.cardsInDeck.clear()
        stacks.cardsInTable.clear()
        stacks.cardsInDeck.addAll(newCardDeck)
    }
}
