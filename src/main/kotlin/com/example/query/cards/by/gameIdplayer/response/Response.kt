package com.example.query.cards.by.gameIdplayer.response

import com.example.query.response.GameStats
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val player: PlayerDetails,
    val gameStats: GameStats
)
