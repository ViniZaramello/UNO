package com.example.application.model.vo

import com.example.application.handler.FirstCardOnTable
import com.example.application.model.Card
import com.example.application.model.SpecialType.NONE
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

    fun getFirstCard(): Card{
        val card = cardsInDeck.random()
        if (card.especial != NONE) {
            getFirstCard()
        }
        cardsInDeck.remove(card)
        return card
    }

    fun getRandomCards(numberOfCards: Int): MutableList<Card> {
        val cards = mutableListOf<Card>()
        repeat(numberOfCards) {
            if (stackIsEmpty()) {
                refillStack(cardsInTable)
            }
            val card = cardsInDeck.random()
            cardsInDeck.remove(card)
            cards.add(card)
        }
        return cards
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
