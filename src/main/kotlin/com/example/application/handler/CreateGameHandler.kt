package com.example.application.handler

import com.example.application.command.CreateGame
import com.example.application.model.Game
import com.example.application.model.Games
import com.example.application.model.vo.PlayerLimit
import com.example.application.ports.inbound.CommandHandler

class CreateGameHandler(
) : CommandHandler<CreateGame, String> {
    override suspend fun handle(command: CreateGame): String {

        val game = Game(
            players = mutableListOf(command.player),
        )

        Games().addGame(game)

        return game.id.toString()
    }
}