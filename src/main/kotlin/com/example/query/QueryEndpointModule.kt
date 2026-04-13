package com.example.query

import com.example.application.ports.outbound.GameRepository
import com.example.query.cards.by.gameIdplayer.getPlayerHandCards
import com.example.query.status.by.gameId.getGameStats
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.queryEndpointConfig() {
    getPlayerHandCards(get<GameRepository>())
    getGameStats(get<GameRepository>())
}