package com.example.application.handler

import com.example.application.command.CreateGame
import com.example.application.command.JoinPlayerInGame
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.Game

class CreateGameHandler(
    private val game: Game,
) : CommandHandler<CreateGame, Unit> {
    override suspend fun handle(command: CreateGame) {
        val (playerInfo, gameInfo) = command
//        gameInfo.players.add(playerInfo)

    }
}