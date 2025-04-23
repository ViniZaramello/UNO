package com.example.application.model.vo

import com.example.application.handler.FirstCardOnTable
import com.example.application.model.Card
import com.example.application.model.newCardDeck

class StackCards(
    val cardsInDeck: MutableList<Card> = newCardDeck.toMutableList(),
    val cardsInTable: MutableList<Card> = mutableListOf(FirstCardOnTable().firstCardOnStack())
) {
    private fun stackIsEmpty(): Boolean = cardsInDeck.isEmpty()

    fun getRandomCard(): Card {
        if (stackIsEmpty()) {
            refillStack(cardsInTable)
        }
        val card = cardsInDeck.random()
        cardsInDeck.remove(card)
        return card
    }

    private fun refillStack(cardList: MutableList<Card>) {
        val lastCard = cardList.last()
        cardList.forEach { card: Card ->
            cardList.remove(card)
            cardsInDeck.add(card)
        }
        cardList.add(lastCard)
    }


}
