package com.example.query.cards.by.gameIdplayer.response

import com.example.application.model.Card
import com.example.application.model.PlayerStatus
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDetails(
    val name: String,
    val number: Int,
    val isYourTurn: Boolean,
    val lastCard: Boolean,
    val totalCards: Int,
    val cards: List<Card>,
    val playerStatus: PlayerStatus,
)