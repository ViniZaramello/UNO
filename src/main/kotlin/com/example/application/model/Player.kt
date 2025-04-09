package com.example.application.model

data class Player (
    val name: String,
    val number: Int = 1,
    val lastCardPlayed: Card? = null,
    val statusInGame: PlayerStatus = PlayerStatus.IN_LOBBY,
    val cards: List<Card>
)
