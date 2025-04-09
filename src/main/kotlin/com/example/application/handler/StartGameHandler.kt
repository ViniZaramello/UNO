package com.example.application.handler

import com.example.application.command.StartGame
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.Game

class StartGameHandler(
    private val game: Game
) : CommandHandler<StartGame, Unit> {
    override suspend fun handle(command: StartGame) {
        game.start(command.playerName, command.gameId)
    }
}