package com.example.query

import com.example.application.model.Games
import com.example.query.cards.by.gameIdplayer.getPlayerHandCards
import com.example.query.status.by.gameId.getGameStats
import io.ktor.server.application.Application

fun Application.queryEndpointConfig(games: Games){
    getPlayerHandCards(games)
    getGameStats(games)
}