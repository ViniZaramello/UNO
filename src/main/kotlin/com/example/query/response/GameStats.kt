package com.example.query.response

import com.example.application.model.Card
import com.example.application.model.GameStatus
import kotlinx.serialization.Serializable


@Serializable
data class GameStats(
    val gameId: String,
    val playerTurn: String,
    val cardsInStackQuantity: Int,
    val cardsInTableQuantity: Int,
    val cardsInHandQuantity: Int,
    val lastCardInTable: Card,
    val playerStats: List<PlayerStats>,
    val reverse: Boolean,
    val status: GameStatus,

)
