package com.example.application.shared

import com.example.application.factories.Domain.card
import com.example.application.model.Card
import com.example.application.model.Colors
import com.example.application.model.Game
import com.example.application.model.Games

fun getFirstGame(): Game {
    return Games.games.first()
}

fun commonCardList(): MutableList<Card> = mutableListOf(
    card(name = "One Red", number = "1", color = Colors.RED),
    card(name = "Two Blue", number = "2", color = Colors.BLUE),
    card(name = "Three Green", number = "3", color = Colors.GREEN)
)