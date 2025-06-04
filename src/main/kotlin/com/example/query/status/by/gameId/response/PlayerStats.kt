package com.example.query.status.by.gameId.response

import kotlinx.serialization.Serializable

@Serializable
data class PlayerStats(
    val playerName : String,
    val cardsQuantity: Int,
    val lastCard: Boolean,
)
