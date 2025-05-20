package com.example.driver.http

import com.example.application.command.CreateGame
import com.example.application.command.JoinPlayerInGame
import com.example.application.ports.inbound.CommandHandler
import com.example.driver.http.createGame.createGameRoute
import com.example.driver.http.joinGame.joinGameRoute
import io.ktor.server.application.*

fun Application.endpointConfig(
    createGame: CommandHandler<CreateGame, String>,
    joinPlayerInGame: CommandHandler<JoinPlayerInGame, Unit>
){
    createGameRoute(createGame)
    joinGameRoute(joinPlayerInGame)
}