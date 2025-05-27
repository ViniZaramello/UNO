package com.example.application.model

import java.util.UUID

data class Player (
    val name: String,
    var number: Int = 1,
    val lastCardPlayed: Card? = null,
    var lastCard: Boolean = false,
    val passphrase: String,
    val owner: Boolean = false,
    var statusInGame: PlayerStatus = PlayerStatus.IN_LOBBY,
    val cards: MutableList<Card> = mutableListOf(),
) {
    fun resetCards(){
        cards.clear()
    }

    fun isLastCard() = lastCard && cards.size == 1

    fun isOwner() = require(owner) { "Player $name is not the owner of game" }

    fun getCardById(cardId: UUID) : Card {
        return cards.find { it.id == cardId.toString() }
            ?: throw IllegalArgumentException("Card with id $cardId not found in player's hand")
    }
}
