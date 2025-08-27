package com.example.query.response

import kotlinx.serialization.Serializable

@Serializable
data class PlayerStats(
    val playerName : String,
    val cardsQuantity: Int,
    val lastCard: Boolean,
)
