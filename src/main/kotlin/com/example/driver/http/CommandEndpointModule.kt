package com.example.driver.http

import com.example.application.command.CreateGame
import com.example.application.command.EndGame
import com.example.application.command.JoinPlayerInGame
import com.example.application.command.StartGame
import com.example.application.ports.inbound.CommandHandler
import com.example.driver.http.createGame.createGameRoute
import com.example.driver.http.endGame.endGameRoute
import com.example.driver.http.joinGame.joinGameRoute
import com.example.driver.http.startGame.startGameRoute
import io.ktor.server.application.Application

fun Application.commandEndpointConfig(
    createGame: CommandHandler<CreateGame, String>,
    joinPlayerInGame: CommandHandler<JoinPlayerInGame, Unit>,
    endGame: CommandHandler<EndGame, Unit>,
    startGame: CommandHandler<StartGame, Unit>
){
    createGameRoute(createGame)
    joinGameRoute(joinPlayerInGame)
    endGameRoute(endGame)
    startGameRoute(startGame)
}
