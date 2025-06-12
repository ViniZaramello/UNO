package com.example.application.model

import MyMessages.player_is_not_owner
import MyMessages.player_is_not_have_this_card
import java.util.UUID

data class Player(
    val name: String,
    var number: Int = 1,
    val lastCardPlayed: Card? = null,
    var lastCard: Boolean = false,
    val passphrase: String,
    val owner: Boolean = false,
    var statusInGame: PlayerStatus = PlayerStatus.IN_LOBBY,
    val cards: MutableList<Card> = mutableListOf(),
) {
    fun resetCards() {
        cards.clear()
    }

    fun isLastCard() = lastCard && cards.size == 1

    fun isOwner() = require(owner) { player_is_not_owner(name) }

    fun getCardById(cardId: UUID): Card {
        return cards.find { it.id == cardId.toString() }
            ?: throw IllegalArgumentException(player_is_not_have_this_card.toString())
    }
}
