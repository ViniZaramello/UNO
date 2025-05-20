package com.example.application.handler

import com.example.application.command.StartGame
import com.example.application.model.GameStatus.PLAYING
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class StartGameHandler(
    private val games: Games
) : CommandHandler<StartGame, Unit> {
    override suspend fun handle(command: StartGame) {
        val game = games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        player.isOwner()
        game.status = PLAYING
    }
}
