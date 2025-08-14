package com.example.application.model

import java.util.UUID

private val ACTIONCARDS: List<Card> = listOf(
    Card(number = "block", color = Colors.YELLOW, especial = SpecialType.BLOCK, name = "block yellow"),
    Card(number = "reverse", color = Colors.YELLOW, especial = SpecialType.REVERSE, name = "reverse yellow"),
    Card(number = "plusTwo", color = Colors.YELLOW, especial = SpecialType.BUY_TWO, name = "buyTwo yellow"),
    Card(number = "block", color = Colors.RED, especial = SpecialType.BLOCK, name = "block red"),
    Card(number = "reverse", color = Colors.RED, especial = SpecialType.REVERSE, name = "reverse red"),
    Card(number = "plusTwo", color = Colors.RED, especial = SpecialType.BUY_TWO, name = "buyTwo red"),
    Card(number = "block", color = Colors.GREEN, especial = SpecialType.BLOCK, name = "block green"),
    Card(number = "reverse", color = Colors.GREEN, especial = SpecialType.REVERSE, name = "reverse green"),
    Card(number = "plusTwo", color = Colors.GREEN, especial = SpecialType.BUY_TWO, name = "buyTwo green"),
    Card(number = "block", color = Colors.BLUE, especial = SpecialType.BLOCK, name = "block blue"),
    Card(number = "reverse", color = Colors.BLUE, especial = SpecialType.REVERSE, name = "reverse blue"),
    Card(number = "plusTwo", color = Colors.BLUE, especial = SpecialType.BUY_TWO, name = "buyTwo blue")
)
private val ZEROCARDS: List<Card> = listOf(
    Card(number = "0", color = Colors.YELLOW, especial = SpecialType.NONE, name = "zero yellow"),
    Card(number = "0", color = Colors.RED, especial = SpecialType.NONE, name = "zero red"),
    Card(number = "0", color = Colors.GREEN, especial = SpecialType.NONE, name = "zero green"),
    Card(number = "0", color = Colors.BLUE, especial = SpecialType.NONE, name = "zero blue"),
)

private val SPECIALCARDS: List<Card> = listOf(
    Card(number = "plusFour", color = Colors.BLACK, especial = SpecialType.BUY_FOUR, name = "buy-four"),
    Card(number = "changeColor", color = Colors.BLACK, especial = SpecialType.CHANGE_COLOR, name = "change-color"),
)

private val NUMERICCARDS = listOf(
    Card(number = "1", color = Colors.YELLOW, especial = SpecialType.NONE, name = "one yellow"),
    Card(number = "2", color = Colors.YELLOW, especial = SpecialType.NONE, name = "two yellow"),
    Card(number = "3", color = Colors.YELLOW, especial = SpecialType.NONE, name = "three yellow"),
    Card(number = "4", color = Colors.YELLOW, especial = SpecialType.NONE, name = "four yellow"),
    Card(number = "5", color = Colors.YELLOW, especial = SpecialType.NONE, name = "five yellow"),
    Card(number = "6", color = Colors.YELLOW, especial = SpecialType.NONE, name = "six yellow"),
    Card(number = "7", color = Colors.YELLOW, especial = SpecialType.NONE, name = "seven yellow"),
    Card(number = "8", color = Colors.YELLOW, especial = SpecialType.NONE, name = "eight yellow"),
    Card(number = "9", color = Colors.YELLOW, especial = SpecialType.NONE, name = "nine yellow"),

    Card(number = "1", color = Colors.RED, especial = SpecialType.NONE, name = "one red"),
    Card(number = "2", color = Colors.RED, especial = SpecialType.NONE, name = "two red"),
    Card(number = "3", color = Colors.RED, especial = SpecialType.NONE, name = "three red"),
    Card(number = "4", color = Colors.RED, especial = SpecialType.NONE, name = "four red"),
    Card(number = "5", color = Colors.RED, especial = SpecialType.NONE, name = "five red"),
    Card(number = "6", color = Colors.RED, especial = SpecialType.NONE, name = "six red"),
    Card(number = "7", color = Colors.RED, especial = SpecialType.NONE, name = "seven red"),
    Card(number = "8", color = Colors.RED, especial = SpecialType.NONE, name = "eight red"),
    Card(number = "9", color = Colors.RED, especial = SpecialType.NONE, name = "nine red"),

    Card(number = "1", color = Colors.GREEN, especial = SpecialType.NONE, name = "one green"),
    Card(number = "2", color = Colors.GREEN, especial = SpecialType.NONE, name = "two green"),
    Card(number = "3", color = Colors.GREEN, especial = SpecialType.NONE, name = "three green"),
    Card(number = "4", color = Colors.GREEN, especial = SpecialType.NONE, name = "four green"),
    Card(number = "5", color = Colors.GREEN, especial = SpecialType.NONE, name = "five green"),
    Card(number = "6", color = Colors.GREEN, especial = SpecialType.NONE, name = "six green"),
    Card(number = "7", color = Colors.GREEN, especial = SpecialType.NONE, name = "seven green"),
    Card(number = "8", color = Colors.GREEN, especial = SpecialType.NONE, name = "eight green"),
    Card(number = "9", color = Colors.GREEN, especial = SpecialType.NONE, name = "nine green"),

    Card(number = "1", color = Colors.BLUE, especial = SpecialType.NONE, name = "one blue"),
    Card(number = "2", color = Colors.BLUE, especial = SpecialType.NONE, name = "two blue"),
    Card(number = "3", color = Colors.BLUE, especial = SpecialType.NONE, name = "three blue"),
    Card(number = "4", color = Colors.BLUE, especial = SpecialType.NONE, name = "four blue"),
    Card(number = "5", color = Colors.BLUE, especial = SpecialType.NONE, name = "five blue"),
    Card(number = "6", color = Colors.BLUE, especial = SpecialType.NONE, name = "six blue"),
    Card(number = "7", color = Colors.BLUE, especial = SpecialType.NONE, name = "seven blue"),
    Card(number = "8", color = Colors.BLUE, especial = SpecialType.NONE, name = "eight blue"),
    Card(number = "9", color = Colors.BLUE, especial = SpecialType.NONE, name = "nine blue"),
)

private val DECK = buildList {
    NUMERICCARDS.forEach { card: Card ->
        repeat(2) {
            add(card.copy(id = UUID.randomUUID().toString()))
        }
    }
    ZEROCARDS.forEach { card: Card ->
        add(card.copy())
    }
    ACTIONCARDS.forEach { card: Card ->
        repeat(2) {
            add(card.copy(id = UUID.randomUUID().toString()))
        }
    }
    SPECIALCARDS.forEach { card: Card ->
        repeat(4) {
            add(card.copy(id = UUID.randomUUID().toString()))
        }
    }
}

val newCardDeck: List<Card> = DECK
