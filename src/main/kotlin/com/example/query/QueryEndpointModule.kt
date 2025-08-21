package com.example.query

import com.example.query.cards.by.gameIdplayer.getPlayerHandCards
import com.example.query.status.by.gameId.getGameStats
import io.ktor.server.application.Application

fun Application.queryEndpointConfig(){
    getPlayerHandCards()
    getGameStats()
}