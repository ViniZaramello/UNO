package com.example.application.ports.outbound

import com.example.application.model.Game
import java.util.UUID

interface Game {

    suspend fun create(game: Game): Game

    suspend fun start(playerName: String, gameId: UUID)

    suspend fun finish(gameId: UUID)

    suspend fun find(gameId: UUID): Game

}