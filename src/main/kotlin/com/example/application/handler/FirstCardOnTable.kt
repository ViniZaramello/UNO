package com.example.application.handler

import com.example.application.model.Card
import com.example.application.model.SpecialType
import com.example.application.model.vo.StackCards

class FirstCardOnTable(
    private val stackCards: StackCards = StackCards()
) {
    fun firstCardOnStack(): Card {
        val card = stackCards.getRandomCard()
        if (card.especial == SpecialType.NONE) {
            return card
        }
        return firstCardOnStack()
    }
}