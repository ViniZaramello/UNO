package com.example.application.handler

import com.example.application.command.EndGame
import com.example.application.model.GameStatus.FINISHED
import com.example.application.model.Games
import com.example.application.ports.inbound.CommandHandler

class EndGameHandler(
    private val games: Games
) : CommandHandler<EndGame, Unit> {
    override suspend fun handle(command: EndGame) {
        val game = games.findGameById(command.gameId)
        val player = game.findPlayer(command.playerName)

        player.isOwner()
        game.status = FINISHED
    }
}
