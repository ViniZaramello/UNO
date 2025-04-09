package com.example.application.model.vo

import com.example.application.model.Card
import com.example.application.model.Player
import com.example.application.model.SpecialType
import com.example.application.model.cardsInStack
import com.example.application.model.cardsInTable

class CardsMovements {
    fun addCardOnStack(card: Card, player: Player) {

    }

    fun removeCardOnStack(card: Card) {

    }

    fun getRandomCard(): Card {
        val card = cardsInStack.random()
        cardsInStack.remove(card)
        return card
    }

    fun firstCardOnStack(): Card {
        val card = getRandomCard()
        if (card.especial == SpecialType.NONE) {
            return card
        }
        return firstCardOnStack()
    }

    fun stackIsEmpty(): Boolean {
        return cardsInStack.isEmpty()
    }

    fun refillStack() {
        val lastCard = cardsInTable.last()
        cardsInTable.forEach { card: Card ->
            cardsInTable.remove(card)
            cardsInStack.add(card)
        }
        cardsInTable.add(lastCard)
    }

    fun resetGame() {
        cardsInStack.clear()
        cardsInTable.clear()

    }
}


