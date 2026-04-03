package com.example.application.handler

import com.example.application.command.CreateGame
import com.example.application.model.Game
import com.example.application.ports.inbound.CommandHandler
import com.example.application.ports.outbound.GameRepository

class CreateGameHandler(
    private val repository: GameRepository
) : CommandHandler<CreateGame, String> {
    override suspend fun handle(command: CreateGame): String {
        val game = Game(players = mutableListOf(command.player))

        repository.save(game)

        return game.id.toString()
    }
}
